package edu.csc223.tasks

/** Immutable task. Identity and natural ordering both use the exact task ID. */
class ProjectTask(taskId: String?, title: String?, val priority: Int) : Comparable<ProjectTask> {
    val taskId: String = requireNotNull(taskId) { "Task ID must not be null." }
    val title: String = requireNotNull(title) { "Title must not be null." }

    init {
        require(this.taskId.isNotBlank()) { "Task ID must not be blank." }
        require(this.title.isNotBlank()) { "Title must not be blank." }
        require(priority in 1..5) { "Priority must be between 1 and 5." }
    }

    override fun equals(other: Any?): Boolean = other is ProjectTask && taskId == other.taskId
    override fun hashCode(): Int = taskId.hashCode()
    override fun compareTo(other: ProjectTask): Int = taskId.compareTo(other.taskId)
    override fun toString(): String = "ProjectTask(taskId=$taskId, title=$title, priority=$priority)"
}
