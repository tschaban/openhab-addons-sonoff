# PowerShell Scripts Usage Guide

Comprehensive guide to the enhanced PowerShell scripts for development, testing, and deployment of the Sonoff Binding Smart'nyDom Enhanced Edition.

## 🎯 Overview

The project includes two powerful PowerShell scripts that provide professional development workflow:
- **task-run-unit-tests.ps1** (115 lines) - Enhanced test runner with detailed progress tracking
- **task-run-deploy.ps1** (267 lines) - Complete CI/CD pipeline with quality gates

These scripts mirror GitHub Actions functionality locally and provide immediate feedback during development.

## 🧪 task-run-unit-tests.ps1

### Purpose
Standalone test execution with comprehensive progress tracking, error handling, and performance metrics.

### Features
- **4-step progress tracking** with colored output
- **Automatic code formatting** with Spotless
- **Comprehensive error handling** with troubleshooting guidance
- **Execution timing** and performance metrics
- **Success/failure summaries** with next steps

### Usage
```powershell
# Navigate to project root
cd path\to\sonoff-binding

# Run the script
.\task-run-unit-tests.ps1
```

### Workflow Steps

#### Step 1: Navigate to Project Directory
```powershell
[STEP 1/4] Navigating to project directory...
Target directory: D:\Adrian\Projekty\openhab\openhab-addons\bundles\org.openhab.binding.sonoff
```

**What it does:**
- Validates project directory exists
- Changes to correct working directory
- Provides clear error messages if path is invalid

**Error handling:**
```powershell
if (Test-Path $projectPath) {
    cd $projectPath
    Write-Host "[OK] Successfully changed to project directory" -ForegroundColor Green
} else {
    Write-Host "[ERROR] Project directory not found: $projectPath" -ForegroundColor Red
    Write-Host "Please verify the path exists and try again." -ForegroundColor Yellow
    pause
    exit 1
}
```

#### Step 2: Clear Console
```powershell
[STEP 2/4] Clearing console for clean output...
```

**What it does:**
- Clears console for clean visual output
- Redisplays header information
- Shows current project context

#### Step 3: Apply Code Formatting
```powershell
[STEP 3/4] Applying code formatting with Spotless...
Running: mvn spotless:apply
```

**What it does:**
- Enforces OpenHAB coding standards
- Automatically fixes formatting issues
- Measures execution time
- Provides detailed error analysis

**Timing and error handling:**
```powershell
$spotlessStart = Get-Date
mvn spotless:apply
$spotlessResult = $LASTEXITCODE
$spotlessEnd = Get-Date
$spotlessDuration = ($spotlessEnd - $spotlessStart).TotalSeconds

if ($spotlessResult -eq 0) {
    Write-Host "[OK] Code formatting completed successfully" -ForegroundColor Green
    Write-Host "Duration: $([math]::Round($spotlessDuration, 2)) seconds" -ForegroundColor Gray
} else {
    Write-Host "[ERROR] Code formatting failed (exit code: $spotlessResult)" -ForegroundColor Red
    # Detailed troubleshooting guidance provided
}
```

#### Step 4: Run Unit Tests
```powershell
[STEP 4/4] Running unit tests...
Running: mvn test
```

**What it does:**
- Executes comprehensive test suite (100+ methods)
- Compiles source and test classes
- Generates test reports and coverage data
- Shows test results and coverage

**Detailed execution info:**
```powershell
Write-Host "This will:" -ForegroundColor Gray
Write-Host "  - Compile all source and test classes" -ForegroundColor Gray
Write-Host "  - Execute JUnit 5 test suites" -ForegroundColor Gray
Write-Host "  - Generate test reports" -ForegroundColor Gray
Write-Host "  - Show test results and coverage" -ForegroundColor Gray
```

### Success Summary
```powershell
[SUCCESS] All unit tests passed!

Test execution completed in 45.67 seconds
Code formatting duration: 3.21 seconds
Total execution time: 48.88 seconds

Next steps:
  - Review test reports in target/surefire-reports/
  - Check code coverage if configured
  - Proceed with compilation and deployment
```

