package com.example.vfsgm.data.network

import android.content.Context
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
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

    // baseDomain -> cookies
    private val cookies = mutableMapOf<String, MutableList<Cookie>>()

    init {
        if (file.exists()) {
            loadCookiesFromFile()
        } else {
            file.createNewFile()
        }
    }

    /**
     * Your requirement: domain will always be like "something.com".
     * So base domain = last 2 labels.
     */
    private fun toBaseDomain(hostOrDomain: String): String {
        val host = hostOrDomain.trim()
            .removePrefix(".")
            .lowercase()

        val parts = host.split(".").filter { it.isNotBlank() }
        if (parts.size <= 2) return host
        return parts.takeLast(2).joinToString(".")
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val baseDomain = toBaseDomain(url.host)

        // Existing cookies for the base domain
        val currentList = this.cookies[baseDomain] ?: mutableListOf()

        // Merge by (name, path) within the base domain
        val dedup = LinkedHashMap<String, Cookie>() // key -> cookie
        fun keyOf(c: Cookie) = "${c.name}|${c.path}"

        // Put old first, then new overrides
        currentList.forEach { old ->
            val normalized = normalizeCookieToBaseDomain(old, baseDomain)
            dedup[keyOf(normalized)] = normalized
        }
        cookies.forEach { incoming ->
            val normalized = normalizeCookieToBaseDomain(incoming, baseDomain)
            dedup[keyOf(normalized)] = normalized
        }

        this.cookies[baseDomain] = dedup.values.toMutableList()
        saveCookiesToFile()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val baseDomain = toBaseDomain(url.host)
        val now = System.currentTimeMillis()

        return cookies[baseDomain]
            .orEmpty()
            .asSequence()
            .filter { it.expiresAt > now } // drop expired at request-time
            .filter { cookie ->
                // Domain match (base-domain bucket already picked, but still keep safe check)
                val reqBase = toBaseDomain(url.host)
                val cookieBase = toBaseDomain(cookie.domain)
                reqBase == cookieBase
            }
            .filter { cookie ->
                // Path match
                url.encodedPath.startsWith(cookie.path)
            }
            .filter { cookie ->
                // Secure check
                (!cookie.secure || url.isHttps)
            }
            .toList()
    }

    private fun normalizeCookieToBaseDomain(cookie: Cookie, baseDomain: String): Cookie {
        val normalizedDomain = toBaseDomain(baseDomain)

        // If already correct, keep as-is
        if (toBaseDomain(cookie.domain) == normalizedDomain && cookie.domain.removePrefix(".") == normalizedDomain) {
            // still might be ".something.com" vs "something.com" — we normalize to "something.com"
            // but OkHttp Cookie.Builder().domain() expects host-only style; it’s fine.
        }

        return Cookie.Builder()
            .name(cookie.name)
            .value(cookie.value)
            .domain(normalizedDomain)
            .path(cookie.path)
            .expiresAt(cookie.expiresAt)
            .apply {
                if (cookie.secure) secure()
                if (cookie.httpOnly) httpOnly()
            }
            .build()
    }

    private fun loadCookiesFromFile() {
        val now = System.currentTimeMillis()
        val dedup = LinkedHashMap<String, Cookie>() // (name|baseDomain|path) -> cookie

        if (!file.exists()) return

        file.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                val parsed = parseCookie(line) ?: return@forEach

                // Drop expired on load
                if (parsed.expiresAt <= now) return@forEach

                val baseDomain = toBaseDomain(parsed.domain)
                val normalized = normalizeCookieToBaseDomain(parsed, baseDomain)

                val key = "${normalized.name}|${baseDomain}|${normalized.path}"
                val prev = dedup[key]

                // keep the one with later expiry
                if (prev == null || normalized.expiresAt >= prev.expiresAt) {
                    dedup[key] = normalized
                }
            }
        }

        cookies.clear()
        dedup.values.forEach { cookie ->
            val baseDomain = toBaseDomain(cookie.domain)
            cookies.getOrPut(baseDomain) { mutableListOf() }.add(cookie)
        }
    }

    private fun saveCookiesToFile() {
        println("Before:")
        printAllCookies()
        println("==============")


        val now = System.currentTimeMillis()
        val dedup = LinkedHashMap<String, Cookie>() // (name|baseDomain|path) -> cookie

        cookies.values
            .flatten()
            .forEach { cookie ->
                // Drop expired on save
                if (cookie.expiresAt <= now) return@forEach

                val baseDomain = toBaseDomain(cookie.domain)
                val normalized = normalizeCookieToBaseDomain(cookie, baseDomain)

                val key = "${normalized.name}|${baseDomain}|${normalized.path}"
                val prev = dedup[key]

                if (prev == null || normalized.expiresAt >= prev.expiresAt) {
                    dedup[key] = normalized
                }
            }

        file.bufferedWriter().use { writer ->
            dedup.values.forEach { cookie ->
                writer.write(serializeCookie(cookie))
                writer.newLine()
            }
        }

        // keep in-memory aligned with disk
        cookies.clear()
        dedup.values.forEach { cookie ->
            val baseDomain = toBaseDomain(cookie.domain)
            cookies.getOrPut(baseDomain) { mutableListOf() }.add(cookie)
        }

        println("After:")
        printAllCookies()
        println("==============")
    }

    private fun serializeCookie(cookie: Cookie): String {
        val baseDomain = toBaseDomain(cookie.domain)
        return listOf(
            cookie.name,
            cookie.value,
            baseDomain,
            cookie.path,
            cookie.expiresAt.toString(),
            cookie.secure.toString(),
            cookie.httpOnly.toString()
        ).joinToString(";")
    }

    private fun parseCookie(line: String): Cookie? {
        val parts = line.split(";")
        if (parts.size != 7) return null

        return try {
            val baseDomain = toBaseDomain(parts[2])

            Cookie.Builder()
                .name(parts[0])
                .value(parts[1])
                .domain(baseDomain)
                .path(parts[3])
                .expiresAt(parts[4].toLong())
                .apply {
                    if (parts[5].toBoolean()) secure()
                    if (parts[6].toBoolean()) httpOnly()
                }
                .build()
        } catch (_: Exception) {
            null
        }
    }

    fun clearCookies() {
        cookies.clear()
        file.writeText("")
    }

    fun getCookiesForDomain(domain: String): List<Cookie> {
        val baseDomain = toBaseDomain(domain)
        return cookies[baseDomain].orEmpty()
    }

    fun addCookiesManually(domain: String, newCookies: List<Cookie>) {
        val baseDomain = toBaseDomain(domain)
        val existingCookies = cookies.getOrPut(baseDomain) { mutableListOf() }

        // Replace by (name, path) inside base domain
        newCookies.forEach { incoming ->
            val newCookie = normalizeCookieToBaseDomain(incoming, baseDomain)

            val existingIndex = existingCookies.indexOfFirst {
                it.name == newCookie.name && it.path == newCookie.path
            }

            if (existingIndex != -1) {
                existingCookies[existingIndex] = newCookie
            } else {
                existingCookies.add(newCookie)
            }
        }

        saveCookiesToFile()
    }

    fun printAllCookies() {
        if (cookies.isEmpty()) {
            println("🍪 CookieJar is empty.")
            return
        }

        println("🍪 Stored cookies in CookieJar (BASE-DOMAIN buckets):")
        cookies.forEach { (baseDomain, cookieList) ->
            println("  ➤ Base Domain: $baseDomain")
            cookieList.forEach { cookie ->
                println(
                    "     - ${cookie.name}=${cookie.value}; Domain=${cookie.domain}; Path=${cookie.path}; Secure=${cookie.secure}; HttpOnly=${cookie.httpOnly}; ExpiresAt=${cookie.expiresAt}"
                )
            }
        }
    }
}
