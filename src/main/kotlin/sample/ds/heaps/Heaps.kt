package sample.ds.heaps

import sample.IProgram
import kotlin.math.floor

/*
* 1.)Implementation of heap using array (think about it)
* 2.)Add elements to heap i.e keep the  heap in order & shape
* 2.1)Add element to the end, then swap  to maintain the heap order
* 3.)Remove elements from heap i.e keep the  heap in order & shape
* */

//            8
//         7      5
//       6   2   1  4
//     0  3
fun heapProgram(version: Int): IProgram {
    return when (version) {
        1 -> MaxHeapProgram()
        else -> throw IllegalArgumentException("Invalid version")
    }
}

class MaxHeapProgram : IProgram {
    override fun execute() {
        val heap = MaxHeap(10)
        heap.add(0)
        heap.add(1)
        heap.add(2)
        heap.add(3)
        heap.add(4)
        heap.add(5)
        heap.add(6)
        heap.add(7)
        heap.add(8)
        heap.add(9)
        println(heap)
        heap.pop()
        println(heap)
    }
}

interface Heap {
    fun add(number: Int)
    fun pop(): Int
}

class MaxHeap(private val maxSize: Int = 100) : Heap {
    private var heapArray = IntArray(maxSize)
    private var size = 0
    override fun add(number: Int) {
        //Add the element to the end of  the array
        heapArray[size] = number
        shiftUp(size)
        size++
    }

    //Compare the element at @index with its parent & shift up recursively
    private fun shiftUp(index: Int) {
        if (index == 0) return
        //Compare element with parent
        if (heapArray[index] < heapArray[parentIndex(index)]) {
            return
        } else {
            //swap the elements
            swap(index, parentIndex(index))
            //recursively call shiftUp
            shiftUp(parentIndex(index))
        }
    }

    private fun swap(index: Int, otherIndex: Int) {
        val tmp = heapArray[otherIndex]
        heapArray[otherIndex] = heapArray[index]
        heapArray[index] = tmp
    }

    private fun parentIndex(index: Int): Int {
        return floor(((index - 1) / 2).toDouble()).toInt()
    }

    override fun pop(): Int {
        /*
        * 1.)access the  1st element of the array
        * 2.)replace the 1st element with last element & reduce the heap size
        * 3.)shiftDown
        * */
        val max = heapArray[0]
        heapArray[0] = heapArray[size - 1]
        size--
        heapArray = heapArray.sliceArray(0 until size)
        shiftDown(0)
        return max
    }

    /*
    * Compare the element@index with its children
    * If a child is greater than itself, shiftDown recursively, else halt
    * */
    private fun shiftDown(index: Int) {
        val maxChildIndex = getMaxChildIndex(index) ?: return
        //Get the index of the max child
        if (heapArray[index] < heapArray[maxChildIndex]) {
            swap(index, maxChildIndex)
            shiftDown(maxChildIndex)
        }
    }

    private fun getMaxChildIndex(parentIndex: Int): Int? {
        val child1 = heapArray.getOrNull(firstChildIndex(parentIndex))
        val child2 = heapArray.getOrNull(secondChildIndex(parentIndex))
        if (child1 == null) return null //if no children
        if (child2 == null && isFirstChildGreater(parentIndex)) return firstChildIndex(parentIndex) //if only 1 child & it is greater
        if ((child1 >= child2!!) && isFirstChildGreater(parentIndex)) return firstChildIndex(parentIndex) //if 1st  child is greater
        if ((child2 > child1) && isSecondChildGreater(parentIndex)) return secondChildIndex(parentIndex) //if 2nd child is greater
        return null
    }

    private fun isSecondChildGreater(parentIndex: Int) =
        heapArray[parentIndex] < heapArray[secondChildIndex(parentIndex)]

    private fun secondChildIndex(parentIndex: Int) = firstChildIndex(parentIndex) + 1

    private fun isFirstChildGreater(parentIndex: Int) = heapArray[parentIndex] < heapArray[firstChildIndex(parentIndex)]

    private fun firstChildIndex(parentIndex: Int) = 2 * parentIndex + 1

    override fun toString(): String {
        return heapArray.contentToString()
    }
}

