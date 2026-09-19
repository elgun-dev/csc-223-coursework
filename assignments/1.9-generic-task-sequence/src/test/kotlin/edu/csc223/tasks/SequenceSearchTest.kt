package edu.csc223.tasks

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SequenceSearchTest {
    private fun searchAndVerify(tasks: List<ProjectTask>, target: ProjectTask?, expected: Int) {
        val values = ResizableTaskSequence<ProjectTask>(1)
        tasks.forEach { values.add(it) }
        assertEquals(expected, SequenceSearch.findFirst(values, target))
        assertEquals(tasks.size, values.size())
        assertEquals(tasks.isEmpty(), values.isEmpty())
        tasks.forEachIndexed { index, task -> assertSame(task, values.get(index)) }
    }
    @Test fun `found target uses logical equality not object identity`() {
        searchAndVerify(listOf(ProjectTask("A", "First", 1), ProjectTask("B", "Second", 2)),
            ProjectTask("B", "Different", 5), 1)
    }
    @Test fun `missing target returns minus one`() {
        searchAndVerify(listOf(ProjectTask("A", "First", 1)), ProjectTask("B", "Missing", 1), -1)
    }
    @Test fun `duplicate target returns first match`() {
        searchAndVerify(listOf(ProjectTask("X", "Other", 1), ProjectTask("A", "First", 2),
            ProjectTask("A", "Second", 3)), ProjectTask("A", "Target", 5), 1)
    }
    @Test fun `first element is searchable`() {
        searchAndVerify(listOf(ProjectTask("A", "First", 1)), ProjectTask("A", "Target", 2), 0)
    }
    @Test fun `empty sequence returns minus one`() {
        searchAndVerify(emptyList(), ProjectTask("A", "Target", 1), -1)
    }
    @Test fun `null sequence is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { SequenceSearch.findFirst<String>(null, "A") }
    }
    @Test fun `null target is absent`() {
        searchAndVerify(listOf(ProjectTask("A", "First", 1)), null, -1)
    }
    @Test fun `search is generic over values other than tasks`() {
        val values = ResizableTaskSequence<Int>()
        listOf(8, 13, 21).forEach { values.add(it) }
        assertEquals(1, SequenceSearch.findFirst(values, 13))
        assertEquals(listOf(8, 13, 21), (0 until values.size()).map { values.get(it) })
        assertEquals(3, values.size())
        assertFalse(values.isEmpty())
    }
}
