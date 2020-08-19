package pl.crejk.tempbin.util

import com.github.benmanes.caffeine.cache.AsyncCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import kotlinx.coroutines.supervisorScope
import java.util.concurrent.CompletableFuture

class SuspendingCache<K : Any, V: Any>(
    private val asyncCache: AsyncCache<K, V>,
    private val loader: suspend (K) -> V?
) {

    suspend fun get(key: K): V? = supervisorScope {
        this.getAsync(key).await()
    }

    fun invalidate(key: K) =
        this.asyncCache.synchronous().invalidate(key)

    private fun CoroutineScope.getAsync(key: K): CompletableFuture<V?> =
        asyncCache.get(key) { k, executor ->
            future(executor.asCoroutineDispatcher()) {
                loader(k)
            }
        }
}
