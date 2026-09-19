package edu.csc223.tasks

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ResizableTaskSequenceTest {
    private fun sequence(vararg values: String): ResizableTaskSequence<String> =
        ResizableTaskSequence<String>().also { result -> values.forEach { result.add(it) } }

    private fun assertState(sequence: TaskSequence<String>, vararg expected: String) {
        assertEquals(expected.size, sequence.size())
        assertEquals(expected.isEmpty(), sequence.isEmpty())
        assertEquals(expected.toList(), (0 until sequence.size()).map { sequence.get(it) })
        assertThrows(IndexOutOfBoundsException::class.java) { sequence.get(expected.size) }
    }

    // White-box inspection is confined to tests for the explicit storage requirements.
    private fun storage(sequence: ResizableTaskSequence<*>): Array<*> {
        val field = sequence.javaClass.getDeclaredField("elements")
        field.isAccessible = true
        return field.get(sequence) as Array<*>
    }

    @Test fun `both constructors create empty sequences`() {
        assertState(ResizableTaskSequence())
        assertState(ResizableTaskSequence(1))
    }
    @Test fun `nonpositive capacities are rejected`() {
        for (capacity in listOf(0, -1, Int.MIN_VALUE)) {
            assertThrows(IllegalArgumentException::class.java) { ResizableTaskSequence<String>(capacity) }
        }
    }
    @Test fun `append preserves insertion order including duplicates`() {
        val values = sequence()
        values.add("A")
        assertState(values, "A")
        values.add("B")
        values.add("A")
        assertState(values, "A", "B", "A")
    }
    @Test fun `null append leaves empty state unchanged`() {
        val values = sequence()
        assertThrows(IllegalArgumentException::class.java) { values.add(null) }
        assertState(values)
    }
    @Test fun `null append leaves full sequence and capacity unchanged`() {
        val values = ResizableTaskSequence<String>(1)
        values.add("A")
        val before = storage(values)
        assertThrows(IllegalArgumentException::class.java) { values.add(null) }
        assertState(values, "A")
        assertSame(before, storage(values))
    }
    @Test fun `first and last valid indices return their values`() {
        val values = sequence("A", "B", "C")
        assertEquals("A", values.get(0))
        assertEquals("C", values.get(2))
        assertState(values, "A", "B", "C")
    }
    @Test fun `get rejects indices just outside range and far above it`() {
        val values = sequence("A", "B")
        for (index in listOf(-1, 2, Int.MAX_VALUE)) {
            assertThrows(IndexOutOfBoundsException::class.java) { values.get(index) }
            assertState(values, "A", "B")
        }
    }
    @Test fun `remove rejects invalid indices without changing state`() {
        val values = sequence("A", "B")
        for (index in listOf(-1, 2, Int.MAX_VALUE)) {
            assertThrows(IndexOutOfBoundsException::class.java) { values.remove(index) }
            assertState(values, "A", "B")
        }
    }
    @Test fun `get and remove reject an empty sequence index`() {
        val values = sequence()
        assertThrows(IndexOutOfBoundsException::class.java) { values.get(0) }
        assertThrows(IndexOutOfBoundsException::class.java) { values.remove(0) }
        assertState(values)
    }
    @Test fun `remove first shifts remaining values in order`() {
        val values = sequence("A", "B", "C")
        assertEquals("A", values.remove(0))
        assertState(values, "B", "C")
    }
    @Test fun `remove middle preserves values on both sides`() {
        val values = sequence("A", "B", "C", "D")
        assertEquals("B", values.remove(1))
        assertState(values, "A", "C", "D")
    }
    @Test fun `remove last preserves preceding values`() {
        val values = sequence("A", "B", "C")
        assertEquals("C", values.remove(2))
        assertState(values, "A", "B")
    }
    @Test fun `removing only value returns to empty and supports reuse`() {
        val values = sequence("A")
        assertEquals("A", values.remove(0))
        assertState(values)
        values.add("B")
        assertState(values, "B")
    }
    @Test fun `removal clears unused storage`() {
        val values = sequence("A", "B", "C")
        values.remove(1)
        assertState(values, "A", "C")
        assertTrue(storage(values).drop(values.size()).all { it == null })
    }
    @Test fun `repeated resizing doubles capacity and preserves every reference`() {
        val values = ResizableTaskSequence<ProjectTask>(1)
        val expected = (1..65).map { ProjectTask("T$it", "Task $it", 1) }
        var expectedCapacity = 1
        expected.forEachIndexed { index, task ->
            if (index == expectedCapacity) expectedCapacity *= 2
            values.add(task)
            assertEquals(index + 1, values.size())
            assertFalse(values.isEmpty())
            assertEquals(expectedCapacity, storage(values).size)
            for (position in 0..index) assertSame(expected[position], values.get(position))
            assertTrue(storage(values).drop(values.size()).all { it == null })
        }
    }
    @Test fun `remove then append after growth preserves order`() {
        val values = ResizableTaskSequence<String>(2)
        listOf("A", "B", "C").forEach { values.add(it) }
        assertEquals("B", values.remove(1))
        assertState(values, "A", "C")
        values.add("D")
        values.add("E")
        values.add("F")
        assertState(values, "A", "C", "D", "E", "F")
    }
}
