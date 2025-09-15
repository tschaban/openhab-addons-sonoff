# Sonoff Binding Documentation

Documentation for the Sonoff OpenHAB Binding.

## 📚 Documentation Categories

### 🔧 Development
**[Development Documentation](development/README.md)**
- **[Testing Framework](development/testing-framework.md)** - Test execution and coverage
- **[CI/CD Automation](development/cicd-automation.md)** - GitHub Actions workflows
- **[PowerShell Scripts](development/powershell-scripts.md)** - Local automation
- **[Parent POM Integration](development/parent-pom-integration.md)** - OpenHAB integration

### 🧪 Testing
**[Testing Documentation](testing/README.md)**
- **[Bridge Handler Tests](testing/bridge-handler-tests.md)** - SonoffBaseBridgeHandler coverage
- **[Cache Provider Tests](testing/cache-provider-tests.md)** - SonoffCacheProvider coverage
- **[Handler Factory Tests](testing/handler-factory-tests.md)** - SonoffHandlerFactory coverage

## 🚀 Quick Start

### Testing
```bash
mvn test                                    # Run all tests
mvn test -Dtest="SonoffBaseBridgeHandler*" # Bridge handler tests
mvn test -Dtest="*IntegrationTest"         # Integration tests
```

### Development
```bash
mvn spotless:apply                         # Format code
mvn clean compile                          # Build project
```

### PowerShell (Windows)
```powershell
.\task-run-unit-tests.ps1                 # Test runner
.\task-run-deploy.ps1                     # CI/CD pipeline
```

## 📋 Test Categories

- **Unit Tests** - Individual component testing
- **Integration Tests** - Component interaction testing
- **Bridge Handler Tests** - Abstract foundation testing

## 🔍 Finding Information

### By Component
- **Bridge Handlers** → [Bridge Handler Tests](testing/bridge-handler-tests.md)
- **Cache Provider** → [Cache Provider Tests](testing/cache-provider-tests.md)
- **Handler Factory** → [Handler Factory Tests](testing/handler-factory-tests.md)