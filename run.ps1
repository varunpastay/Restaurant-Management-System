# Builds, deploys, and (re)starts Tomcat for the Restaurant Ordering System.
# Run every time you want to build + launch with your latest code:
#   .\run.ps1
#
# NOTE: this always does a full Tomcat restart rather than relying on
# hot-redeploy - autoDeploy on this machine does not reliably pick up a
# replaced WAR file (verified: the exploded webapps\restaurant-ordering-system\
# folder can sit stale for many minutes after a newer WAR is copied in), so a
# clean restart is the only way to be sure you're running the code you just built.

$ErrorActionPreference = "Stop"

$ProjectDir   = $PSScriptRoot
$CatalinaHome = "C:\apache-tomcat-10.1.54\apache-tomcat-10.1.54"
$WebappsDir   = "$CatalinaHome\webapps"
$WarName      = "restaurant-ordering-system.war"
$ExplodedDir  = "$WebappsDir\restaurant-ordering-system"
$AppUrl       = "http://localhost:8081/restaurant-ordering-system/menu?table=1&token=a1e6f9c2b3d84e0f9a1c2b3d4e5f6071"

$env:CATALINA_HOME = $CatalinaHome

Write-Host "== Building ==" -ForegroundColor Cyan
Set-Location $ProjectDir
mvn -B -q package -DskipTests
if ($LASTEXITCODE -ne 0) { Write-Host "Build failed." -ForegroundColor Red; exit 1 }

$tomcatRunning = Get-NetTCPConnection -LocalPort 8081 -ErrorAction SilentlyContinue
if ($tomcatRunning) {
    Write-Host "== Stopping Tomcat ==" -ForegroundColor Cyan
    & "$CatalinaHome\bin\shutdown.bat"
    $waited = 0
    while ((Get-NetTCPConnection -LocalPort 8081 -ErrorAction SilentlyContinue) -and $waited -lt 30) {
        Start-Sleep -Seconds 1
        $waited++
    }
}

Write-Host "== Deploying WAR (clean) ==" -ForegroundColor Cyan
if (Test-Path $ExplodedDir) { Remove-Item $ExplodedDir -Recurse -Force }
Copy-Item "$ProjectDir\target\$WarName" "$WebappsDir\$WarName" -Force

Write-Host "== Starting Tomcat ==" -ForegroundColor Cyan
& "$CatalinaHome\bin\startup.bat"

Write-Host "== Waiting for app to come up ==" -ForegroundColor Cyan
$ready = $false
for ($i = 0; $i -lt 30; $i++) {
    Start-Sleep -Seconds 2
    try {
        $r = Invoke-WebRequest -Uri $AppUrl -UseBasicParsing -TimeoutSec 5
        if ($r.StatusCode -eq 200) { $ready = $true; break }
    } catch { }
}

if (-not $ready) {
    Write-Host "App did not come up in time - check the log:" -ForegroundColor Red
    Write-Host "  Get-Content `"$CatalinaHome\logs\catalina.out`" -Tail 40"
    exit 1
}

Write-Host ""
Write-Host "App is up." -ForegroundColor Green
Write-Host "Customer menu : $AppUrl"
Write-Host "Staff login   : http://localhost:8081/restaurant-ordering-system/staff/login"
Write-Host "Admin login   : http://localhost:8081/restaurant-ordering-system/admin/login"
