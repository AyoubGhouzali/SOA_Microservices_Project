# Load environment variables
Write-Host "Loading environment variables..." -ForegroundColor Cyan
$envFile = ".env"

if (-not (Test-Path $envFile)) {
    Write-Host "ERROR: .env file not found!" -ForegroundColor Red
    exit 1
}

Get-Content $envFile | ForEach-Object {
    if ($_ -match '^([^#][^=]+)=(.*)$') {
        $name = $matches[1].Trim()
        $value = $matches[2].Trim()
        Set-Item -Path "env:$name" -Value $value
    }
}

# Verify
if (-not $env:USER_DB_URL) {
    Write-Host "ERROR: USER_DB_URL not set!" -ForegroundColor Red
    exit 1
}

Write-Host "Environment loaded successfully!" -ForegroundColor Green
Write-Host "Database URL: $env:USER_DB_URL" -ForegroundColor Gray
Write-Host "Username: $env:USER_DB_USERNAME" -ForegroundColor Gray

# Run service
Write-Host ""
Write-Host "Starting User Service..." -ForegroundColor Cyan
cd services/user-service
./mvnw spring-boot:run
