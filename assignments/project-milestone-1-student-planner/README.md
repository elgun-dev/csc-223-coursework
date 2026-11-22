# Student Assignment Planner

Elgun Shiralizade | CSC 223 | Final local release v1.0.0 | 2026-11-22

Purpose: a local console demonstration and Java API for one student's fictional
courses, modules, assignments, priorities, and prerequisites. The default scenario
is integrated; the interface is not an interactive task editor.

Features: validated records and guarded deletion; title/ID search; course/status
filters; due/priority/title ordering; prerequisite-aware FIFO and recommendations;
course/module hierarchy; ID ranges; BFS/DFS/paths; blockers; invariant validation;
explicit reconstruction of derived ID indexes.

Prerequisites: Windows PowerShell and a JDK supporting --release 17. Execution
was verified on Java 27, not a Java 17 VM. Put java and javac on PATH. JUnit Platform Console Standalone 1.11.4 is test-only. A local copy may be in lib;
if missing, verify.ps1 downloads the pinned artifact from Maven Central and checks
its SHA-256 digest before execution. First-time remote setup therefore needs
internet access. License notices are inside the JAR. Application execution needs
no account, database, network, external input data, or IDE.

From this directory:
    powershell -NoProfile -ExecutionPolicy Bypass -File .\verify.ps1
Optional: pass -JavaBin with your installed JDK bin directory if not on PATH.
The script compiles src and benchmark/src, runs all 184 tests, runs controlled
corruption/recovery evidence, prints the version, and runs the integrated demo.
An unexpected nonzero process exit fails verification. Bypass applies only to
the launched PowerShell process; the script does not change system policy.

Run after compilation:
    java -cp build/classes edu.csc223.planner.StudentAssignmentPlanner
    java -cp build/classes edu.csc223.planner.StudentAssignmentPlanner --help

Reproduce the two final performance investigations:
    java -Xms128m -Xmx512m -cp build/classes edu.csc223.planner.ReleaseBenchmark > benchmark/raw/heap-new.csv
    java -cp build/classes edu.csc223.planner.GraphExperiment benchmark/raw/graph-new

The graph command creates graph-new/graph.csv, preserving the submitted evidence.
Both experiments generate deterministic fictional data and verify results.
The heap comparison includes setup outside timing and five trials per condition;
graph results are operation counts, not timings.

Layout:
  src/main/java/edu/csc223/planner  domain, services, structures, demos
  src/test/java/edu/csc223/planner  full suite and test-only validation executable
  benchmark/src/edu/csc223/planner comparison-only utility sources
  benchmark/raw                  final measurement CSV files
  benchmark/figures              charts matching the report
  docs                           final technical report and release notes
  evidence                       tests, validation, demo, audit, source manifest
  lib                            JUnit standalone test dependency
  VERSION, verify.ps1             release marker and clean build/run procedure

The main entry point defaults to the strict ReleasePlanner workflow. Historical
--legacy, --search, --index, --tree, --priority, and --graph modes are intentionally
retained component diagnostics. Their advisory graph/manual-arrival scheduling
contracts differ from the release workflow; --help identifies that boundary.

Known limitations: in-memory, single-threaded; no persistence or interactive editor;
unbalanced ID tree; incoming dependency scans; full recommendation rebuild per
query; historical manual history is unbounded. Resource exhaustion and concurrent
mutation are not transactional. A repeated version marker does not certify
instructor approval or remote repository publication.

Repository reference: https://github.com/elgun-dev/csc-223-coursework
Project path: assignments/project-milestone-1-student-planner
Final release branch: final-release-v1.0.0
The branch preserves the existing repository history and integrates the current
software. Main is being brought forward to the same release. Earlier milestones were developed locally; this
does not fabricate missing progressive remote commits. Instructor access to the
private repository remains unverified.

The final technical report submitted through Canvas contains the requirements traceability, architecture, design decisions, complexity analysis, testing discussion, and limitations. Read it in [docs/final-report.txt](docs/final-report.txt): section 3 covers traceability, section 4 architecture, sections 5–7 decisions and performance, section 8 tests and defects, and section 10 limitations.
