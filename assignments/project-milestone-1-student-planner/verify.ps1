param([string]$JavaBin = '')
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    $javac = if ($JavaBin) {Join-Path $JavaBin 'javac.exe'} else {'javac'}
    $java = if ($JavaBin) {Join-Path $JavaBin 'java.exe'} else {'java'}
    $jar = 'lib/junit-platform-console-standalone-1.11.4.jar'
    if (!(Test-Path -LiteralPath $jar)) {
        New-Item -ItemType Directory -Force lib | Out-Null
        $uri = 'https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-console-standalone/1.11.4/junit-platform-console-standalone-1.11.4.jar'
        Invoke-WebRequest -Uri $uri -OutFile $jar -UseBasicParsing
    }
    $expected = 'B016EF6B1C3454D6D7C2C88CE081DABF289699686AF6622D6E4E2E1B54B4A2FC'
    $hasher = [System.Security.Cryptography.SHA256]::Create()
    try { $actual = [BitConverter]::ToString($hasher.ComputeHash([IO.File]::ReadAllBytes((Join-Path $PSScriptRoot $jar)))).Replace('-', '') } finally {$hasher.Dispose()}
    if ($actual -ne $expected) {
        throw 'JUnit dependency checksum mismatch; replace with the documented artifact'
    }
    New-Item -ItemType Directory -Force build/classes | Out-Null
    $sources = Get-ChildItem src,benchmark/src -Recurse -Filter '*.java'
    & $javac --release 17 -cp $jar -d build/classes $sources.FullName
    if ($LASTEXITCODE -ne 0) {throw 'Compilation failed'}
    & $java -jar $jar execute --class-path build/classes --scan-class-path --disable-ansi-colors --details summary
    if ($LASTEXITCODE -ne 0) {throw 'Tests failed'}
    & $java -cp build/classes edu.csc223.planner.FinalValidationEvidence
    if ($LASTEXITCODE -ne 0) {throw 'Validation evidence failed'}
    & $java -cp build/classes edu.csc223.planner.StudentAssignmentPlanner --version
    if ($LASTEXITCODE -ne 0) {throw 'Version check failed'}
    & $java -cp build/classes edu.csc223.planner.StudentAssignmentPlanner --release
    if ($LASTEXITCODE -ne 0) {throw 'Integrated demonstration failed'}
} finally {Pop-Location}
