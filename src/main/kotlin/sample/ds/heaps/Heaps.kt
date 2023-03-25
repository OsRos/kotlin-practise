package sample.ds.heaps

import sample.IProgram
import sample.IProgramFactory
import kotlin.math.floor

/*
* 1.)Implementation of heap using array (think about it)
* 2.)Add elements to heap i.e keep the  heap in order & shape
* 2.1)Add element to the end, then swap  to maintain the heap order
* 3.)Remove elements from heap i.e keep the  heap in order & shape
* */

/*
* 1
* 2 1
* 3 1 2
* 4 3 2 1 -> 4 3 2 1 5 -> 4 5 2 1 3 -> 5 4 2 1 3
* 3 4 2 1 -> 4 3 2 1
* */
class HeapProgramFactory : IProgramFactory {
    override fun getInstance(version: Int): IProgram {
        return when (version) {
            1 -> MaxHeapProgram()
            else -> throw IllegalArgumentException("Invalid version")
        }
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
        heapArray=heapArray.sliceArray(0..size-1)
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
            swap(index,maxChildIndex)
            shiftDown(maxChildIndex)
        }
    }

    private fun getMaxChildIndex(index: Int): Int? {
        val child1= getElement(firstChildIndex(index))
        val child2=getElement(firstChildIndex(index) +1)
        if (child1==null)return null //if no children
        if (child2==null && heapArray[index]<heapArray[firstChildIndex(index)]) return firstChildIndex(index) //if only 1 child & it is greater
        if ((child1 > child2!!) && heapArray[index]<heapArray[firstChildIndex(index)]) return firstChildIndex(index) //if 1st  child is greater
        if ((child2 > child1) && heapArray[index]<heapArray[firstChildIndex(index) +1]) return firstChildIndex(index) +1 //if 2nd child is greater
        return null
    }

    private fun firstChildIndex(index: Int) = 2 * index+1

    private fun getElement(index: Int): Int? {
        return if (index < size) {
            heapArray[index]
        } else {
            null
        }
    }

    override fun toString(): String {
        return heapArray.contentToString()
    }
}

