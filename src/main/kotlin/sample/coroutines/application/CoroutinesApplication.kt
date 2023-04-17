package sample.sample.coroutines.application

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import sample.IProgram
import sample.sample.coroutines.introduction.clientId
import sample.sample.coroutines.introduction.coroutineName
import kotlin.system.measureTimeMillis


fun coroutineApplicationProgram(version: Int, vararg args: String): IProgram = when (version) {
    1 -> CoroutineScoper(
        CoroutineScoper.CoroutineBuilderArg.valueOf(args.getOrElse(0) { "launch" }.uppercase()),
        CoroutineScoper.ScopeArg.valueOf(args.getOrElse(1) { "scoped" }.uppercase())
    )

    2 -> CoroutineChanneller()
    3 -> CoroutinePooler(args.getOrElse(0) { "2" }.toInt() )
    else -> throw IllegalArgumentException("Invalid version")
}

fun CoroutineScope.produceFibonacciSequence(first: Int, second: Int, maxSequence: Int = 10): ReceiveChannel<Int> =
    produce {
        send(first)
        send(second)
        var first = first
        var second = second
        var sequence = 3
        while (sequence < maxSequence) {
            val next = first + second
            send(next)
            sequence++
            first = second
            second = next
        }
        send(first + second)
    }

class CoroutinePooler(val workers:Int) : IProgram  {
    /*
    * 1.)Demonstrate the use of coroutine pool
    * 2.)Demonstrate the use of send & receive channels
    * 3.)Program will try to empty the queue using 5 co-routines
    * 4.)Each coroutine will add a delay to simulate long running task
    * 5.)Will also pool  the results & return the final result
    * */
    override fun execute() {
        println("Running program with workers=$workers")
        runBlocking {
            val fibonacciSequence = produceFibonacciSequence(0, 1)
            val partialSums = delayedWork(fibonacciSequence)
            println(totalSum(partialSums))
        }
    }

    suspend fun CoroutineScope.delayedWork(numbers: ReceiveChannel<Int>) = produce<Int> {
        repeat(workers) {
            launch {
                println(
                    "Time taken :${
                        measureTimeMillis {
                            var sum = 0
                            numbers.consumeEach  {
                                delay(1 * 1000L)
                                sum += it
                            }
                            send(sum)
                        }
                    }"
                )
            }
        }
    }

    suspend fun CoroutineScope.totalSum(numbers: ReceiveChannel<Int>): Int {
        var sum = 0
        numbers.consumeEach {
            sum += it
        }
        return sum
    }


}

class CoroutineChanneller : IProgram {

    /*
    * 1.)Demonstrate co-ordination between co-routines using channels
    * 2.)fibonacci program using channels
    * 2.1)Producing coroutine will generate fibonacci numbers
    * 2.2)Queueing coroutine will consume the numbers from the producer and push it into a queue
    * */

    override fun execute() {
        runBlocking {
            val fibonacciSequence = produceFibonacciSequence(0, 1)
            printFibonacciSequenceParallely(fibonacciSequence)
        }
    }


    suspend fun ProducerScope<Int>.timedSend(value: Int) {
//        println("Sending $value at ${System.nanoTime()}")
        send(value)
    }

    suspend fun CoroutineScope.printFibonacciSequence(numbers: ReceiveChannel<Int>) = numbers.consumeEach {
        print("$it,")
    }

    //TODO :  The fibonacci sequence is not printed in the same order, if you use multiple consumer threads and remove  the delay. Why?
    //TODO :  How to create a consumer which prints elements in the same order in which elements are sent.
    suspend fun CoroutineScope.printFibonacciSequenceParallely(numbers: ReceiveChannel<Int>) =
        launch {
            numbers.consumeEach {
                print("$it,")
            }
        }

    /*
        fun generateFibonacciSequenceRecursively(first: Int, second: Int): Int {
            if (sequence == maxSequence) {
                return second
            }
            print("$second,")
            sequence++
            return generateFibonacciSequenceRecursively(second, first + second)
        }
    */

}

//TODO : How to catch a cancellation exception
//TODO : Why is the behaivour different during retries i.e delay+1 behaivour is getting exhibited in retries (for async not_scoped)
class CoroutineScoper(private val coroutineBuilder: CoroutineBuilderArg, private val scope: ScopeArg) : IProgram {
    private val RUNS = 6
    private val MAX_RETRIES = 1
    private val FAIL_AFTER_SECONDS = 1

    enum class CoroutineBuilderArg {
        LAUNCH, ASYNC;
    }

    enum class ScopeArg {
        SCOPED, NOT_SCOPED;
    }

    /*
    * Program to demonstrate the use of coroutine scope
    * 1.)Demonstrate how coroutine scope can clean up/manage the co-routines
    * 2.)Throw exceptions from within coroutines & cancel using scope
    * 3.)Also show an example without use of scope
    * */
    var retries = 0
    override fun execute() {
        println("Running program with coroutineBuilder=$coroutineBuilder and scope=$scope")
        when (coroutineBuilder) {
            CoroutineBuilderArg.ASYNC -> asyncProgram()
            CoroutineBuilderArg.LAUNCH -> launchProgram()
        }

    }

    //Run failing program in launch parallel without retry logic
    private fun launchProgram() {
        runBlocking {
            val jobs = mutableListOf<Job>()
            repeat(RUNS) {
                when (scope) {
                    ScopeArg.SCOPED -> jobs.add(executeInternalLaunchScoped(it))
                    ScopeArg.NOT_SCOPED -> jobs.add(executeInternalLaunch(it))
                }
            }
            jobs.joinAll()
        }
    }

    //Run failing program in async parallel with retry logic
    private fun asyncProgram() {
        runBlocking {
            try {
                val tasks = mutableListOf<Deferred<Unit>>()
                repeat(RUNS) {
                    when (scope) {
                        ScopeArg.SCOPED -> tasks.add(executeInternalAsyncScoped(it))
                        ScopeArg.NOT_SCOPED -> tasks.add(executeInternalAsync(it))
                        else -> throw RuntimeException("Invalid coroutine scope. Please provide either of ${ScopeArg.values()}")
                    }
                }
                tasks.awaitAll()
            } catch (e: Exception) {
                println("Exception caught in execute ${e.stackTrace.contentToString()}")
                if (retries < MAX_RETRIES) {
                    println("Retrying ...")
                    retries++
                    execute()
                } else {
                    println("Max retries reached")
                }
            }
        }
    }


    //Launch without scope
    private fun executeInternalLaunch(i: Int) = GlobalScope.launch(coroutineName("$i #Launched#")) {
        failingCommand(i, clientId())
    }


    private fun CoroutineScope.executeInternalLaunchScoped(i: Int) = launch(coroutineName("$i #Launched#")) {
        failingCommand(i, clientId())
    }

    //Async without scope
    private fun executeInternalAsync(i: Int) = GlobalScope.async(coroutineName("$i #Asynced#")) {
        failingCommand(i, clientId())
    }

    private fun CoroutineScope.executeInternalAsyncScoped(i: Int) = async(coroutineName("$i #Asynced#")) {
        failingCommand(i, clientId())
    }

    //
    /*
    Command which fails randomly
    * */
    private suspend fun failingCommand(i: Int, clientId: String) {
        println("### Command executing for $clientId")

        if (i == RUNS - 1) {
            delay(FAIL_AFTER_SECONDS.toLong() * 1000)
            throw RuntimeException("Random Exception for $clientId after $i seconds")
        } else {
            delay(i.toLong() * 1000)
            println("XXX Command executed for $clientId after $i seconds")
        }
    }

}


