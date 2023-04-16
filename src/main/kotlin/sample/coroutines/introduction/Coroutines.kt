package sample.sample.coroutines.introduction

import kotlinx.coroutines.*
import sample.IProgram
import java.util.*

fun coroutineIntroductionProgram(version: Int): IProgram {
    return when (version) {
        1 -> CoroutineLauncher()
        2 -> CoroutineAsyncer()
        3 -> CoroutineAsyncerWithExceptions()
        else -> throw IllegalArgumentException("Invalid version")
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

class CoroutineAsyncerWithExceptions : IProgram {

    /*
    * Program to demonstrate the use of coroutine scope
    * 1.)Demonstrate how coroutine scope can clean up/manage the co-routines
    * 2.)Throw exceptions from within coroutines & cancel using scope
    * 3.)Also show an example without use of scope
    * */
    override fun execute() {
        runBlocking {
            try {
                repeat(5) {
                    try {
                        coroutineAsyncer(it)
                    } catch (e: Exception) {
                        println("Exception caught in execute repeat ${e.stackTrace}")
                        throw e
                    }
                }
            } catch (e: Exception) {
                println("Exception caught in execute ${e.stackTrace}")
            }
        }
    }

    private suspend fun coroutineAsyncer(it: Int) = GlobalScope.async(coroutineName("$it #Launched#")) {
        try {
            failingCommand(clientId())
        } catch (e: Exception) {
            println("Exception caught in coroutineLauncher $it")
            throw e
        }
    }.await()

    /*
    Command which fails randomly
    * */
    private suspend fun failingCommand(clientId: String) {
        val random = Random()
        val randomInt = random.nextInt(10)
        delay(randomInt.toLong() * 1000)
        if (randomInt % 2 == 0) {
            throw RuntimeException("Random Exception for $clientId after $randomInt seconds")
        } else {
            println("Command executed for $clientId after $randomInt seconds")
        }
    }

}


internal fun CoroutineScope.clientId() = "${coroutineContext[CoroutineName]?.name} - ${UUID.randomUUID().toString()}"

internal fun coroutineName(sequence: String) = CoroutineName("Co-Routine $sequence")

internal fun command(clientId: String) {
    println("Command executed for $clientId")
}
