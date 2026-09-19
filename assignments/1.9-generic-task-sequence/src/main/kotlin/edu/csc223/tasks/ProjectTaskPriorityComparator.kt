package edu.csc223.tasks

/** Higher priority first, with ascending task ID breaking priority ties. */
class ProjectTaskPriorityComparator : Comparator<ProjectTask> {
    override fun compare(first: ProjectTask, second: ProjectTask): Int {
        val priorityOrder = second.priority.compareTo(first.priority)
        return if (priorityOrder != 0) priorityOrder else first.taskId.compareTo(second.taskId)
    }
}