### Failure Analysis
```powershell
[ERROR] Unit tests failed (exit code: 1)

Test execution duration: 23.45 seconds
Code formatting duration: 3.21 seconds

Troubleshooting:
  - Check test output above for specific failures
  - Review test reports in target/surefire-reports/
  - Verify all dependencies are available
  - Check for compilation errors

Fix the failing tests before proceeding with deployment.
```

## 🚀 task-run-deploy.ps1

### Purpose
Complete CI/CD pipeline with testing, compilation, and deployment to OpenHAB with comprehensive quality gates.

### Features
- **7-step CI/CD workflow** with quality gates
- **Unit test execution** as mandatory step 4
- **Deployment abortion** if any tests fail
- **JAR file validation** and deployment verification
- **OpenHAB log clearing** for clean monitoring

### Usage
```powershell
# Navigate to project root
cd path\to\sonoff-binding

# Run the complete deployment pipeline
.\task-run-deploy.ps1
```

### Configuration
The script uses these configurable paths:
```powershell
$projectPath = "D:\Adrian\Projekty\openhab\openhab-addons\bundles\org.openhab.binding.sonoff"
$jarSourcePath = "D:\Adrian\Projekty\openhab\openhab-addons\bundles\org.openhab.binding.sonoff\target\org.openhab.binding.sonoff-5.0.2-SNAPSHOT.jar"
$jarDestPath = "O:\configuration\addons\org.openhab.binding.sonoff-5.0.2-SmartnyDom-v0.x.jar"
$logPath = "O:\configuration\logs\openhab.log"
```

### Workflow Steps

#### Step 1: Navigate to Project Directory
```powershell
[STEP 1/7] Navigating to project directory...
Target directory: D:\Adrian\Projekty\openhab\openhab-addons\bundles\org.openhab.binding.sonoff
```

**Same as task-run-unit-tests.ps1 Step 1**

#### Step 2: Clear Console
```powershell
[STEP 2/7] Clearing console for clean output...
```

**Enhanced with configuration display:**
```powershell
Configuration:
  Project: D:\Adrian\Projekty\openhab\openhab-addons\bundles\org.openhab.binding.sonoff
  JAR Source: ...\target\org.openhab.binding.sonoff-5.0.2-SNAPSHOT.jar
  JAR Destination: O:\configuration\addons\org.openhab.binding.sonoff-5.0.2-SmartnyDom-v0.x.jar
  Log File: O:\configuration\logs\openhab.log
```

#### Step 3: Apply Code Formatting
```powershell
[STEP 3/7] Applying code formatting with Spotless...
This ensures code follows OpenHAB style guidelines
```

**Same as task-run-unit-tests.ps1 Step 3**

#### Step 4: Run Unit Tests (Quality Gate)
```powershell
[STEP 4/7] Running unit tests...
```

**Critical quality gate:**
```powershell
if ($testResult -eq 0) {
    Write-Host "[OK] All unit tests passed successfully" -ForegroundColor Green
    Write-Host "Code quality validated - proceeding with deployment" -ForegroundColor Gray
} else {
    Write-Host "[ERROR] Unit tests failed (exit code: $testResult)" -ForegroundColor Red
    Write-Host "DEPLOYMENT ABORTED - Tests must pass before deployment" -ForegroundColor Red
    # Detailed troubleshooting provided
    exit $testResult
}
```

**What it validates:**
- All source and test classes compile
- JUnit 5 test suites execute successfully
- Code functionality before deployment
- Test reports and coverage generation
- Binding quality and reliability

#### Step 5: Clean and Compile Binding
```powershell
[STEP 5/7] Compiling Sonoff binding...
Running: mvn clean install -DskipChecks -DskipTests -pl :org.openhab.binding.sonoff
```

