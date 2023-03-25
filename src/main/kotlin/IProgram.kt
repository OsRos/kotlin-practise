package sample

interface IProgram {
    fun execute()
}

interface  IProgramFactory {
    fun getInstance(version: Int): IProgram
}