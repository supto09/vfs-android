package com.example.vfsgm.store

import kotlinx.coroutines.channels.Channel
import kotlin.let
import kotlin.text.isEmpty
import kotlin.text.orEmpty
import kotlin.text.trim

object TurnstileStore {
    private const val EXPIRY_MS = 90_000L

    private data class TokenEntry(val token: String, val ts: Long)

    // Up to 10 pending tokens (FIFO). Readers will skip expired ones.
    private val tokenCh = Channel<TokenEntry>(capacity = 20)

    /** Add a token with timestamp. Drops oldest if buffer is full so latest wins. */
    fun setToken(token: String?) {
        val v = token?.trim().orEmpty()
        if (v.isEmpty()) return

        val entry = TokenEntry(v, System.currentTimeMillis())

        // Try once; if full, drop one oldest then try again.
        val first = tokenCh.trySend(entry)
        if (!first.isSuccess) {
            tokenCh.tryReceive().getOrNull() // drop oldest
            tokenCh.trySend(entry)
        }
    }

    /** Remove all buffered tokens. @return number of tokens cleared. */
    fun clear(): Int {
        var count = 0
        while (tokenCh.tryReceive().getOrNull() != null) count++
        return count
    }

    /**
     * Get a non-expired token.
     * If none is immediately available, calls [onStartWaiting] once and suspends until a fresh token arrives.
     */
    suspend fun readToken(onStartWaiting: (() -> Unit)? = null): String {
        // Fast path: try to pull a fresh token from buffer without suspending
        pollFresh()?.let { return it.token }

        // Not available right now -> notify caller that we're about to wait
        onStartWaiting?.invoke()

        // Suspend until we receive a fresh (non-expired) token
        while (true) {
            val e = tokenCh.receive()
            if (!isExpired(e)) return e.token
            // else continue waiting
        }
    }

    fun readTokenImmediate(): String? = pollFresh()?.token

    // --- Helpers ---

    /** Drain expired tokens from the head and return the first fresh one, or null if none. */
    private fun pollFresh(): TokenEntry? {
        val now = System.currentTimeMillis()
        while (true) {
            val e = tokenCh.tryReceive().getOrNull() ?: return null
            if (!isExpired(e, now)) return e
            // else drop and continue
        }
        // unreachable
    }

    private fun isExpired(e: TokenEntry, now: Long = System.currentTimeMillis()): Boolean {
        return (now - e.ts) > EXPIRY_MS
    }



//    // Keep at most one pending token; consumer waits if empty.
//    private val tokenCh = Channel<String>(capacity = 10)
//
//    fun setToken(token: String?) {
//        val v = token?.trim().orEmpty()
//        if (v.isEmpty()) return
////        // Drop any pending value so the latest wins.
////        tokenCh.tryReceive().getOrNull()
//        tokenCh.trySend(v)
//    }
//
//    /**
//     * Consumer: suspends until a token arrives.
//     * If no token is immediately available, calls [onStartWaiting] once, then waits.
//     * Cancels instantly if the caller's Job is cancelled.
//     */
//    suspend fun readToken(onStartWaiting: (() -> Unit)? = null): String {
//        // Fast path: grab immediately if already buffered
//        tokenCh.tryReceive().getOrNull()?.let { return it }
//
//        // Not available right now -> notify caller that we're about to wait
//        onStartWaiting?.invoke()
//
//        // Suspend here until a token arrives (cancellable)
//        return tokenCh.receive()
//    }
//
//
//    suspend fun readTokenImmediate(): String? {
//        return tokenCh.tryReceive().getOrNull()
//    }
//
//
//    /**
//     * Remove all currently buffered tokens.
//     * @return the number of tokens cleared.
//     */
//    fun clear(): Int {
//        var count = 0
//        while (true) {
//            val removed = tokenCh.tryReceive().getOrNull() ?: break
//            count++
//        }
//        return count
//    }
}