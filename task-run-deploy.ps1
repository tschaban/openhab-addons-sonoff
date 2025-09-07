
# Sonoff Binding Compile and Upload Script
# Compiles the Sonoff binding and deploys it to OpenHAB

# Configuration
$projectPath = "D:\Adrian\Projekty\openhab\openhab-addons\bundles\org.openhab.binding.sonoff"
$jarSourcePath = "D:\Adrian\Projekty\openhab\openhab-addons\bundles\org.openhab.binding.sonoff\target\org.openhab.binding.sonoff-5.0.2-SNAPSHOT.jar"
$jarDestPath = "O:\configuration\addons\org.openhab.binding.sonoff-5.0.2-SmartnyDom-v0.x.jar"
$logPath = "O:\configuration\logs\openhab.log"

# Step 1: Clear console for clean output
clear
Write-Host "[STEP 1/6] Clearing console for clean output..." -ForegroundColor Yellow

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Sonoff Binding Compile & Upload" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""


Write-Host "Configuration:" -ForegroundColor Gray
Write-Host "  Project: $projectPath" -ForegroundColor Gray
Write-Host "  JAR Source: $jarSourcePath" -ForegroundColor Gray
Write-Host "  JAR Destination: $jarDestPath" -ForegroundColor Gray
Write-Host "  Log File: $logPath" -ForegroundColor Gray
Write-Host ""

# Step 2: Navigate to project directory
Write-Host "[STEP 2/6] Navigating to project directory..." -ForegroundColor Yellow
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
Write-Host "  Sonoff Binding Compile & Upload" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Project: $(Split-Path -Leaf (Get-Location))" -ForegroundColor Gray
Write-Host "Location: $(Get-Location)" -ForegroundColor Gray
Write-Host ""

# Step 3: Apply code formatting
Write-Host "[STEP 3/6] Applying code formatting with Spotless..." -ForegroundColor Yellow
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

# Step 4: Clean and compile the binding
Write-Host "[STEP 4/6] Compiling Sonoff binding..." -ForegroundColor Yellow
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

# Step 5: Deploy JAR to OpenHAB
Write-Host "[STEP 5/6] Deploying JAR to OpenHAB..." -ForegroundColor Yellow
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

# Step 6: Clear OpenHAB log
Write-Host "[STEP 6/6] Clearing OpenHAB log file..." -ForegroundColor Yellow
Write-Host "Log file: $logPath" -ForegroundColor Gray

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

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "         DEPLOYMENT SUMMARY" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "[SUCCESS] Sonoff binding deployment completed!" -ForegroundColor Green
Write-Host ""
Write-Host "Execution Summary:" -ForegroundColor Cyan
Write-Host "  Code formatting: $([math]::Round($spotlessDuration, 2)) seconds" -ForegroundColor Gray
Write-Host "  Compilation: $([math]::Round($compileDuration, 2)) seconds" -ForegroundColor Gray
Write-Host "  Total time: $([math]::Round($spotlessDuration + $compileDuration, 2)) seconds" -ForegroundColor Gray
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "  1. Restart OpenHAB to load the new binding" -ForegroundColor White
Write-Host "  2. Check OpenHAB logs for binding startup messages" -ForegroundColor White
Write-Host "  3. Verify Sonoff devices are discovered properly" -ForegroundColor White
Write-Host "  4. Test binding functionality with your devices" -ForegroundColor White
Write-Host ""
Write-Host "Files updated:" -ForegroundColor Cyan
Write-Host "  - Binding JAR: $jarDestPath" -ForegroundColor White
Write-Host "  - Log file: $logPath (cleared)" -ForegroundColor White
Write-Host ""
Write-Host "Press any key to continue..." -ForegroundColor Gray
pause