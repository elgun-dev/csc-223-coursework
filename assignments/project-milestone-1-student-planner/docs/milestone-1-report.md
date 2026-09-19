# Student Assignment Planner

Project Milestone 1

Elgun S  |  CSC 223  |  September 19 2026

## Project proposal

I propose a local console application that helps college students organize assignments across courses, see upcoming work, and identify prerequisite tasks. The project will grow through the ten course milestones, beginning with the repository, a documented plan, and a basic Java entry point. Topic and scope approval will precede advanced implementation.

### Problem and intended users

Students often track deadlines, task priorities, and prerequisite work in separate lists. A flat list can show that a task is due without explaining which earlier task must be completed first. The planner will bring those records together for one student and make the order of work visible.

The domain is education and personal task organization. Intended users are college students managing several courses; the instructor is a reviewer of the software and its technical evidence. Users need to record work, update completion status, find a particular assignment, compare deadlines, and choose an eligible next task.

### Project value

The planner will combine searchable records with explicit workflow and dependency information. Its value is a clear, reproducible view of pending work rather than an automated guarantee of the best schedule. A console interface and fictional sample data keep the project focused on data structures, algorithms, and correctness.

### Primary data and relationships

| Entity | Proposed attributes and relationships |
| --- | --- |
| Course | Unique course ID and name; contains modules. |
| Module | Unique module ID, title, course ID, and sequence number; groups assignments. |
| Assignment | Unique ID, title, course/module IDs, due date, priority 1 to 5, and status such as pending, active, or complete. |
| Dependency | Directed prerequisite ID to dependent ID; both endpoints must exist. Self-links and cycles are invalid. |
| Study queue | Ordered assignment IDs selected for a study session; references the same assignment records. |

Example: in a fictional CSC 223 module, completing a design outline unlocks an implementation task, which then unlocks a testing task. The course and module form a hierarchy, while the prerequisite links form a separate directed graph.

## Scope and feature plan

The minimum viable application has six capabilities. These are goals for the completed semester project; Milestone 1 implements only the executable foundation.

1. Manage records. Create, edit, list, and remove courses and assignments. Validate required fields and unique IDs before changing stored state.

2. Find and order work. Search by ID or title, filter by course or status, and sort by due date, priority, or title. Use ID as a deterministic tie-breaker.

3. Process a study queue. Add eligible assignments to a first-in-first-out queue and process them without dropping or repeating queued entries.

4. Browse course structure. Traverse a course and module hierarchy and display the assignments grouped beneath each module.

5. Suggest the next task. Use priority, due date, and ID to select eligible pending work. Exclude completed tasks and tasks with unfinished prerequisites.

6. Explain dependencies. Add prerequisite relationships and traverse connected assignments to report blockers. Reject missing endpoints, self-links, duplicate edges, and cycles.

### Optional enhancements

After the required application is stable, consider CSV import/export with schema validation, undo of the most recent status change, and a text workload summary by course. No more than these three enhancements are planned.

### Project boundaries

The project will not include production deployment, authentication, Canvas integration, a paid service, payment processing, real student records, collaborative editing, a complex graphical interface, or machine learning. The required version uses in-memory data and repeatable sample records; persistence remains optional.

### Success criteria

Record operations must preserve unique IDs and valid references, and rejected input must leave existing data unchanged. Searches must distinguish found and absent records; sorting ties must produce repeatable results. The queue must preserve insertion order, and hierarchy traversal must report each contained record once.

Priority selection must exclude completed or blocked assignments, and dependency traversal must identify all relevant blockers without looping on invalid input. Unit and integration tests must cover normal, empty, boundary, and invalid cases. A clean checkout must build and run using documented instructions, with reproducible search and sorting comparisons and a coherent final demonstration.

## Development plan for ten milestones

Each stage follows the corresponding course milestone. Exact dates will follow Canvas deadlines. Each stage ends with its tests or verification evidence, updated documentation, and a meaningful commit before dependent work begins.

