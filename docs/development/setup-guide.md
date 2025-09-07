# Development Setup Guide

Complete guide for setting up and working with the Sonoff Binding Smart'nyDom Enhanced Edition development environment.

## 🎯 Overview

This enhanced branch features:
- **100+ test methods** across 6 comprehensive test classes
- **Quality gates** that prevent deployment of failing code
- **Mock-based testing** for reliable unit tests
- **Integration testing** with real file system operations
- **Automated scripts** with detailed progress tracking and error handling
- **GitHub Actions CI/CD** for automated testing and releases

## 🛠️ Prerequisites

### Required Software
- **Java 17+** - Primary development and runtime environment
- **Maven 3.6+** - Build and dependency management
- **Git** - Version control and repository operations
- **PowerShell** (Windows) - For enhanced development scripts

### Recommended IDE Setup
- **Visual Studio Code** with Java extensions
- **IntelliJ IDEA** with Maven integration
- **Eclipse** with Maven and Git plugins

### Environment Verification
```bash
# Verify Java installation
java -version

# Verify Maven installation
mvn -version

# Verify Git installation
git --version
```

## 📁 Project Structure

```
sonoff-binding/
├── src/
│   ├── main/java/                 # Main source code
│   └── test/java/                 # Comprehensive test suite (2,026+ lines)
│       └── org/openhab/binding/sonoff/internal/
│           ├── SonoffCacheProviderTest.java              # Core cache functionality (436 lines)
│           ├── SonoffCacheProviderIntegrationTest.java   # Integration tests (413 lines)
│           ├── SonoffCacheProviderErrorHandlingTest.java # Error scenarios (396 lines)
│           ├── SonoffHandlerFactoryTest.java             # Handler factory tests (481 lines)
│           ├── SonoffHandlerFactoryIntegrationTest.java  # Factory integration (300 lines)
│           └── discovery/
│               └── SonoffDiscoveryServiceTest.java       # Discovery service tests
├── docs/
│   ├── development/               # Development documentation
│   └── testing/                   # Test-specific documentation
├── .github/
│   └── workflows/                 # GitHub Actions CI/CD
├── task-run-unit-tests.ps1        # Enhanced test runner
├── task-run-deploy.ps1            # Full CI/CD pipeline script
└── pom.xml                        # Maven configuration
```

## 🚀 Quick Start

### 1. Clone and Setup
```bash
# Clone the repository
git clone https://github.com/tschaban/openhab-addons-sonoff.git
cd openhab-addons-sonoff

# Verify setup
mvn validate
```

### 2. Run Tests (Recommended Method)
```powershell
# Windows PowerShell - Enhanced test runner with progress tracking
.\task-run-unit-tests.ps1
```

**Features of task-run-unit-tests.ps1:**
- 4-step progress tracking with colored output
- Automatic code formatting with Spotless
- Comprehensive error handling and troubleshooting
- Execution timing and performance metrics
- Success/failure summaries with next steps

### 3. Full Development Workflow
```powershell
# Windows PowerShell - Complete CI/CD pipeline
.\task-run-deploy.ps1
```

**7-step workflow:**
1. Navigate to project directory
2. Clear console for clean output
3. Apply code formatting (Spotless)
4. **Run unit tests** (Quality gate - deployment aborts if tests fail)
5. Clean and compile binding
6. Deploy JAR to OpenHAB
7. Clear OpenHAB logs

## 🧪 Testing Framework

### Test Categories

#### 1. Unit Tests
**Purpose:** Test individual components in isolation
- **SonoffCacheProviderTest.java** - Core cache functionality
- **SonoffHandlerFactoryTest.java** - Handler factory logic
- Mock external dependencies using Mockito
- Fast execution, no file system or network operations

#### 2. Integration Tests  
**Purpose:** Test component interactions and real operations
- **SonoffCacheProviderIntegrationTest.java** - Real file system operations
- **SonoffHandlerFactoryIntegrationTest.java** - Factory with real dependencies
- Test concurrent access and thread safety
- Performance and stress testing

#### 3. Error Handling Tests
**Purpose:** Test edge cases and error scenarios
- **SonoffCacheProviderErrorHandlingTest.java** - Exception handling
- Boundary conditions and invalid inputs
- Resource cleanup and recovery scenarios

#### 4. Discovery Tests
**Purpose:** Test device discovery functionality
- **SonoffDiscoveryServiceTest.java** - Discovery service logic
- Mock network operations and device responses

### Manual Test Execution

#### Run All Tests
```bash
mvn test
```

#### Component-Specific Testing
```bash
# Cache Provider Tests (50+ test methods)
mvn test -Dtest="SonoffCacheProviderTest"
mvn test -Dtest="SonoffCacheProviderIntegrationTest" 
mvn test -Dtest="SonoffCacheProviderErrorHandlingTest"

# Handler Factory Tests (40+ test methods)
mvn test -Dtest="SonoffHandlerFactoryTest"
mvn test -Dtest="SonoffHandlerFactoryIntegrationTest"

# Discovery Service Tests
mvn test -Dtest="SonoffDiscoveryServiceTest"

# Run test suites by pattern
mvn test -Dtest="*IntegrationTest"
mvn test -Dtest="*ErrorHandlingTest"
```

