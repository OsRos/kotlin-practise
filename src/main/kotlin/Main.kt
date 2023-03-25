package sample;

import sample.ds.heaps.HeapProgramFactory
import sample.sample.coroutines.introduction.CoroutineIntroductionProgramFactory

fun main(args: Array<String>) {
    //  val program = CoroutineIntroductionProgramFactory().getInstance(2)
    val program = HeapProgramFactory().getInstance(1)
    program.execute()
}
//            8
//         7      5
//       6   2   1  4
//     0  3