| Stage | Course focus | Project contribution and evidence |
| --- | --- | --- |
| 1 | Foundation and approval | Define the problem and scope, establish the repository, and compile and run the Java starter. Obtain instructor feedback before advanced development. |
| 2 | Requirements and baseline | Write acceptance scenarios and a linear scan for pending work. Plan fixed-seed datasets of 100, 1000, and 10000 records and record comparison counts and timings. |
| 3 | Domain and sequences | Introduce Course, Module, and Assignment classes with validated identifiers and fields. Store records in arrays or lists and test insertion, update, removal, and state invariants. |
| 4 | Queue workflow | Implement a FIFO study-session queue. Verify empty behavior, insertion order, and consistency when an assignment is completed or removed. |
| 5 | Search and sort | Compare linear search with binary search on a sorted ID snapshot. Compare insertion sort and merge sort on due dates; separate sorting cost from search cost. |
| 6 | Hash retrieval | Introduce an ID-to-assignment map and a set of unique IDs. Compare retrieval with the baseline and test that updates keep every view consistent. |
| 7 | Hierarchy | Represent courses and modules as a rooted tree with assignment references at the leaves. Traverse it to group work and verify parent-child relationships. |
| 8 | Priority selection | Use a heap or priority queue for eligible work ordered by priority, due date, and ID. Refresh entries after updates and verify deterministic ties. |
| 9 | Dependencies and graphs | Represent prerequisite edges with adjacency lists. Use DFS to detect cycles and traversal to report blockers or affected dependent tasks. |
| 10 | Integration and release | Integrate the console workflow, rerun tests and performance comparisons, review complexity, document limitations, and prepare a tagged release candidate. |

The sequence is the initial baseline. Later maps, trees, heaps, and graph structures serve distinct operations; they will refer to stable assignment IDs rather than maintain conflicting copies of mutable records. Instructor requirements will determine where custom structures are necessary instead of library collections.

## Risks and mitigation

| Risk or constraint | Mitigation |
| --- | --- |
| Scope growth | Freeze the six core capabilities until approval. Keep the interface text-based and defer persistence and undo until required features pass their tests. |
| Unfamiliar structures | Trace small examples by hand, define contracts first, and add boundary tests as each structure is introduced. |
| Inconsistent indexes | Use unique stable IDs and one authoritative record store. Update or rebuild derived queue, map, and priority views through one service layer. |
| Cycles and blocked work | Reject self-links and cycles before storing edges. Test chains, branches, isolated tasks, and attempted cycles with fictional data. |
| Time and measurement limits | Reserve time at each stage for tests and documentation. Use fixed-seed inputs, repeated measurements, and operation counts; do not treat noisy timings as proof. |
| Environment and access | Use a documented Java target, confirm the required JDK, and verify instructor access to the private repository before submission. |

## Repository and technical evidence

Repository: https://github.com/elgun-dev/csc-223-coursework

Branch: main. Project directory: assignments/project-milestone-1-student-planner. The private class repository keeps this project separate from Assignment 1.9. Source and test locations, documentation, a README, and ignore rules exclude compiled output, IDE caches, and credentials.

The Java entry point is StudentAssignmentPlanner in package edu.csc223.planner. It was compiled with javac 17.0.20.1 using the Java 17 release target and executed successfully with exit code 0. The run script was also verified. The saved evidence contains these actual output lines:

Project: Student Assignment Planner

Status: Initial project structure created successfully.

Current Stage: Milestone 1 - Project Foundation

Compilation and execution evidence is in evidence/compilation-and-execution.txt. Three development commits separate proposal and scope, Java structure and starter, and the report with the plan and evidence. No domain test suite or advanced feature is claimed at this stage.

### Approval and disclosure

Project approval and instructor repository access remain pending. Java 17 is the verified project target; the instructor must confirm the course-required version and any repository naming or access rules. Review feedback and revise the plan before implementing dependent features.

AI assistance: An AI assistant generated the proposal, plan, Java starter, documentation, and verification commands and ran the verification. Elgun S must review the work and follow the instructor’s AI requirements; no independent-authorship claim is made.
