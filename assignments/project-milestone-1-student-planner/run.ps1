$ErrorActionPreference = 'Stop'
$compilerPath = 'javac'
$runtimePath = 'java'
if ($env:JAVA_HOME) {
    $compilerPath = Join-Path $env:JAVA_HOME 'bin/javac.exe'
    $runtimePath = Join-Path $env:JAVA_HOME 'bin/java.exe'
}
$sourcePath = Join-Path $PSScriptRoot 'src/main/java/edu/csc223/planner/StudentAssignmentPlanner.java'
$classPath = Join-Path $PSScriptRoot 'target/classes'
& $compilerPath --release 17 -d $classPath $sourcePath
if ($LASTEXITCODE -ne 0) { throw "Compilation failed with exit code $LASTEXITCODE." }
& $runtimePath -cp $classPath edu.csc223.planner.StudentAssignmentPlanner
if ($LASTEXITCODE -ne 0) { throw "Execution failed with exit code $LASTEXITCODE." }
