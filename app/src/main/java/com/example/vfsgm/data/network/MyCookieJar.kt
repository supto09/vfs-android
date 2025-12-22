package com.example.vfsgm.data.network

import android.content.Context
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.File

object CookieJarHolder {
    lateinit var cookieJar: MyCookieJar
        private set

    fun init(context: Context) {
        cookieJar = MyCookieJar(context.applicationContext)
    }
}


class MyCookieJar(context: Context) : CookieJar {

    private val file = File(context.filesDir, "cookies.txt")
    private val cookies = mutableMapOf<String, MutableList<Cookie>>() // domain -> cookies

    init {
        if (file.exists()) {
            loadCookiesFromFile()
        } else {
            file.createNewFile()
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val domain = url.host

        // Get existing cookies for the domain, or create a new empty list if none exist
        val currentList = this.cookies[url.host] ?: mutableListOf()

        // Create a mutable list to hold merged cookies
        val mergedList = mutableListOf<Cookie>()

        // Keep a map of cookie names that were updated (to avoid duplicates)
        val updatedNames = mutableSetOf<String>()

        // Add new cookies to the merged list, and mark their names
        for (newCookie in cookies) {
            mergedList.add(newCookie)
            updatedNames.add(newCookie.name)
        }

        // Add old cookies that were not overwritten by new cookies
        for (oldCookie in currentList) {
            if (!updatedNames.contains(oldCookie.name)) {
                mergedList.add(oldCookie)
            }
        }

        // Save the merged list back to the map
        this.cookies[domain] = mergedList

        // Persist to file
        saveCookiesToFile()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
//        println("loadFor request: ${url}")
//        println("loadFor host: ${url.host}")
//        return cookies[url.host].orEmpty()


        return cookies.values.flatten().filter { cookie ->
            // not expired

            // domain match
            "https://${url.host}".toHttpUrlOrNull()?.host?.endsWith(
                cookie.domain.removePrefix(
                    "."
                )
            ) == true &&
                    // path match
                    url.encodedPath.startsWith(cookie.path) &&
                    // secure check
                    (!cookie.secure || url.isHttps)
        }
    }

    private fun loadCookiesFromFile() {
        // Dedupe by (name, domain, path), keep the newest expiresAt (and drop expired if you want)
        val now = System.currentTimeMillis()
        val dedup = LinkedHashMap<String, Cookie>() // key -> cookie (preserve file order)

        if (!file.exists()) return

        file.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                val cookie = parseCookie(line) ?: return@forEach

                // OPTIONAL: drop expired cookies while loading
                if (cookie.expiresAt <= now) return@forEach

                val key = "${cookie.name}|${cookie.domain}|${cookie.path}"
                val prev = dedup[key]

                // keep the one with later expiry (or keep the latest read if you prefer)
                if (prev == null || cookie.expiresAt >= prev.expiresAt) {
                    dedup[key] = cookie
                }
            }
        }

        cookies.clear()
        dedup.values.forEach { cookie ->
            cookies.getOrPut(cookie.domain) { mutableListOf() }.add(cookie)
        }
    }

    private fun saveCookiesToFile() {
        println("Before:")
        printAllCookies()
        println("==============")

        // Dedupe by (name, domain, path), keep the newest expiresAt (and drop expired if you want)
        val now = System.currentTimeMillis()
        val dedup = LinkedHashMap<String, Cookie>()

        cookies.values
            .flatten()
            .forEach { cookie ->
                // OPTIONAL: drop expired cookies while saving
                if (cookie.expiresAt <= now) return@forEach

                val key = "${cookie.name}|${cookie.domain}|${cookie.path}"
                val prev = dedup[key]

                if (prev == null || cookie.expiresAt >= prev.expiresAt) {
                    dedup[key] = cookie
                }
            }

        // Rewrite file from deduped set
        file.bufferedWriter().use { writer ->
            dedup.values.forEach { cookie ->
                writer.write(serializeCookie(cookie))
                writer.newLine()
            }
        }

        // Keep in-memory jar consistent with what we wrote
        cookies.clear()
        dedup.values.forEach { cookie ->
            cookies.getOrPut(cookie.domain) { mutableListOf() }.add(cookie)
        }

        println("After:")
        printAllCookies()
        println("==============")
    }

    private fun serializeCookie(cookie: Cookie): String {
        return listOf(
            cookie.name, cookie.value, cookie.domain, cookie.path,
            cookie.expiresAt.toString(), cookie.secure.toString(), cookie.httpOnly.toString()
        ).joinToString(";")
    }

    private fun parseCookie(line: String): Cookie? {
        val parts = line.split(";")
        return if (parts.size == 7) {
            try {
                Cookie.Builder()
                    .name(parts[0])
                    .value(parts[1])
                    .domain(parts[2])
                    .path(parts[3])
                    .expiresAt(parts[4].toLong())
                    .apply {
                        if (parts[5].toBoolean()) secure()
                        if (parts[6].toBoolean()) httpOnly()
                    }
                    .build()
            } catch (e: Exception) {
                null
            }
        } else null
    }

    fun clearCookies() {
        cookies.clear()
        file.writeText("")
    }

    fun getCookiesForDomain(domain: String): List<Cookie> {
        return cookies[domain].orEmpty()
    }

    fun addCookiesManually(domain: String, newCookies: List<Cookie>) {
//        cookies.getOrPut(domain) { mutableListOf() }.apply {
//            removeAll { existing -> newCookies.any { it.name == existing.name } }
//            addAll(newCookies)
//        }
//        saveCookiesToFile()

        val existingCookies = cookies.getOrPut(domain) { mutableListOf() }

        newCookies.forEach { newCookie ->
            val existingIndex = existingCookies.indexOfFirst { it.name == newCookie.name }
            if (existingIndex != -1) {
                existingCookies[existingIndex] = newCookie // Replace existing cookie
            } else {
                existingCookies.add(newCookie) // Add new cookie
            }
        }

        saveCookiesToFile()
    }

    fun printAllCookies() {
        if (cookies.isEmpty()) {
            println("🍪 CookieJar is empty.")
            return
        }

        println("🍪 Stored cookies in CookieJar:")
        cookies.forEach { (domain, cookieList) ->
            println("  ➤ Domain: $domain")
            cookieList.forEach { cookie ->
                println("     - ${cookie.name}=${cookie.value}; Path=${cookie.path};  Secure=${cookie.secure}; HttpOnly=${cookie.httpOnly}")
            }
        }
    }
}