**What it does:**
- Cleans previous build artifacts
- Compiles source code
- Packages into JAR file
- Skips tests and checks for faster build
- Builds only the Sonoff binding module

**JAR validation:**
```powershell
if (Test-Path $jarSourcePath) {
    $jarInfo = Get-Item $jarSourcePath
    Write-Host "JAR file created: $($jarInfo.Name)" -ForegroundColor Green
    Write-Host "Size: $([math]::Round($jarInfo.Length / 1KB, 2)) KB" -ForegroundColor Gray
    Write-Host "Created: $($jarInfo.CreationTime)" -ForegroundColor Gray
}
```

#### Step 6: Deploy JAR to OpenHAB
```powershell
[STEP 6/7] Deploying JAR to OpenHAB...
Source: D:\...\target\org.openhab.binding.sonoff-5.0.2-SNAPSHOT.jar
Destination: O:\configuration\addons\org.openhab.binding.sonoff-5.0.2-SmartnyDom-v0.x.jar
```

**Deployment process:**
1. **Check destination directory** - Create if needed
2. **Remove existing JAR** - Clean deployment
3. **Copy new JAR** - Deploy latest version
4. **Verify deployment** - Confirm successful copy

**Error handling:**
```powershell
try {
    # Deployment logic
    Write-Host "[OK] JAR deployed successfully" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Failed to deploy JAR: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Possible causes:" -ForegroundColor Yellow
    Write-Host "  - OpenHAB is running and has the file locked" -ForegroundColor White
    Write-Host "  - Insufficient permissions to write to destination" -ForegroundColor White
    Write-Host "  - Network drive not accessible" -ForegroundColor White
}
```

#### Step 7: Clear OpenHAB Log
```powershell
[STEP 7/7] Clearing OpenHAB log file...
Log file: O:\configuration\logs\openhab.log
```

**What it does:**
- Clears existing log entries
- Prepares clean log for monitoring
- Enables easy tracking of binding startup
- Provides clean debugging environment

### Deployment Summary
```powershell
========================================
         DEPLOYMENT SUMMARY
========================================

[SUCCESS] Sonoff binding deployment completed!

Execution Summary:
  Code formatting: 3.21 seconds
  Unit tests: 45.67 seconds
  Compilation: 12.34 seconds
  Total time: 61.22 seconds

Quality Assurance:
  - Code formatting: PASSED
  - Unit tests: PASSED
  - Compilation: PASSED
  - Deployment: COMPLETED

Next steps:
  1. Restart OpenHAB to load the new binding
  2. Check OpenHAB logs for binding startup messages
  3. Verify Sonoff devices are discovered properly
  4. Test binding functionality with your devices
  5. Monitor logs for any runtime issues

Files updated:
  - Binding JAR: O:\configuration\addons\org.openhab.binding.sonoff-5.0.2-SmartnyDom-v0.x.jar
  - Log file: O:\configuration\logs\openhab.log (cleared)
```

## 🔧 Script Customization

### Modifying Paths
Edit the configuration variables at the top of `task-run-deploy.ps1`:

```powershell
# Customize these paths for your environment
$projectPath = "C:\Your\Path\To\sonoff-binding"
$jarSourcePath = "C:\Your\Path\To\target\org.openhab.binding.sonoff-*.jar"
$jarDestPath = "C:\OpenHAB\addons\org.openhab.binding.sonoff-custom.jar"
$logPath = "C:\OpenHAB\logs\openhab.log"
```

### Adding Custom Steps
```powershell
# Add after existing steps
Write-Host "[STEP 8/8] Custom deployment step..." -ForegroundColor Yellow
# Your custom logic here
```

### Modifying Output Colors
```powershell
# Available colors: Black, DarkBlue, DarkGreen, DarkCyan, DarkRed, DarkMagenta, 
# DarkYellow, Gray, DarkGray, Blue, Green, Cyan, Red, Magenta, Yellow, White

Write-Host "Custom message" -ForegroundColor Blue
Write-Host "Success message" -ForegroundColor Green
Write-Host "Warning message" -ForegroundColor Yellow
Write-Host "Error message" -ForegroundColor Red
```

