
# Sonoff Binding Compile and Upload Script
# Compiles the Sonoff binding and deploys it to OpenHAB

# Configuration
$projectPath = "D:\Adrian\Projekty\openhab\openhab-addons\bundles\org.openhab.binding.sonoff"
$jarSourcePath = "D:\Adrian\Projekty\openhab\openhab-addons\bundles\org.openhab.binding.sonoff\target\org.openhab.binding.sonoff-5.0.2-SNAPSHOT.jar"
$jarDestPath = "O:\configuration\addons\org.openhab.binding.sonoff-5.0.2-SmartnyDom-v0.x.jar"
$logPath = "O:\configuration\logs\openhab.log"

# Step 1: Clear console for clean output
clear
Write-Host "[STEP 1/4] Clearing console for clean output..." -ForegroundColor Yellow

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
Write-Host "[STEP 2/4] Navigating to project directory..." -ForegroundColor Yellow
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
Write-Host "[STEP 3/4] Applying code formatting with Spotless..." -ForegroundColor Yellow
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
Write-Host "[STEP 4/4] Compiling Sonoff binding..." -ForegroundColor Yellow
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

