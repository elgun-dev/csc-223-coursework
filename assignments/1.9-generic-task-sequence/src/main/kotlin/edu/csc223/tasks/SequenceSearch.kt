package edu.csc223.tasks

object SequenceSearch {
    /** Read-only linear search using logical equality; null targets cannot match stored values. */
    @JvmStatic
    fun <T : Any> findFirst(sequence: TaskSequence<T>?, target: T?): Int {
        val values = requireNotNull(sequence) { "Sequence must not be null." }
        for (index in 0 until values.size()) {
            if (values.get(index) == target) return index
        }
        return -1
    }
}