## 🛠️ Prerequisites and Setup

### PowerShell Requirements
- **PowerShell 5.1+** (Windows PowerShell) or **PowerShell 7+** (PowerShell Core)
- **Execution Policy** set to allow script execution

### Setting Execution Policy
```powershell
# For current user (recommended)
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser

# For all users (requires admin)
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope LocalMachine
```

### Required Software
- **Java 17+** in PATH
- **Maven 3.6+** in PATH
- **Git** for version control

### Verification Commands
```powershell
# Check PowerShell version
$PSVersionTable.PSVersion

# Check execution policy
Get-ExecutionPolicy

# Verify Java
java -version

# Verify Maven
mvn -version
```

## 🚀 Integration with Development Workflow

### Daily Development Cycle
```powershell
# 1. Start development session
.\task-run-unit-tests.ps1

# 2. Make code changes
# ... edit files ...

# 3. Test changes
.\task-run-unit-tests.ps1

# 4. Deploy when ready
.\task-run-deploy.ps1
```

### Pre-Commit Workflow
```powershell
# Before committing changes
.\task-run-unit-tests.ps1

# If tests pass, commit
git add .
git commit -m "feat: add new device support"

# Deploy to test environment
.\task-run-deploy.ps1
```

### Release Preparation
```powershell
# Final validation before release
.\task-run-deploy.ps1

# If deployment succeeds, create release
git tag v1.0.0
git push origin v1.0.0
```

## 📊 Performance and Timing

### Typical Execution Times
- **Code formatting:** 2-5 seconds
- **Unit tests:** 30-60 seconds (depending on system)
- **Compilation:** 10-20 seconds
- **Deployment:** 1-3 seconds
- **Total (task-run-deploy.ps1):** 45-90 seconds

### Performance Tips
- **Use SSD storage** for faster Maven operations
- **Increase Maven memory:** `$env:MAVEN_OPTS="-Xmx2048m"`
- **Use Maven offline mode** when dependencies are cached: `mvn -o test`
- **Close unnecessary applications** during testing

## 🛠️ Troubleshooting

### Common Issues

#### Script Won't Run
```powershell
# Check execution policy
Get-ExecutionPolicy

# Set if needed
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

#### Path Not Found Errors
- **Verify project path** in script configuration
- **Check Java and Maven** are in PATH
- **Ensure OpenHAB paths** are accessible

#### Permission Denied
- **Run PowerShell as Administrator** if needed
- **Check file permissions** on target directories
- **Verify network drive access** for OpenHAB deployment

#### Maven Errors
```powershell
# Clear Maven cache
Remove-Item -Recurse -Force ~/.m2/repository

# Verify Maven settings
mvn help:effective-settings

# Check for proxy issues
mvn help:system
```

### Getting Help

#### Script-Specific Issues
- **Review script output** for detailed error messages
- **Check timing information** for performance issues
- **Verify all prerequisites** are installed

#### General Development Issues
- **GitHub Issues** for bug reports
- **Documentation** in docs/ directory
- **OpenHAB Community** for binding-specific questions

## 🎯 Best Practices

### Script Usage
- **Run tests frequently** during development
- **Use deployment script** for final validation
- **Monitor execution times** for performance regression
- **Keep scripts updated** with project changes

### Development Workflow
- **Test before committing** to avoid broken builds
- **Use quality gates** to maintain code quality
- **Monitor OpenHAB logs** after deployment
- **Validate device functionality** after updates

### Maintenance
- **Update paths** when project structure changes
- **Customize output** for team preferences
- **Add custom steps** for specific requirements
- **Document modifications** for team knowledge

These PowerShell scripts provide a professional development experience with comprehensive feedback, quality gates, and automation that mirrors enterprise CI/CD practices.