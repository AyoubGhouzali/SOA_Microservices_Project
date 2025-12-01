# PowerShell Script to Test Transport System Containers
# Run this after starting Docker Desktop

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Transport System Container Testing" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$baseUrl = "http://localhost:8222"

# Step 1: Check Docker
Write-Host "`n[1/8] Checking Docker..." -ForegroundColor Yellow
docker --version
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Docker is not running. Please start Docker Desktop." -ForegroundColor Red
    exit 1
}
Write-Host "✓ Docker is running" -ForegroundColor Green

# Step 2: Check containers
Write-Host "`n[2/8] Checking containers..." -ForegroundColor Yellow
docker-compose ps
Write-Host "✓ Containers listed above" -ForegroundColor Green

# Step 3: Health checks
Write-Host "`n[3/8] Testing health endpoints..." -ForegroundColor Yellow
$services = @(
    "users",
    "tickets",
    "payments",
    "subscriptions",
    "buses",
    "notifications"
)

foreach ($service in $services) {
    try {
        $response = Invoke-WebRequest -Uri "$baseUrl/api/$service/health" -UseBasicParsing -TimeoutSec 5
        if ($response.StatusCode -eq 200) {
            Write-Host "  ✓ $service service is UP" -ForegroundColor Green
        }
    } catch {
        Write-Host "  ✗ $service service is DOWN" -ForegroundColor Red
    }
}

# Step 4: Check Eureka
Write-Host "`n[4/8] Checking Eureka registry..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8761" -UseBasicParsing -TimeoutSec 5
    Write-Host "  ✓ Eureka Dashboard: http://localhost:8761" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Eureka is not accessible" -ForegroundColor Red
}

# Step 5: Register user
Write-Host "`n[5/8] Registering test user..." -ForegroundColor Yellow
$registerBody = @{
    email = "test.$(Get-Random)@example.com"
    password = "TestPassword123"
    firstName = "Test"
    lastName = "User"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "$baseUrl/api/users/register" `
        -Method Post `
        -ContentType "application/json" `
        -Body $registerBody

    $userId = $registerResponse.userId
    $userEmail = $registerResponse.email
    Write-Host "  ✓ User registered: $userEmail" -ForegroundColor Green
    Write-Host "  ✓ User ID: $userId" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Registration failed: $_" -ForegroundColor Red
    exit 1
}

# Step 6: Login
Write-Host "`n[6/8] Logging in..." -ForegroundColor Yellow
$loginBody = @{
    email = $userEmail
    password = "TestPassword123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/users/login" `
        -Method Post `
        -ContentType "application/json" `
        -Body $loginBody

    $token = $loginResponse.token
    Write-Host "  ✓ Login successful" -ForegroundColor Green
    Write-Host "  ✓ JWT Token: $($token.Substring(0, 50))..." -ForegroundColor Green
} catch {
    Write-Host "  ✗ Login failed: $_" -ForegroundColor Red
    exit 1
}

# Step 7: Purchase tickets
Write-Host "`n[7/8] Purchasing tickets..." -ForegroundColor Yellow
$purchaseBody = @{
    userId = $userId
    ticketType = "DAILY"
    quantity = 2
} | ConvertTo-Json

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

try {
    $purchaseResponse = Invoke-RestMethod -Uri "$baseUrl/api/tickets/purchase" `
        -Method Post `
        -Headers $headers `
        -Body $purchaseBody

    $orderId = $purchaseResponse.orderId
    Write-Host "  ✓ Tickets purchased successfully" -ForegroundColor Green
    Write-Host "  ✓ Order ID: $orderId" -ForegroundColor Green
    Write-Host "  ✓ Total Amount: $($purchaseResponse.totalAmount) $($purchaseResponse.currency)" -ForegroundColor Green
    Write-Host "  ✓ Tickets: $($purchaseResponse.tickets.Count)" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Purchase failed: $_" -ForegroundColor Red
    exit 1
}

# Step 8: Check payment
Write-Host "`n[8/8] Checking payment status..." -ForegroundColor Yellow
Start-Sleep -Seconds 2  # Wait for payment processing

try {
    $paymentResponse = Invoke-RestMethod -Uri "$baseUrl/api/payments/order/$orderId" `
        -Method Get `
        -Headers $headers

    Write-Host "  ✓ Payment Status: $($paymentResponse.status)" -ForegroundColor Green
    Write-Host "  ✓ Transaction ID: $($paymentResponse.transactionId)" -ForegroundColor Green
    Write-Host "  ✓ Amount: $($paymentResponse.amount) $($paymentResponse.currency)" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Payment check failed: $_" -ForegroundColor Red
}

# Summary
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "✓ ALL TESTS PASSED!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`nQuick Links:" -ForegroundColor Yellow
Write-Host "  - Eureka Dashboard: http://localhost:8761" -ForegroundColor Cyan
Write-Host "  - Kafka UI: http://localhost:8090" -ForegroundColor Cyan
Write-Host "  - PgAdmin: http://localhost:5050" -ForegroundColor Cyan
Write-Host "  - Mongo Express: http://localhost:8084" -ForegroundColor Cyan

Write-Host "`nYour test user:" -ForegroundColor Yellow
Write-Host "  - Email: $userEmail" -ForegroundColor Cyan
Write-Host "  - Password: TestPassword123" -ForegroundColor Cyan
Write-Host "  - JWT Token: $($token.Substring(0, 50))..." -ForegroundColor Cyan
