package sample.sample.coroutines.introduction

import kotlinx.coroutines.*
import sample.IProgram
import sample.IProgramFactory
import java.util.*

class CoroutineIntroductionProgramFactory : IProgramFactory {
    override fun getInstance(version: Int): IProgram {
        return when (version) {
            1 -> CoroutineLauncher()
            2 -> CoroutineAsyncer()
            else -> throw IllegalArgumentException("Invalid version")
        }
    }
}

/* Sample Program which uses the launch coroutine builder
* 1.)Launches 2 co-routines
* 2.)Executes the command in each co-routine
* */
class CoroutineLauncher : IProgram {
    override fun execute() {
        repeat(2) {
            coroutineLauncher(it)
        }
    }

    private fun coroutineLauncher(it: Int) {
        val job = GlobalScope.launch(coroutineName("$it #Launched#")) {
            command(clientId())
        }
        runBlocking {
            job.join()
        }
    }

}

/* Sample Program which uses the async coroutine builder
* 1.)Create 2 async co-routines
* 2.)Executes the command in each co-routine in parallel
* */
class CoroutineAsyncer : IProgram {
    override fun execute() {
        //Entered co-routine land
        val job = GlobalScope.launch {
            asyncProgram()
        }
        runBlocking { job.join() }
    }

    private suspend fun asyncProgram() {
        val executors = mutableListOf<Deferred<Unit>>()
        repeat(2) {
            executors.add(coroutineAsyncerAsync(it))
        }
        executors.awaitAll()
    }

    private fun coroutineAsyncerAsync(it: Int): Deferred<Unit> {
        return GlobalScope.async(coroutineName("$it #Asynced#")) {
            when (it) {
                0 -> delay(4000)
                1 -> delay(1000)
            }
            command(clientId())
        }
    }
}

internal fun CoroutineScope.clientId() = "${coroutineContext[CoroutineName]?.name} - ${UUID.randomUUID().toString()}"

internal fun coroutineName(sequence: String) = CoroutineName("Co-Routine $sequence")

internal fun command(clientId: String) {
    println("Command executed for $clientId")
}
