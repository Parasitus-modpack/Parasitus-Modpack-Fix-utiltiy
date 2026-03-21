$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$gradleWrapper = Join-Path $repoRoot "gradlew.bat"

if (-not (Test-Path $gradleWrapper)) {
    throw "Could not find gradlew.bat at $gradleWrapper"
}

Push-Location $repoRoot
try {
    & $gradleWrapper build
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }

    $artifact = Join-Path $repoRoot "build\\libs\\Parasitus-Core-1.4.0.jar"
    if (Test-Path $artifact) {
        Write-Host "Build complete: $artifact"
    } else {
        Write-Host "Build complete."
    }
}
finally {
    Pop-Location
}
