<#
.SYNOPSIS
    Runs the PayPulse backend tests and opens the HTML report.

.DESCRIPTION
    unit: unit tests only, no database needed.
    api:  API integration tests against PostgreSQL.
    all:  both (the default).

    The API tests start PostgreSQL in Docker (Testcontainers). Without Docker, pass -LocalDb to use
    your local PostgreSQL instead: the database (default paypulse_test) is created if missing, and every
    test rolls back, so it stays empty. The password is read from PAYPULSE_TEST_DB_PASSWORD or asked for.

.EXAMPLE
    .\test.ps1 unit
.EXAMPLE
    .\test.ps1 -LocalDb
.EXAMPLE
    .\test.ps1 api -LocalDb -Filter '*UserApiSecurityTest'
#>
param(
    [ValidateSet('all', 'unit', 'api')]
    [string] $Suite = 'all',
    [switch] $LocalDb,
    [string] $Database = 'paypulse_test',
    [string] $Filter,
    [switch] $NoReport
)

$ErrorActionPreference = 'Stop'
$task = @{ all = 'test'; unit = 'unitTest'; api = 'apiTest' }[$Suite]

function Initialize-LocalDatabase {
    if (-not $env:PAYPULSE_TEST_DB_USERNAME) { $env:PAYPULSE_TEST_DB_USERNAME = 'postgres' }
    if (-not $env:PAYPULSE_TEST_DB_PASSWORD) {
        $secure = Read-Host "PostgreSQL password for $env:PAYPULSE_TEST_DB_USERNAME" -AsSecureString
        $env:PAYPULSE_TEST_DB_PASSWORD = [System.Net.NetworkCredential]::new('', $secure).Password
        $script:promptedPassword = $true
    }
    $env:PAYPULSE_TEST_DB_URL = "jdbc:postgresql://localhost:5432/$Database"

    if (Get-Command psql -ErrorAction SilentlyContinue) {
        $env:PGPASSWORD = $env:PAYPULSE_TEST_DB_PASSWORD
        $exists = psql -h localhost -U $env:PAYPULSE_TEST_DB_USERNAME -d postgres -tAc "select 1 from pg_database where datname = '$Database'"
        if ($exists -ne '1') {
            Write-Host "Creating database $Database"
            psql -h localhost -U $env:PAYPULSE_TEST_DB_USERNAME -d postgres -c "create database $Database" | Out-Null
        }
        Remove-Item Env:PGPASSWORD
    }
    Write-Host "API tests use $env:PAYPULSE_TEST_DB_URL"
}

$gradleArgs = @($task, '--rerun')
if ($Filter) { $gradleArgs += @('--tests', $Filter) }

try {
    if ($LocalDb -and $Suite -ne 'unit') { Initialize-LocalDatabase }
    & "$PSScriptRoot\gradlew.bat" -p $PSScriptRoot @gradleArgs
    $exitCode = $LASTEXITCODE
}
finally {
    if ($LocalDb) {
        Remove-Item Env:PAYPULSE_TEST_DB_URL -ErrorAction SilentlyContinue
        if ($script:promptedPassword) { Remove-Item Env:PAYPULSE_TEST_DB_PASSWORD }
    }
}

$report = Join-Path $PSScriptRoot "build\reports\tests\$task\index.html"
if ($exitCode -eq 0) { Write-Host "`nAll tests passed." -ForegroundColor Green }
else { Write-Host "`nSome tests failed." -ForegroundColor Red }
Write-Host "Report: $report"
if (-not $NoReport -and (Test-Path $report)) { Invoke-Item $report }
exit $exitCode
