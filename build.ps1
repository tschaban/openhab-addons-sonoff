# Sonoff Binding Build Script
# Unified build script with multiple execution modes
#
# Usage:
#   .\build.ps1 [mode]
#
# Modes:
#   deploy - Full pipeline: format, test, compile, and deploy (default)
#   test   - Format and test only (no deployment)
#   fast   - Fast pipeline: format, compile, and deploy (skip tests)
#
# Examples:
#   .\build.ps1           # Full deployment with tests
#   .\build.ps1 deploy    # Same as above
#   .\build.ps1 test      # Run tests only
#   .\build.ps1 fast      # Fast deployment without tests

param(
    [Parameter(Position=0)]
    [ValidateSet("deploy", "test", "fast")]
    [string]$Mode = "deploy"
)

# Load configuration
$scriptDir = Split-Path -Parent $PSCommandPath

# Load default configuration
$defaultConfigPath = Join-Path $scriptDir "config.ps1"
if (Test-Path $defaultConfigPath) {
    . $defaultConfigPath
    Write-Host "[CONFIG] Loaded default configuration from config.ps1" -ForegroundColor Gray
} else {
    Write-Host "[WARNING] Default config.ps1 not found!" -ForegroundColor Yellow
    Write-Host "Expected location: $defaultConfigPath" -ForegroundColor Gray
}

# Load custom configurations from conf.d/ directory
$confDir = Join-Path $scriptDir "conf.d"
if (Test-Path $confDir) {
    $customConfigs = Get-ChildItem -Path $confDir -Filter "*.ps1" | Sort-Object Name
    
    if ($customConfigs.Count -gt 0) {
        Write-Host "[CONFIG] Loading custom configurations from conf.d/" -ForegroundColor Gray
        foreach ($configFile in $customConfigs) {
            Write-Host "  - Loading: $($configFile.Name)" -ForegroundColor Gray
            . $configFile.FullName
        }
    } else {
        Write-Host "[CONFIG] No custom configurations found in conf.d/" -ForegroundColor Gray
    }
} else {
    Write-Host "[CONFIG] conf.d/ directory not found, using default configuration only" -ForegroundColor Gray
}

# Calculate derived paths
$jarSourcePath = Join-Path $projectPath "target\$sourceJAR"
$jarDestPath = Join-Path $openhabAddonsPath $targetJAR

# Display final configuration
Write-Host ""
Write-Host "Active Configuration:" -ForegroundColor Cyan
Write-Host "  Project Path: $projectPath" -ForegroundColor Gray
Write-Host "  JAR Source: $jarSourcePath" -ForegroundColor Gray
Write-Host "  OpenHAB Addons: $openhabAddonsPath" -ForegroundColor Gray
Write-Host "  JAR Destination: $jarDestPath" -ForegroundColor Gray
Write-Host "  Log File: $logPath" -ForegroundColor Gray
Write-Host "  Reset Log: $resetOpenhabLog" -ForegroundColor Gray
Write-Host ""

# Determine execution mode
$runTests = $true
$runDeploy = $true
$totalSteps = 7

switch ($Mode.ToLower()) {
    "deploy" {
        $pipelineName = "Full CI/CD Pipeline"
        $runTests = $true
        $runDeploy = $true
        $totalSteps = 7
    }
    "test" {
        $pipelineName = "Test Pipeline"
        $runTests = $true
        $runDeploy = $false
        $totalSteps = 4
    }
    "fast" {
        $pipelineName = "Fast Deployment Pipeline"
        $runTests = $false
        $runDeploy = $true
        $totalSteps = 6
    }
}

