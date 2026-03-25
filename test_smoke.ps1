param(
    [int]$Port = 23456,
    [switch]$SkipCompile
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$root = if ([string]::IsNullOrWhiteSpace($PSScriptRoot)) { (Get-Location).Path } else { $PSScriptRoot }

function Assert-Contains {
    param(
        [string]$Output,
        [string]$Expected,
        [string]$Label
    )

    if ($Output -notmatch [regex]::Escape($Expected)) {
        throw "FALHOU [$Label]: nao encontrou '$Expected' no output.`n--- OUTPUT ---`n$Output`n--- FIM OUTPUT ---"
    }

    Write-Host "OK [$Label]"
}

function Invoke-ClientSession {
    param(
        [int]$Port,
        [string]$Username,
        [string]$Password,
        [string[]]$Commands
    )

    $tmpInput = [System.IO.Path]::GetTempFileName()
    $tmpOut = [System.IO.Path]::GetTempFileName()
    $tmpErr = [System.IO.Path]::GetTempFileName()
    try {
        # Each command goes in one line as expected by SpertaClient Scanner.
        $payload = ($Commands -join "`r`n") + "`r`n"
        [System.IO.File]::WriteAllText($tmpInput, $payload)

        $cmd = "type `"$tmpInput`" | java -cp bin server.SpertaClient 127.0.0.1 $Port $Username $Password"
        $proc = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", $cmd -Wait -PassThru -NoNewWindow -RedirectStandardOutput $tmpOut -RedirectStandardError $tmpErr

        # SpertaClient throws NoSuchElementException when piped stdin ends.
        # That is expected for this smoke flow, so we assert only on stdout.
        $output = if (Test-Path $tmpOut) { Get-Content $tmpOut -Raw } else { "" }
        return $output
    }
    finally {
        Remove-Item -Force $tmpInput -ErrorAction SilentlyContinue
        Remove-Item -Force $tmpOut -ErrorAction SilentlyContinue
        Remove-Item -Force $tmpErr -ErrorAction SilentlyContinue
    }
}

$serverOut = Join-Path $root "logs\\smoke_server_stdout.log"
$serverErr = Join-Path $root "logs\\smoke_server_stderr.log"
if (!(Test-Path (Join-Path $root "logs"))) {
    New-Item -ItemType Directory -Path (Join-Path $root "logs") | Out-Null
}

$serverProcess = $null
try {
    if (-not $SkipCompile) {
        Write-Host "[1/4] A compilar projeto..."
        $files = Get-ChildItem -Path (Join-Path $root "src") -Recurse -Filter *.java | ForEach-Object { $_.FullName }
        if (!(Test-Path (Join-Path $root "bin"))) {
            New-Item -ItemType Directory -Path (Join-Path $root "bin") | Out-Null
        }
        & javac -d (Join-Path $root "bin") $files
        if ($LASTEXITCODE -ne 0) {
            throw "Compilacao falhou."
        }
    }

    Write-Host "[2/4] A iniciar servidor na porta $Port..."
    $serverProcess = Start-Process -FilePath "java" -ArgumentList "-cp", "bin", "server.SpertaServer", "$Port" -WorkingDirectory $root -RedirectStandardOutput $serverOut -RedirectStandardError $serverErr -PassThru

    Start-Sleep -Seconds 2

    if ($serverProcess.HasExited) {
        $outText = if (Test-Path $serverOut) { Get-Content $serverOut -Raw } else { "" }
        $errText = if (Test-Path $serverErr) { Get-Content $serverErr -Raw } else { "" }
        throw "Servidor terminou ao arrancar (exit $($serverProcess.ExitCode)).`nSTDOUT:`n$outText`nSTDERR:`n$errText"
    }

    $suffix = Get-Date -Format "HHmmss"
    $owner = "owner_$suffix"
    $user = "user_$suffix"
    $house = "casa_$suffix"
    $section = "Electros"
    $device = "E1"

    Write-Host "[3/4] A preparar utilizador secundario..."
    $seedUserOutput = Invoke-ClientSession -Port $Port -Username $user -Password "1111" -Commands @()
    Assert-Contains -Output $seedUserOutput -Expected "Resposta de autenticacao:" -Label "auth secondary user"

    Write-Host "[3/4] A correr cenario owner..."
    $ownerOutput = Invoke-ClientSession -Port $Port -Username $owner -Password "1234" -Commands @(
        "CREATE $house",
        "ADD $user $house $section",
        "RD $house $section",
        "EC $house $device 15",
        "RT $house",
        "RH $house $device",
        "EC $house $device abc"
    )

    Assert-Contains -Output $ownerOutput -Expected "Resposta de autenticacao:" -Label "auth owner"
    Assert-Contains -Output $ownerOutput -Expected "Casa '$house' criada com sucesso" -Label "create"
    Assert-Contains -Output $ownerOutput -Expected "Utilizador '$user' adicionado" -Label "add user"
    Assert-Contains -Output $ownerOutput -Expected "Dispositivo registado com sucesso" -Label "register device"
    Assert-Contains -Output $ownerOutput -Expected "ligado por mais 15 minutos" -Label "device command"
    Assert-Contains -Output $ownerOutput -Expected "Historico do dispositivo '$device'" -Label "history"
    Assert-Contains -Output $ownerOutput -Expected "ERRO: valor para dispositivo deve ser um inteiro" -Label "integer validation"

    Write-Host "[4/4] A correr cenario user/permissoes..."
    $userOutput = Invoke-ClientSession -Port $Port -Username $user -Password "1111" -Commands @(
        "RD $house $section",
        "ADD $owner $house $section"
    )

    Assert-Contains -Output $userOutput -Expected "Dispositivo registado com sucesso" -Label "authorized user can RD"
    Assert-Contains -Output $userOutput -Expected "ERRO:" -Label "non-owner cannot ADD"

    Write-Host ""
    Write-Host "SMOKE TEST OK"
    Write-Host "Servidor: porta $Port"
    Write-Host "Owner: $owner"
    Write-Host "User: $user"
    Write-Host "House: $house"
}
finally {
    if ($serverProcess -and -not $serverProcess.HasExited) {
        Stop-Process -Id $serverProcess.Id -Force
    }
}
