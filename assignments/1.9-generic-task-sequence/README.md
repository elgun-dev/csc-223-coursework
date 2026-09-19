# 1.9: Generic Task Sequence and Search

Student: Elgun (replace with full roster name if needed)
Course: CSC 223, Module 1
Language: Kotlin/JVM 2.2.21, targeting Java 17
Verification: JDK 17.0.20.1, Maven 3.9.9, JUnit Jupiter 5.11.4

The user reports instructor approval for Kotlin instead of Java. Each required class and test class is in its own .kt file under package edu.csc223.tasks.

## Build and test

Install JDK 17 and Maven 3.9.9 or a compatible Maven 3.9 release. Set JAVA_HOME to that JDK, then run from this directory:

```shell
mvn clean test
```

The first run needs internet access for dependencies. Results appear in target/surefire-reports/. Saved verification evidence is in evidence/. Build configuration follows the [official Kotlin Maven documentation](https://kotlinlang.org/docs/maven.html), with JUnit pinned to version 5 for this assignment.

## Completed requirements

- Immutable ProjectTask; validated ID, title, and priority; ID-based equality, hash code, natural ordering; readable string representation.
- Generic TaskSequence contract with add, get, remove, size, and isEmpty.
- Internal array, default and positive-capacity constructors, validated append/get/remove, shifted removal, cleared unused slots, doubled capacity, preserved order and elements.
- Descending priority comparator with ascending ID tie-breaker.
- Generic first-match sequential search using logical equality, -1 when absent, null-sequence rejection, and unchanged sequence state.
- 34 passing JUnit 5 tests covering normal, invalid, boundary, state-change, resize, comparison, and search behavior.
- Actual execution evidence and an AI-generated technical reasoning draft for student review.

## Design choices and limitations

Invalid constructor arguments and null additions throw IllegalArgumentException; invalid indices throw IndexOutOfBoundsException. Nullable input parameters permit direct testing of null rejection from Kotlin. Stored values are non-null (T : Any). A null search target returns -1.

IDs are case-sensitive and lexicographic, without trimming or numeric interpretation. Duplicate IDs can occur in a sequence for first-match searches. Priority ordering can distinguish equal-by-ID tasks with different priorities; use it for sorting rather than identity in a sorted set. The sequence is not thread-safe; very large allocations are limited by JVM memory, and capacity overflow is rejected before mutation.

This Kotlin version differs from the literal required .java file list. Confirm the approved submission format. The student must review the name and reasoning; no claim of personal understanding is made here.

## AI-use disclosure

OpenAI Codex generated the implementation, automated tests, build configuration, documentation, and technical reasoning draft, and ran the tests. This is not independently student-written work. The pasted assignment prohibits AI-generated complete classes and test suites; approval for Kotlin does not by itself waive that restriction. Obtain an explicit exception before submitting this generated work under that policy. Do not claim independent authorship unless true.

## Submission packaging

Include pom.xml, src/, README.md, TECHNICAL_REASONING.md, and evidence/. Exclude target/, dependency caches, .git/, and IDE files. Review the code and reasoning before submission.
