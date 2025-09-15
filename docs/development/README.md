# Development Documentation

Essential development resources for the Sonoff OpenHAB Binding.

## 📚 Documentation Index

### Testing
- **[Testing Framework](testing-framework.md)** - Test execution and coverage
- **[Bridge Handler Tests](../testing/bridge-handler-tests.md)** - Foundation testing

### Automation
- **[CI/CD Automation](cicd-automation.md)** - GitHub Actions workflows
- **[PowerShell Scripts](powershell-scripts.md)** - Local automation tools

### Project Integration
- **[Parent POM Integration](parent-pom-integration.md)** - OpenHAB integration
- **[Workflow Best Practices](workflow-best-practices.md)** - Development guidelines

## 📋 Quick Reference

### Essential Commands
```bash
# Testing
mvn test                                    # Run all tests
mvn test -Dtest="SonoffBaseBridgeHandler*" # Bridge handler tests
mvn test -Dtest="*IntegrationTest"         # Integration tests

# Development
mvn spotless:apply                         # Format code
mvn clean compile                          # Build project

# PowerShell (Windows)
.\task-run-unit-tests.ps1                 # Test runner
.\task-run-deploy.ps1                     # CI/CD pipeline
```

## 🏗️ Core Components

- **SonoffCacheProvider** - Device state caching
- **SonoffHandlerFactory** - Device handler creation
- **SonoffBaseBridgeHandler** - Abstract foundation for bridge handlers
- **SonoffDiscoveryService** - Device discovery

## 🧪 Test Categories

- **Unit Tests** - Individual component testing
- **Integration Tests** - Component interaction testing
- **Bridge Handler Tests** - Abstract base class testing