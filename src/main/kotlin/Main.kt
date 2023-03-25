package sample;

import sample.ds.heaps.heapProgram
import sample.sample.coroutines.introduction.coroutineIntroductionProgram

enum class ProgramType(vararg val aliases: String) {
    Coroutines_Introduction("CI"),
    Heaps("HP")
}

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val programType = args.getOrNull(0)
        val programVersion = args.getOrElse(1) { "1" }.toInt()
        val program = getProgram(resolveProgramType(programType), programVersion)
        program.execute()
    }

    private fun getProgram(programType: ProgramType, programVersion: Int) = when (programType) {
        ProgramType.Coroutines_Introduction -> coroutineIntroductionProgram(programVersion)
        ProgramType.Heaps -> heapProgram(programVersion)
        else -> throw IllegalArgumentException("Invalid Program Type")
    }

    private fun resolveProgramType(programType: String?): ProgramType =
        ProgramType.values().filter { it.name == programType || it.aliases.contains(programType) }.firstOrNull()
            ?: throw IllegalArgumentException("Invalid Program Type")
}