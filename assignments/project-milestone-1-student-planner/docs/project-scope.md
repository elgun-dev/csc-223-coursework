# Proposed scope

The Student Assignment Planner is a single-user, local console application for organizing college assignments and prerequisite tasks. It uses fictional examples and requires no Canvas connection, student account, paid service, or confidential records.

## Six essential capabilities

1. Create, edit, list, and remove courses and assignments with validated identifiers, titles, dates, priorities, and status.
2. Find assignments by ID or title and filter by course or completion state; sort by due date, priority, or title with deterministic ID tie-breaking.
3. Build a FIFO study queue and record status transitions without silently losing queued work.
4. Browse a course and module hierarchy and summarize pending or completed work.
5. Suggest the next eligible assignment using priority, due date, and ID, while excluding completed or dependency-blocked assignments.
6. Track directed prerequisite relationships and traverse related tasks to explain blockers, rejecting self-dependencies and cycles.

## Optional enhancements

- CSV import and export with schema validation.
- Undo of the most recent status change.
- A simple text summary of workload by course.

## Boundaries

No production deployment, authentication, Canvas integration, network service, payments, real student data, collaborative editing, complex GUI, or machine learning. The required version uses in-memory records with repeatable sample data; persistence is optional. The application recommends an ordering and does not guarantee optimal scheduling.

## Final success criteria

- Valid records can be added, edited, found, and removed; invalid input leaves existing state unchanged.
- Equal-priority or equal-deadline records have deterministic order using unique assignment IDs.
- The study queue processes first-in-first-out without dropping or repeating entries.
- Course and module traversal returns each contained record once, and ID retrieval agrees with the sequence contents.
- The next-work suggestion excludes completed or blocked work; duplicate edges, missing endpoints, self-links, and cycles are handled consistently.
- Required unit and integration tests pass, search/sort measurements are reproducible, and a clean checkout can be built and demonstrated using the README.

These are acceptance goals for the completed semester project, not claims of current implementation.
