package org.thelazybattley.macrotrack

import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

/**
 * Runs [block] to completion right away. For tests whose fakes never really suspend, so they don't need a
 * coroutine test library; fails if [block] does suspend.
 */
fun runSync(block: suspend () -> Unit) {
    var result: Result<Unit>? = null
    block.startCoroutine(completion = Continuation(context = EmptyCoroutineContext) { result = it })
    (result ?: error("The block suspended; runSync only runs blocks that finish right away.")).getOrThrow()
}
