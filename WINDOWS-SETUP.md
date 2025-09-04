# Windows Setup Guide for JUnit 5 Testing

This guide helps Windows users set up and run the JUnit 5 testing framework.

## Prerequisites

### 1. Install Java 17+
- Download from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://openjdk.org/)
- Add Java to your system PATH
- Verify installation: `java -version`

### 2. Install Maven
- Download from [Apache Maven](https://maven.apache.org/download.cgi)
- Extract to a folder (e.g., `C:\Program Files\Apache\maven`)
- Add Maven `bin` directory to your system PATH
- Verify installation: `mvn -version`

### 3. Install Git (Optional)
- Download from [Git for Windows](https://git-scm.com/download/win)
- Required if cloning the repository

## Running Tests

### Option 1: PowerShell (Recommended)
1. Open PowerShell as Administrator (first time only)
2. Enable script execution: `Set-ExecutionPolicy RemoteSigned`
3. Navigate to project directory: `cd path\to\openhab-addons-sonoff`
4. Run verification: `.\verify-tests.ps1`

### Option 2: Command Prompt
1. Open Command Prompt
2. Navigate to project directory: `cd path\to\openhab-addons-sonoff`
3. Run verification: `verify-tests.bat`

### Option 3: Direct Maven Commands
```cmd
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=SimpleJUnitTest

# Compile tests only
mvn test-compile
```

## Troubleshooting

### Common Issues

**"java is not recognized as an internal or external command"**
- Java is not in your PATH
- Add Java installation directory to system PATH
- Restart Command Prompt/PowerShell

**"mvn is not recognized as an internal or external command"**
- Maven is not in your PATH
- Add Maven `bin` directory to system PATH
- Restart Command Prompt/PowerShell

**PowerShell execution policy error**
- Run PowerShell as Administrator
- Execute: `Set-ExecutionPolicy RemoteSigned`
- Choose `Y` when prompted

**Parent POM not found**
- This is expected outside the full OpenHAB environment
- Tests are still properly configured
- Will work when integrated into OpenHAB project

### Environment Variables
Add these to your system PATH:
```
C:\Program Files\Java\jdk-17\bin
C:\Program Files\Apache\maven\bin
```

### IDE Integration
Most IDEs (IntelliJ IDEA, Eclipse, VS Code) can run JUnit tests directly:
1. Right-click on test file
2. Select "Run Test" or "Run JUnit"
3. View results in IDE test runner

## Verification Script Features

The Windows scripts provide:
- ✅ Java installation check
- ✅ Maven installation check  
- ✅ Parent POM availability check
- ✅ Automatic test execution (when possible)
- ✅ Colored output for better readability
- ✅ Detailed error messages and suggestions

## Next Steps

Once verification passes:
1. Write your unit tests in `src/test/java/`
2. Follow the naming convention: `*Test.java`
3. Use JUnit 5 annotations and assertions
4. Run tests regularly during development

For more details, see `README-TESTING.md`.