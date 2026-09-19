package edu.csc223.tasks

/** An insertion-ordered sequence of non-null values. Rejected operations do not mutate it. */
interface TaskSequence<T : Any> {
    /** Append a value; null throws IllegalArgumentException. */
    fun add(element: T?)
    /** Return the value at index, or throw IndexOutOfBoundsException outside 0 until size(). */
    fun get(index: Int): T
    /** Remove and return a value, preserving remaining order; same bounds rule as get. */
    fun remove(index: Int): T
    fun size(): Int
    fun isEmpty(): Boolean = size() == 0
}
