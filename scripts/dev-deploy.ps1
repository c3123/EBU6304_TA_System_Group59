param(
    [Parameter(Mandatory = $true)]
    [string]$TomcatWebappsPath,

    [string]$AppName = "web",

    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"

$ScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptRoot
$WebRoot = Join-Path $ProjectRoot "web"
$ExplodedSource = Join-Path $WebRoot "target\web"
$DeployTarget = Join-Path $TomcatWebappsPath $AppName

if (-not (Test-Path $WebRoot)) {
    throw "Cannot find web module at: $WebRoot"
}

if (-not (Test-Path $TomcatWebappsPath)) {
    throw "Tomcat webapps path does not exist: $TomcatWebappsPath"
}

if (-not $SkipBuild) {
    Write-Host "[1/3] Building exploded web app..." -ForegroundColor Cyan
    Push-Location $WebRoot
    try {
        & mvn "-DskipTests" compile war:exploded
        if ($LASTEXITCODE -ne 0) {
            throw "Maven build failed with exit code $LASTEXITCODE"
        }
    }
    finally {
        Pop-Location
    }
}

if (-not (Test-Path $ExplodedSource)) {
    throw "Exploded artifact not found: $ExplodedSource"
}

if (-not (Test-Path $DeployTarget)) {
    Write-Host "[2/3] Creating deploy target: $DeployTarget" -ForegroundColor Cyan
    New-Item -ItemType Directory -Path $DeployTarget | Out-Null
} else {
    Write-Host "[2/3] Updating deploy target: $DeployTarget" -ForegroundColor Cyan
}

Write-Host "[3/3] Syncing files to Tomcat (keeping runtime data/uploads)..." -ForegroundColor Cyan
$null = & robocopy $ExplodedSource $DeployTarget /MIR /XD "WEB-INF\data" "WEB-INF\uploads"
$robocopyExit = $LASTEXITCODE
if ($robocopyExit -gt 7) {
    throw "Robocopy failed with exit code $robocopyExit"
}

Write-Host "Done. Deployed exploded app to: $DeployTarget" -ForegroundColor Green
Write-Host "Tip: after only JS/JSP/CSS edits, you can rerun with -SkipBuild." -ForegroundColor Yellow
