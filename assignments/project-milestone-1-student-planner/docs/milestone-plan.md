# Ten milestone development plan

## Milestone 1 Foundation and approval

Define the problem and scope, establish the repository, and compile and run the Java starter. Obtain instructor feedback before advanced development.

## Milestone 2 Requirements and baseline

Write acceptance scenarios and a linear scan for pending work. Plan fixed-seed datasets of 100, 1000, and 10000 records and record comparison counts and timings.

## Milestone 3 Domain and sequences

Introduce Course, Module, and Assignment classes with validated identifiers and fields. Store records in arrays or lists and test insertion, update, removal, and state invariants.

## Milestone 4 Queue workflow

Implement a FIFO study-session queue. Verify empty behavior, insertion order, and consistency when an assignment is completed or removed.

## Milestone 5 Search and sort

Compare linear search with binary search on a sorted ID snapshot. Compare insertion sort and merge sort on due dates; separate sorting cost from search cost.

## Milestone 6 Hash retrieval

Introduce an ID-to-assignment map and a set of unique IDs. Compare retrieval with the baseline and test that updates keep every view consistent.

## Milestone 7 Hierarchy

Represent courses and modules as a rooted tree with assignment references at the leaves. Traverse it to group work and verify parent-child relationships.

## Milestone 8 Priority selection

Use a heap or priority queue for eligible work ordered by priority, due date, and ID. Refresh entries after updates and verify deterministic ties.

## Milestone 9 Dependencies and graphs

Represent prerequisite edges with adjacency lists. Use DFS to detect cycles and traversal to report blockers or affected dependent tasks.

## Milestone 10 Integration and release

Integrate the console workflow, rerun tests and performance comparisons, review complexity, document limitations, and prepare a tagged release candidate.

Scheduling: follow the course milestone sequence and Canvas deadlines. Each stage includes its evidence, documentation update, and commit. Topic approval precedes advanced implementation.
