# PowerShell script to run SonoffHandlerFactory test suite
# Usage: .\run-tests.ps1

Write-Host "Running SonoffHandlerFactory test suite..." -ForegroundColor Cyan

# Check if Maven is available
try {
    $null = mvn -version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Maven is available" -ForegroundColor Green
    } else {
        throw "Maven not found"
    }
} catch {
    Write-Host "❌ Maven is not installed or not in PATH." -ForegroundColor Red
    Write-Host "Please install Maven and ensure it's in your PATH." -ForegroundColor Yellow
    exit 1
}

# Clean and compile test classes
Write-Host "📦 Compiling test classes..." -ForegroundColor Yellow
mvn clean test-compile -q

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Test compilation failed" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Test compilation successful" -ForegroundColor Green

# Run specific SonoffHandlerFactory tests
Write-Host "🧪 Running SonoffHandlerFactory tests..." -ForegroundColor Yellow
mvn test -Dtest="SonoffHandlerFactoryTest,SonoffHandlerFactoryIntegrationTest" -q

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ All SonoffHandlerFactory tests passed!" -ForegroundColor Green
    
    # Check for coverage report
    if (Test-Path "target\site\jacoco\index.html") {
        Write-Host "📊 Test coverage report generated: target\site\jacoco\index.html" -ForegroundColor Cyan
    }
    
    # Show test summary
    Write-Host ""
    Write-Host "📋 Test Summary:" -ForegroundColor Cyan
    Write-Host "- SonoffHandlerFactoryTest: Unit tests for core functionality" -ForegroundColor White
    Write-Host "- SonoffHandlerFactoryIntegrationTest: Integration and edge case tests" -ForegroundColor White
    Write-Host ""
    Write-Host "🎯 Coverage includes:" -ForegroundColor Cyan
    Write-Host "- Constructor and dependency injection" -ForegroundColor White
    Write-Host "- supportsThingType() for all supported/unsupported types" -ForegroundColor White
    Write-Host "- createHandler() for all 40+ device types" -ForegroundColor White
    Write-Host "- Error handling and edge cases" -ForegroundColor White
    Write-Host "- Bridge vs Thing handler creation" -ForegroundColor White
    Write-Host "- Null parameter handling" -ForegroundColor White
    Write-Host "- Case sensitivity and special characters" -ForegroundColor White
    
} else {
    Write-Host "❌ Some tests failed. Check the output above for details." -ForegroundColor Red
    exit 1
}