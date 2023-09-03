package sample;

import sample.ds.heaps.heapProgram
import sample.sample.coroutines.application.coroutineApplicationProgram
import sample.sample.coroutines.application.coroutineConstructorsProgram
import sample.sample.coroutines.introduction.coroutineIntroductionProgram

enum class ProgramType(vararg val aliases: String) {
    Coroutines_Introduction("CI"),
    Heaps("HP"),
    Coroutines_Application("CA"),
    Coroutines_Constructors("CC")
}

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val programType = args.getOrNull(0)
        val programVersion = args.getOrElse(1) { "1" }.toInt()
        var otherArgs = args.sliceArray(2 until args.size)
        val program = getProgram(resolveProgramType(programType), programVersion, *otherArgs)
        program.execute()
    }

    private fun getProgram(programType: ProgramType, programVersion: Int, vararg programArgs:String) = when (programType) {
        ProgramType.Coroutines_Introduction -> coroutineIntroductionProgram(programVersion)
        ProgramType.Heaps -> heapProgram(programVersion)
        ProgramType.Coroutines_Application-> coroutineApplicationProgram(programVersion, *programArgs)
        ProgramType.Coroutines_Constructors ->  coroutineConstructorsProgram(programVersion, *programArgs)
        else -> throw IllegalArgumentException("Invalid Program Type")
    }


    private fun resolveProgramType(programType: String?): ProgramType =
        ProgramType.values().filter { it.name == programType || it.aliases.contains(programType) }.firstOrNull()
            ?: throw IllegalArgumentException("Invalid Program Type")
}