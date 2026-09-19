package edu.csc223.tasks

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ProjectTaskPriorityComparatorTest {
    private val comparator = ProjectTaskPriorityComparator()
    @Test fun `higher priority sorts first even when ID is later`() {
        val low = ProjectTask("A", "Low", 1)
        val high = ProjectTask("Z", "High", 5)
        assertTrue(comparator.compare(high, low) < 0)
        assertTrue(comparator.compare(low, high) > 0)
        assertEquals(listOf(high, low), listOf(low, high).sortedWith(comparator))
    }
    @Test fun `priority ties use ascending ID`() {
        val a = ProjectTask("A", "Z", 3)
        val b = ProjectTask("B", "A", 3)
        assertTrue(comparator.compare(a, b) < 0)
        assertTrue(comparator.compare(b, a) > 0)
        assertEquals(0, comparator.compare(a, ProjectTask("A", "Different", 3)))
    }
    @Test fun `mixed priorities produce deterministic complete ordering`() {
        val tasks = listOf(ProjectTask("D", "D", 1), ProjectTask("C", "C", 5),
            ProjectTask("B", "B", 3), ProjectTask("A", "A", 5))
        assertEquals(listOf("A", "C", "B", "D"), tasks.sortedWith(comparator).map { it.taskId })
    }
}