# Step 1: Clear console for clean output
clear
Write-Host "[STEP 1/$totalSteps] Clearing console for clean output..." -ForegroundColor Yellow

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Sonoff Binding $pipelineName" -ForegroundColor Cyan
if ($Mode -eq "fast") {
    Write-Host "  [*] UNIT TESTS SKIPPED [*]" -ForegroundColor Red
}
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Display mode information
Write-Host "Execution Mode: $Mode" -ForegroundColor Magenta
if ($Mode -eq "fast") {
    Write-Host "[!] WARNING: This is a FAST pipeline for development" -ForegroundColor Yellow
    Write-Host "[!] Unit tests are SKIPPED for faster iteration" -ForegroundColor Yellow
    Write-Host "[!] Use 'deploy' mode for full validation before committing" -ForegroundColor Yellow
} elseif ($Mode -eq "test") {
    Write-Host "[i] Running tests only - no deployment will be performed" -ForegroundColor Cyan
} else {
    Write-Host "[+] Full pipeline with all quality checks enabled" -ForegroundColor Green
}
Write-Host ""

Write-Host "Configuration:" -ForegroundColor Gray
Write-Host "  Project: $projectPath" -ForegroundColor Gray
Write-Host "  JAR Source: $jarSourcePath" -ForegroundColor Gray
if ($runDeploy) {
    Write-Host "  JAR Destination: $jarDestPath" -ForegroundColor Gray
    Write-Host "  Log File: $logPath" -ForegroundColor Gray
}
Write-Host ""

# Step 2: Navigate to project directory
$currentStep = 2
Write-Host "[STEP $currentStep/$totalSteps] Navigating to project directory..." -ForegroundColor Yellow
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
Write-Host "Project: $(Split-Path -Leaf (Get-Location))" -ForegroundColor Gray
Write-Host "Location: $(Get-Location)" -ForegroundColor Gray
Write-Host ""

# Step 3: Apply code formatting
$currentStep = 3
Write-Host "[STEP $currentStep/$totalSteps] Applying code formatting with Spotless..." -ForegroundColor Yellow
Write-Host "Running: mvn spotless:apply" -ForegroundColor Gray
Write-Host "This ensures code follows OpenHAB style guidelines" -ForegroundColor Gray
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

# Step 4: Run unit tests (conditional)
$currentStep = 4
$testDuration = 0

if ($runTests) {
    Write-Host "[STEP $currentStep/$totalSteps] Running unit tests..." -ForegroundColor Yellow
    Write-Host "Running: mvn test" -ForegroundColor Gray
    Write-Host ""
    Write-Host "This will:" -ForegroundColor Gray
    Write-Host "  - Compile all source and test classes" -ForegroundColor Gray
    Write-Host "  - Execute JUnit 5 test suites" -ForegroundColor Gray
    Write-Host "  - Validate code functionality" -ForegroundColor Gray
    Write-Host "  - Generate test reports and coverage data" -ForegroundColor Gray
    if ($runDeploy) {
        Write-Host "  - Ensure binding quality before deployment" -ForegroundColor Gray
    }
    Write-Host ""

    $testStart = Get-Date
    mvn test
    $testResult = $LASTEXITCODE
    $testEnd = Get-Date
    $testDuration = ($testEnd - $testStart).TotalSeconds

    Write-Host ""
    if ($testResult -eq 0) {
        Write-Host "[OK] All unit tests passed successfully" -ForegroundColor Green
        Write-Host "Duration: $([math]::Round($testDuration, 2)) seconds" -ForegroundColor Gray
        if ($runDeploy) {
            Write-Host "Code quality validated - proceeding with deployment" -ForegroundColor Gray
        }
    } else {
        Write-Host "[ERROR] Unit tests failed (exit code: $testResult)" -ForegroundColor Red
        Write-Host "Duration: $([math]::Round($testDuration, 2)) seconds" -ForegroundColor Gray
        Write-Host ""
        if ($runDeploy) {
            Write-Host "DEPLOYMENT ABORTED - Tests must pass before deployment" -ForegroundColor Red
            Write-Host ""
        }
        Write-Host "Troubleshooting:" -ForegroundColor Yellow
        Write-Host "  - Check test output above for specific failures" -ForegroundColor White
        Write-Host "  - Review test reports in target/surefire-reports/" -ForegroundColor White
        Write-Host "  - Fix failing tests before attempting deployment" -ForegroundColor White
        Write-Host "  - Verify all dependencies are available" -ForegroundColor White
        Write-Host "  - Check for compilation errors in test classes" -ForegroundColor White
        Write-Host ""
        pause
        exit $testResult
    }
    Write-Host ""
} else {
    Write-Host "[STEP $currentStep/$totalSteps] Unit tests: SKIPPED [*]" -ForegroundColor Magenta
    Write-Host "[!] Tests are skipped for faster development iteration" -ForegroundColor Yellow
    Write-Host "[!] Run with 'deploy' mode before committing changes" -ForegroundColor Yellow
    Write-Host ""
}

