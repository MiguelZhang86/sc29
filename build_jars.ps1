Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$root = if ([string]::IsNullOrWhiteSpace($PSScriptRoot)) { (Get-Location).Path } else { $PSScriptRoot }

if (!(Test-Path (Join-Path $root "bin"))) {
    New-Item -ItemType Directory -Path (Join-Path $root "bin") | Out-Null
}

$files = Get-ChildItem -Path (Join-Path $root "src") -Recurse -Filter *.java | ForEach-Object { $_.FullName }

Write-Host "A compilar..."
& javac -d (Join-Path $root "bin") $files
if ($LASTEXITCODE -ne 0) {
    throw "Compilacao falhou."
}

if (!(Test-Path (Join-Path $root "dist"))) {
    New-Item -ItemType Directory -Path (Join-Path $root "dist") | Out-Null
}

Write-Host "A criar JAR do cliente..."
& jar cfe (Join-Path $root "dist/sperta-client.jar") server.SpertaClient -C (Join-Path $root "bin") .
if ($LASTEXITCODE -ne 0) {
    throw "Falha a criar sperta-client.jar"
}

Write-Host "A criar JAR do servidor..."
& jar cfe (Join-Path $root "dist/sperta-server.jar") server.SpertaServer -C (Join-Path $root "bin") .
if ($LASTEXITCODE -ne 0) {
    throw "Falha a criar sperta-server.jar"
}

Write-Host "Concluido:"
Write-Host " - dist/sperta-client.jar"
Write-Host " - dist/sperta-server.jar"
