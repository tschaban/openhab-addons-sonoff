# Sonoff Binding Unit Test Runner
# Runs code formatting and unit tests for the Sonoff binding

# Step 1: Clear console for clean output
clear
Write-Host "[STEP 1/4] Clearing console for clean output..." -ForegroundColor Yellow

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Sonoff Binding Unit Test Runner" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 2: Navigate to project directory
Write-Host "[STEP 2/4] Navigating to project directory..." -ForegroundColor Yellow
#$projectPath = "D:\Adrian\Projekty\openhab\openhab-addons\bundles\org.openhab.binding.sonoff"
$projectPath = "C:\Users\adria\Desktop\Projekty\openhab-addons\bundles\org.openhab.binding.sonoff"
Write-Host "Target directory: $projectPath" -ForegroundColor Gray

if (Test-Path $projectPath) {
    cd $projectPath
    Write-Host "[OK] Successfully changed to project directory" -ForegroundColor Green
} else {
    Write-Host "[ERROR] Project directory not found: $projectPath" -ForegroundColor Red
    Write-Host "Please verify the path exists and try again." -ForegroundColor Yellow
    pause
    exit 1
}

Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Sonoff Binding Unit Test Runner" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Project: $(Split-Path -Leaf (Get-Location))" -ForegroundColor Gray
Write-Host "Location: $(Get-Location)" -ForegroundColor Gray
Write-Host ""

# Step 3: Apply code formatting
Write-Host "[STEP 3/4] Applying code formatting with Spotless..." -ForegroundColor Yellow
Write-Host "Running: mvn spotless:apply" -ForegroundColor Gray
Write-Host ""

$spotlessStart = Get-Date
mvn spotless:apply
$spotlessResult = $LASTEXITCODE
$spotlessEnd = Get-Date
$spotlessDuration = ($spotlessEnd - $spotlessStart).TotalSeconds

Write-Host ""
if ($spotlessResult -eq 0) {
    Write-Host "[OK] Code formatting completed successfully" -ForegroundColor Green
    Write-Host "Duration: $([math]::Round($spotlessDuration, 2)) seconds" -ForegroundColor Gray
} else {
    Write-Host "[ERROR] Code formatting failed (exit code: $spotlessResult)" -ForegroundColor Red
    Write-Host "Duration: $([math]::Round($spotlessDuration, 2)) seconds" -ForegroundColor Gray
    Write-Host ""
    Write-Host "This may indicate code style violations that need to be fixed." -ForegroundColor Yellow
    Write-Host "Check the output above for specific formatting issues." -ForegroundColor Yellow
    pause
    exit $spotlessResult
}

Write-Host ""

# Step 4: Run unit tests
Write-Host "[STEP 4/4] Running unit tests..." -ForegroundColor Yellow
Write-Host "Running: mvn test" -ForegroundColor Gray
Write-Host ""
Write-Host "This will:" -ForegroundColor Gray
Write-Host "  - Compile all source and test classes" -ForegroundColor Gray
Write-Host "  - Execute JUnit 5 test suites" -ForegroundColor Gray
Write-Host "  - Generate test reports" -ForegroundColor Gray
Write-Host "  - Show test results and coverage" -ForegroundColor Gray
Write-Host ""

$testStart = Get-Date
mvn test
$testResult = $LASTEXITCODE
$testEnd = Get-Date
$testDuration = ($testEnd - $testStart).TotalSeconds

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "           TEST EXECUTION SUMMARY" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

if ($testResult -eq 0) {
    Write-Host "[SUCCESS] All unit tests passed!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Test execution completed in $([math]::Round($testDuration, 2)) seconds" -ForegroundColor Gray
    Write-Host "Code formatting duration: $([math]::Round($spotlessDuration, 2)) seconds" -ForegroundColor Gray
    Write-Host "Total execution time: $([math]::Round($spotlessDuration + $testDuration, 2)) seconds" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Cyan
    Write-Host "  - Review test reports in target/surefire-reports/" -ForegroundColor White
    Write-Host "  - Check code coverage if configured" -ForegroundColor White
    Write-Host "  - Proceed with compilation and deployment" -ForegroundColor White
} else {
    Write-Host "[ERROR] Unit tests failed (exit code: $testResult)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Test execution duration: $([math]::Round($testDuration, 2)) seconds" -ForegroundColor Gray
    Write-Host "Code formatting duration: $([math]::Round($spotlessDuration, 2)) seconds" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Troubleshooting:" -ForegroundColor Yellow
    Write-Host "  - Check test output above for specific failures" -ForegroundColor White
    Write-Host "  - Review test reports in target/surefire-reports/" -ForegroundColor White
    Write-Host "  - Verify all dependencies are available" -ForegroundColor White
    Write-Host "  - Check for compilation errors" -ForegroundColor White
    Write-Host ""
    Write-Host "Fix the failing tests before proceeding with deployment." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Press any key to continue..." -ForegroundColor Gray