# Exit here if test-only mode
if ($Mode -eq "test") {
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "        TEST EXECUTION SUMMARY" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "[SUCCESS] All tests completed!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Execution Summary:" -ForegroundColor Cyan
    Write-Host "  Code formatting: $([math]::Round($spotlessDuration, 2)) seconds" -ForegroundColor Gray
    Write-Host "  Unit tests: $([math]::Round($testDuration, 2)) seconds" -ForegroundColor Gray
    Write-Host "  Total time: $([math]::Round($spotlessDuration + $testDuration, 2)) seconds" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Quality Assurance:" -ForegroundColor Cyan
    Write-Host "  - Code formatting: PASSED" -ForegroundColor Green
    Write-Host "  - Unit tests: PASSED" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Cyan
    Write-Host "  - Review test reports in target/surefire-reports/" -ForegroundColor White
    Write-Host "  - Check code coverage if configured" -ForegroundColor White
    Write-Host "  - Run '.\build.ps1 deploy' to compile and deploy" -ForegroundColor White
    Write-Host ""
    Write-Host "Press any key to continue..." -ForegroundColor Gray
    exit 0
}

# Step 5: Clean and compile the binding
$currentStep = 5
Write-Host "[STEP $currentStep/$totalSteps] Compiling Sonoff binding..." -ForegroundColor Yellow
Write-Host "Running: mvn clean install -DskipChecks -DskipTests -pl :org.openhab.binding.sonoff" -ForegroundColor Gray
Write-Host ""
Write-Host "This will:" -ForegroundColor Gray
Write-Host "  - Clean previous build artifacts" -ForegroundColor Gray
Write-Host "  - Compile source code" -ForegroundColor Gray
Write-Host "  - Package into JAR file" -ForegroundColor Gray
Write-Host "  - Skip tests and checks for faster build" -ForegroundColor Gray
Write-Host "  - Build only the Sonoff binding module" -ForegroundColor Gray
Write-Host ""

$compileStart = Get-Date
mvn clean install -DskipChecks -DskipTests -pl :org.openhab.binding.sonoff
$compileResult = $LASTEXITCODE
$compileEnd = Get-Date
$compileDuration = ($compileEnd - $compileStart).TotalSeconds

Write-Host ""
if ($compileResult -eq 0) {
    Write-Host "[OK] Compilation completed successfully" -ForegroundColor Green
    Write-Host "Duration: $([math]::Round($compileDuration, 2)) seconds" -ForegroundColor Gray
    
    # Verify JAR file was created
    if (Test-Path $jarSourcePath) {
        $jarInfo = Get-Item $jarSourcePath
        Write-Host "JAR file created: $($jarInfo.Name)" -ForegroundColor Green
        Write-Host "Size: $([math]::Round($jarInfo.Length / 1KB, 2)) KB" -ForegroundColor Gray
        Write-Host "Created: $($jarInfo.CreationTime)" -ForegroundColor Gray
    } else {
        Write-Host "[WARNING] JAR file not found at expected location" -ForegroundColor Yellow
        Write-Host "Expected: $jarSourcePath" -ForegroundColor Gray
    }
} else {
    Write-Host "[ERROR] Compilation failed (exit code: $compileResult)" -ForegroundColor Red
    Write-Host "Duration: $([math]::Round($compileDuration, 2)) seconds" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Check the output above for compilation errors." -ForegroundColor Yellow
    pause
    exit $compileResult
}

