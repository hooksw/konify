package io.github.hooksw.konify.runtime.diff

import androidx.collection.mutableObjectIntMapOf

class DiffUtils(
    private val diffOperation: DiffOperation
) {

    fun <T> perform(oldList: List<T>, newList: List<T>) {
        val oLen = oldList.size
        val nLen = newList.size

        var oldStart = 0
        var newStart = 0
        var oldEnd = oLen
        var newEnd = nLen
        while (oldStart < oldEnd && newStart < newEnd && oldList[oldStart] == newList[newStart]) {
            oldStart++;
            newStart++;
            continue
        }
        while (oldStart < oldEnd && newStart < newEnd && oldList[oldEnd - 1] == newList[newEnd - 1]) {
            oldEnd--;
            newEnd--;
        }

        if (oldStart == oldEnd) {
            while (newStart < newEnd) {
                diffOperation.insert(newStart, newStart)
                newStart++
            }
            return
        } else if (newStart == newEnd) {
            var removedIndex = oldStart
            while (removedIndex < oldEnd) {
                diffOperation.remove(oldStart)
                removedIndex++
            }
            return
        } else {
            performComplex(newStart, newEnd - 1, oldStart, oldEnd - 1, newList, oldList)
        }

    }

    private fun <T> performComplex(
        newStart: Int,
        newEnd: Int,
        oldStart: Int,
        oldEnd: Int,
        newList: List<T>,
        oldList: List<T>,
    ) {
        val newNarrowLen = newEnd - newStart + 1
        val newMap = mutableObjectIntMapOf<T>()
        for (i in newStart..newEnd) {
            newMap[newList[i]] = i
        }
        val sources = IntArray(newNarrowLen) { -1 }
        //新节点列表中已经遍历过的索引最大值
        var pos = 0
        var moved = false
        var removedAhead = 0

        for (oldIndex in oldStart..oldEnd) {
            val item = oldList[oldIndex]
            val newIndex = newMap.getOrDefault(item, -1)
            if (newIndex < 0) {
                diffOperation.remove(oldIndex - removedAhead)
                removedAhead++
            } else {
                sources[newIndex - newStart] = oldIndex - removedAhead
                if (pos > newIndex) {
                    moved = true
                } else {
                    pos = newIndex
                }
            }
        }
        if (moved) {
            val seq = findLIS(sources)
            var seqPos = 0
            for (newIndex in newStart..newEnd) {
                val sourcesIndex = newIndex - newStart
                val oldPos = sources[sourcesIndex]
                if (oldPos == -1) {
                    val insertIndex = if (newIndex == newStart) newIndex else sources[sourcesIndex - 1] + 1
                    diffOperation.insert(insertIndex, newIndex)
                    for (si in sources.indices) {
                        val preOldPos = sources[si]
                        if (preOldPos >= insertIndex) {
                            sources[si] = preOldPos + 1
                        }
                    }
                    sources[sourcesIndex] = insertIndex
                } else if (seqPos >= seq.size || (newIndex-newStart) != seq[seqPos]) {
                    var moveIndex = if (newIndex == newStart) newIndex else sources[sourcesIndex - 1] + 1
                    if (moveIndex > oldPos) {
                        moveIndex--
                    }
                    diffOperation.move(oldPos, moveIndex)
                    for (si in sources.indices) {
                        val preOldPos = sources[si]
                        if (oldPos > moveIndex && preOldPos >= moveIndex && preOldPos < oldPos) {
                            sources[si] = preOldPos + 1
                        } else if (oldPos < moveIndex && preOldPos <= moveIndex && preOldPos > oldPos) {
                            sources[si] = preOldPos - 1
                        }
                    }
                    sources[sourcesIndex] = moveIndex
                } else {
                    seqPos++;
                }
            }

        } else {
            for (i in newStart..newEnd) {
                val sourcesIndex = i - newStart
                if (sources[sourcesIndex] == -1) {
                    diffOperation.insert(i, i)
                }
            }
        }
    }


}

private fun findLIS(nums: IntArray): IntArray {
    if (nums.isEmpty()) return intArrayOf()

    val dp = IntArray(nums.size) { 1 }
    val prevIndices = IntArray(nums.size) { -1 }
    var maxLength = 1
    var endIndex = 0

    for (i in 1 until nums.size) {
        for (j in 0 until i) {
            if (nums[i] > nums[j] && dp[i] < dp[j] + 1) {
                dp[i] = dp[j] + 1
                prevIndices[i] = j
                if (dp[i] > maxLength) {
                    maxLength = dp[i]
                    endIndex = i
                }
            }
        }
    }

    val lisIndices = IntArray(maxLength)
    var currentIndex = endIndex
    for (i in maxLength - 1 downTo 0) {
        lisIndices[i] = currentIndex
        currentIndex = prevIndices[currentIndex]
    }

    return lisIndices
}

