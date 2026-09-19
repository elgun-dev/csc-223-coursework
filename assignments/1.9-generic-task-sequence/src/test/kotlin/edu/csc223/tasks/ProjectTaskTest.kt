package edu.csc223.tasks

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ProjectTaskTest {
    @Test fun `valid task exposes immutable values and readable text`() {
        val task = ProjectTask("T1", "Plan", 3)
        assertEquals("T1", task.taskId)
        assertEquals("Plan", task.title)
        assertEquals(3, task.priority)
        assertTrue(task.toString().contains("T1"))
        assertTrue(task.toString().contains("Plan"))
        assertTrue(task.toString().contains("3"))
    }
    @Test fun `null empty and whitespace IDs are rejected`() {
        for (id in listOf(null, "", " \t\n")) {
            assertThrows(IllegalArgumentException::class.java) { ProjectTask(id, "Plan", 3) }
        }
    }
    @Test fun `null empty and whitespace titles are rejected`() {
        for (title in listOf(null, "", " \t\n")) {
            assertThrows(IllegalArgumentException::class.java) { ProjectTask("T1", title, 3) }
        }
    }
    @Test fun `priorities outside both boundaries are rejected`() {
        for (priority in listOf(Int.MIN_VALUE, 0, 6, Int.MAX_VALUE)) {
            assertThrows(IllegalArgumentException::class.java) { ProjectTask("T1", "Plan", priority) }
        }
    }
    @Test fun `priority boundaries are accepted`() {
        assertEquals(1, ProjectTask("T1", "Plan", 1).priority)
        assertEquals(5, ProjectTask("T2", "Build", 5).priority)
    }
    @Test fun `equality and hash codes depend on ID alone`() {
        val first = ProjectTask("T1", "Plan", 1)
        val second = ProjectTask("T1", "Changed title", 5)
        val third = ProjectTask("T1", "Third", 2)
        assertEquals(first, first)
        assertEquals(first, second)
        assertEquals(second, first)
        assertEquals(second, third)
        assertEquals(first, third)
        assertEquals(first.hashCode(), second.hashCode())
        assertNotEquals(first, ProjectTask("T2", "Plan", 1))
        assertNotEquals(first, null)
        assertFalse(first.equals("T1"))
    }
    @Test fun `natural order is lexicographic ID order consistent with equality`() {
        val first = ProjectTask("A", "Z title", 5)
        val second = ProjectTask("B", "A title", 1)
        assertTrue(first < second)
        assertTrue(second > first)
        assertEquals(0, first.compareTo(ProjectTask("A", "Other", 1)))
        assertEquals(listOf("T1", "T10", "T2"), listOf("T2", "T10", "T1")
            .map { ProjectTask(it, "Title", 1) }.sorted().map { it.taskId })
    }
}