Write-Host ""

# Step 6: Deploy JAR to OpenHAB
$currentStep = 6
Write-Host "[STEP $currentStep/$totalSteps] Deploying JAR to OpenHAB..." -ForegroundColor Yellow
Write-Host "Source: $jarSourcePath" -ForegroundColor Gray
Write-Host "Destination: $jarDestPath" -ForegroundColor Gray

if (Test-Path $jarSourcePath) {
    try {
        # Check if destination directory exists
        $destDir = Split-Path $jarDestPath -Parent
        if (!(Test-Path $destDir)) {
            Write-Host "[INFO] Creating destination directory: $destDir" -ForegroundColor Cyan
            New-Item -ItemType Directory -Path $destDir -Force | Out-Null
        }
        
        # Check if old JAR exists and remove it
        if (Test-Path $jarDestPath) {
            Write-Host "[INFO] Removing existing JAR file" -ForegroundColor Cyan
            Remove-Item $jarDestPath -Force
        }
        
        # Copy new JAR
        Copy-Item -Path $jarSourcePath -Destination $jarDestPath -Force
        
        if (Test-Path $jarDestPath) {
            $deployedJar = Get-Item $jarDestPath
            Write-Host "[OK] JAR deployed successfully" -ForegroundColor Green
            Write-Host "Deployed file: $($deployedJar.Name)" -ForegroundColor Gray
            Write-Host "Size: $([math]::Round($deployedJar.Length / 1KB, 2)) KB" -ForegroundColor Gray
            Write-Host "Deployed at: $($deployedJar.CreationTime)" -ForegroundColor Gray
        } else {
            throw "JAR file not found at destination after copy"
        }
    } catch {
        Write-Host "[ERROR] Failed to deploy JAR: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host ""
        Write-Host "Possible causes:" -ForegroundColor Yellow
        Write-Host "  - OpenHAB is running and has the file locked" -ForegroundColor White
        Write-Host "  - Insufficient permissions to write to destination" -ForegroundColor White
        Write-Host "  - Network drive not accessible" -ForegroundColor White
        pause
        exit 1
    }
} else {
    Write-Host "[ERROR] Source JAR file not found: $jarSourcePath" -ForegroundColor Red
    Write-Host "Compilation may have failed or JAR was not created." -ForegroundColor Yellow
    pause
    exit 1
}

Write-Host ""

# Step 7: Clear OpenHAB log
$currentStep = 7
Write-Host "[STEP $currentStep/$totalSteps] Clearing OpenHAB log file..." -ForegroundColor Yellow
Write-Host "Log file: $logPath" -ForegroundColor Gray

if ($resetOpenhabLog) {
    try {
        if (Test-Path $logPath) {
            Set-Content -Path $logPath -Value ""
            Write-Host "[OK] OpenHAB log cleared successfully" -ForegroundColor Green
            Write-Host "Log file reset for clean monitoring of binding startup" -ForegroundColor Gray
        } else {
            Write-Host "[WARNING] Log file not found: $logPath" -ForegroundColor Yellow
            Write-Host "This may be normal if OpenHAB hasn't created the log yet" -ForegroundColor Gray
        }
    } catch {
        Write-Host "[WARNING] Failed to clear log file: $($_.Exception.Message)" -ForegroundColor Yellow
        Write-Host "This won't prevent the binding from working" -ForegroundColor Gray
    }
} else {
    Write-Host "[INFO] Log reset is disabled (resetOpenhabLog = false)" -ForegroundColor Cyan
    Write-Host "Log file will not be cleared" -ForegroundColor Gray
}

Write-Host ""

