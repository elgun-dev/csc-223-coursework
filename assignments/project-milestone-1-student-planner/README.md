# Student Assignment Planner

CSC 223 Progressive Software Development Project, Milestone 1

Author: Elgun

## Purpose and users

A proposed local console application for college students who need to organize assignments across courses, identify upcoming work, and understand prerequisite tasks. It will store fictional course, assignment, and dependency records for development and demonstration.

## Current status

Milestone 1 foundation only. The proposal is awaiting instructor approval. The initial Java entry point prints the project title and status; assignment management and later data structures are planned rather than implemented.

## Environment

Java 17 is the project compatibility target. Verification uses the workspace JDK 17; the supplied milestone does not identify a specific required JDK release, so confirm the instructor's version before submission. This is a Java project that may later include Kotlin/JVM if approved; Milestone 1 uses Java source files as requested by the assignment.

## Repository and access

Repository: https://github.com/elgun-dev/csc-223-coursework

Branch: main

Project folder: assignments/project-milestone-1-student-planner

Visibility: private. Instructor access has not been verified. The course-specific naming and access instructions must be confirmed before submission. Keep this project separate from Assignment 1.9.

## Project structure

- src/main/java/edu/csc223/planner: executable Java entry point.
- src/test/java/edu/csc223/planner: reserved test location and testing plan.
- docs: proposal, scope, development plan, and milestone report.
- evidence: actual compilation and execution results.
- pom.xml: Java 17 Maven project for IntelliJ import.
- run.ps1: compile and run the starter from PowerShell.

## Compile and run

Open this folder as a project in IntelliJ and load pom.xml when prompted. Select JDK 17 as the project SDK and run StudentAssignmentPlanner.main.

For a command-line run, put a JDK on PATH, open PowerShell in this project folder, and run ./run.ps1. The script invokes javac with --release 17, stops if compilation fails, and then starts the application. It can also use the JDK named by JAVA_HOME.

Manual commands from this directory:

    javac --release 17 -d target/classes src/main/java/edu/csc223/planner/StudentAssignmentPlanner.java
    java -cp target/classes edu.csc223.planner.StudentAssignmentPlanner

Expected output:

    Project: Student Assignment Planner
    Status: Initial project structure created successfully.
    Current Stage: Milestone 1 - Project Foundation

Maven users can run mvn compile, followed by the java command above. Maven is optional for this dependency-free starter. The direct javac/java path is the verified execution path recorded in evidence.

## Next milestones and limitations

See docs/milestone-plan.md and docs/milestone-1-report.md. No advanced feature is implemented before topic approval. There are no domain tests yet because no domain behavior exists; the entry point is verified through compilation, exit codes, and its complete console output.

## AI assistance

An AI assistant helped choose the topic and generated the proposal, development plan, Java starter, documentation, and verification commands, then executed the verification. The student must review the work and follow the instructor's assignment-specific AI requirements. This disclosure does not assert independent authorship or personal mastery.
