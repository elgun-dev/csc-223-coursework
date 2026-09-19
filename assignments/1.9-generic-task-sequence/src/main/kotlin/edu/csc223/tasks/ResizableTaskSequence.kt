package edu.csc223.tasks

/**
 * Array-backed sequence: 0 <= count <= capacity; occupied slots are non-null,
 * unused slots are null. Capacity is positive and doubles on growth.
 */
class ResizableTaskSequence<T : Any>(initialCapacity: Int = 10) : TaskSequence<T> {
    private var elements: Array<Any?>
    private var count = 0

    init {
        require(initialCapacity > 0) { "Initial capacity must be positive." }
        elements = arrayOfNulls(initialCapacity)
    }

    override fun add(element: T?) {
        val value = requireNotNull(element) { "Null elements are not allowed." }
        if (count == elements.size) {
            check(elements.size <= Int.MAX_VALUE / 2) { "Cannot double capacity further." }
            elements = elements.copyOf(elements.size * 2)
        }
        elements[count] = value
        count++
    }

    override fun get(index: Int): T {
        validateIndex(index)
        // Only values of T enter occupied slots; validation excludes unused slots.
        @Suppress("UNCHECKED_CAST")
        return elements[index] as T
    }

    override fun remove(index: Int): T {
        val removed = get(index) // Validate before any mutation.
        for (position in index until count - 1) {
            elements[position] = elements[position + 1]
        }
        count--
        elements[count] = null
        return removed
    }

    override fun size(): Int = count

    private fun validateIndex(index: Int) {
        if (index < 0 || index >= count) {
            throw IndexOutOfBoundsException("Index $index outside sequence of size $count.")
        }
    }
}