# Final Summary
Write-Host "========================================" -ForegroundColor Cyan
if ($Mode -eq "fast") {
    Write-Host "      FAST DEPLOYMENT SUMMARY" -ForegroundColor Cyan
} else {
    Write-Host "         DEPLOYMENT SUMMARY" -ForegroundColor Cyan
}
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "[SUCCESS] Sonoff binding $Mode completed!" -ForegroundColor Green
Write-Host ""
Write-Host "Execution Summary:" -ForegroundColor Cyan
Write-Host "  Mode: $Mode" -ForegroundColor Gray
Write-Host "  Code formatting: $([math]::Round($spotlessDuration, 2)) seconds" -ForegroundColor Gray
if ($runTests) {
    Write-Host "  Unit tests: $([math]::Round($testDuration, 2)) seconds" -ForegroundColor Gray
} else {
    Write-Host "  Unit tests: SKIPPED [*]" -ForegroundColor Magenta
}
Write-Host "  Compilation: $([math]::Round($compileDuration, 2)) seconds" -ForegroundColor Gray
$totalTime = $spotlessDuration + $testDuration + $compileDuration
Write-Host "  Total time: $([math]::Round($totalTime, 2)) seconds" -ForegroundColor Gray
Write-Host ""
Write-Host "Quality Assurance:" -ForegroundColor Cyan
Write-Host "  - Code formatting: PASSED" -ForegroundColor Green
if ($runTests) {
    Write-Host "  - Unit tests: PASSED" -ForegroundColor Green
} else {
    Write-Host "  - Unit tests: SKIPPED [*]" -ForegroundColor Magenta
}
Write-Host "  - Compilation: PASSED" -ForegroundColor Green
Write-Host "  - Deployment: COMPLETED" -ForegroundColor Green
Write-Host ""

if ($Mode -eq "fast") {
    Write-Host "[!] IMPORTANT REMINDERS:" -ForegroundColor Yellow
    Write-Host "  - This was a FAST pipeline for development iteration" -ForegroundColor White
    Write-Host "  - Unit tests were SKIPPED to save time" -ForegroundColor White
    Write-Host "  - Run '.\build.ps1 deploy' before:" -ForegroundColor White
    Write-Host "    * Committing changes to repository" -ForegroundColor White
    Write-Host "    * Creating pull requests" -ForegroundColor White
    Write-Host "    * Deploying to production" -ForegroundColor White
    Write-Host ""
}

Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "  1. Restart OpenHAB to load the new binding" -ForegroundColor White
Write-Host "  2. Check OpenHAB logs for binding startup messages" -ForegroundColor White
Write-Host "  3. Verify Sonoff devices are discovered properly" -ForegroundColor White
Write-Host "  4. Test binding functionality with your devices" -ForegroundColor White
if ($Mode -eq "fast") {
    Write-Host "  5. Run '.\build.ps1 deploy' when ready to commit" -ForegroundColor White
} else {
    Write-Host "  5. Monitor logs for any runtime issues" -ForegroundColor White
}
Write-Host ""
Write-Host "Files updated:" -ForegroundColor Cyan
Write-Host "  - Binding JAR: $jarDestPath" -ForegroundColor White
if ($resetOpenhabLog) {
    Write-Host "  - Log file: $logPath (cleared)" -ForegroundColor White
} else {
    Write-Host "  - Log file: $logPath (not cleared)" -ForegroundColor White
}
Write-Host ""

# Show available modes
Write-Host "Available build modes:" -ForegroundColor Cyan
Write-Host "  .\build.ps1 deploy  - Full pipeline with tests (recommended)" -ForegroundColor Gray
Write-Host "  .\build.ps1 test    - Run tests only (no deployment)" -ForegroundColor Gray
Write-Host "  .\build.ps1 fast    - Fast deployment without tests" -ForegroundColor Gray
Write-Host ""
Write-Host "Press any key to continue..." -ForegroundColor Gray