#### Advanced Testing Options
```bash
# Run with detailed output
mvn test -X

# Generate test reports
mvn test jacoco:report

# Run specific test method
mvn test -Dtest="SonoffCacheProviderTest#testConstructorWithGson"
```

## 🔧 Development Workflow

### Quality Assurance Pipeline

#### 1. Code Formatting (Automatic)
```bash
mvn spotless:apply
```
- Enforces OpenHAB coding standards
- Automatically fixes formatting issues
- Integrated into all test scripts

#### 2. Unit Testing (Quality Gate)
```bash
mvn test
```
- All tests must pass before deployment
- Comprehensive coverage of core functionality
- Mock-based testing for fast execution

#### 3. Integration Testing
- Real file system operations
- Concurrent access testing
- Performance validation

#### 4. Error Handling Validation
- Exception scenarios
- Edge case testing
- Resource cleanup verification

### Development Best Practices

#### Code Quality
- **Follow OpenHAB coding standards** - Enforced by Spotless
- **Write comprehensive tests** - Aim for high coverage
- **Use meaningful commit messages** - Follow conventional commits
- **Document complex logic** - Focus on "why" not "what"

#### Testing Guidelines
- **Unit tests first** - Test individual components in isolation
- **Mock external dependencies** - Use Mockito for reliable tests
- **Test error scenarios** - Include negative test cases
- **Verify resource cleanup** - Ensure proper cleanup in tests

#### Git Workflow
```bash
# Create feature branch
git checkout -b feature/new-device-support

# Make changes and test
.\task-run-unit-tests.ps1

# Commit with meaningful message
git commit -m "feat: add support for SONOFF T5-4C switch"

# Push and create PR
git push origin feature/new-device-support
```

## 🔄 Continuous Integration

### GitHub Actions Integration
The project includes comprehensive GitHub Actions workflows:

#### CI/CD Pipeline
- **Automatic testing** on every push/PR
- **Multiple Java versions** (17, 21)
- **Cross-platform testing** (Ubuntu, Windows, macOS)
- **Quality gates** preventing broken code merges

#### Release Pipeline
- **Automated releases** on git tags
- **JAR artifact creation** and publishing
- **Professional release notes** generation

### Local CI/CD Simulation
```powershell
# Simulate full CI/CD pipeline locally
.\task-run-deploy.ps1
```

This script mirrors the GitHub Actions workflow:
1. Code formatting validation
2. Comprehensive test execution
3. Build verification
4. Deployment preparation

## 🛠️ Troubleshooting

### Common Issues

#### Test Failures
```bash
# Check specific test output
mvn test -Dtest="FailingTestClass" -X

# Run tests with clean state
mvn clean test

# Check for dependency conflicts
mvn dependency:tree
```

#### Build Issues
```bash
# Clean and rebuild
mvn clean compile

# Verify dependencies
mvn dependency:resolve

# Check for formatting issues
mvn spotless:check
```

#### PowerShell Script Issues
- **Execution Policy:** `Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser`
- **Path Issues:** Verify Java and Maven are in PATH
- **Permissions:** Run PowerShell as Administrator if needed

### Getting Help

#### Documentation Resources
- **[Testing Framework](testing-framework.md)** - Comprehensive testing guide
- **[Component Tests](../testing/)** - Detailed test documentation
- **[GitHub Actions](../../.github/workflows/README.md)** - CI/CD workflow documentation

#### Community Support
- **GitHub Issues:** [Report bugs and request features](https://github.com/tschaban/openhab-addons-sonoff/issues)
- **OpenHAB Community:** [Community forum](https://community.openhab.org/)
- **Original Repository:** [delid4ve/openhab-sonoff](https://github.com/delid4ve/openhab-sonoff)

## 📈 Performance Tips

### Development Efficiency
- **Use PowerShell scripts** for consistent workflow
- **Run component-specific tests** during development
- **Leverage IDE test runners** for quick feedback
- **Use Maven offline mode** when possible: `mvn -o test`

### Testing Optimization
- **Parallel test execution** - Configure in IDE
- **Test categorization** - Run relevant tests first
- **Mock external dependencies** - Faster and more reliable
- **Use test profiles** - Different configurations for different scenarios

## 🎯 Next Steps

### For New Developers
1. **Set up development environment** following this guide
2. **Run the test suite** to verify setup
3. **Explore the codebase** starting with test files
4. **Make a small change** and run tests to see the workflow

### For Contributors
1. **Read the testing documentation** to understand the framework
2. **Follow the development workflow** for consistent quality
3. **Write comprehensive tests** for new features
4. **Use the PowerShell scripts** for professional development experience

### For Maintainers
1. **Monitor GitHub Actions** for automated quality assurance
2. **Review test coverage** regularly
3. **Update documentation** as the project evolves
4. **Maintain quality gates** to ensure code reliability