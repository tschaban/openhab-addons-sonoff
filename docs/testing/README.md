# Testing Documentation

Test suite documentation for the Sonoff OpenHAB binding.

## Test Documentation Index

### Component Tests
- **[Bridge Handler Tests](bridge-handler-tests.md)** - SonoffBaseBridgeHandler coverage
- **[Cache Provider Tests](cache-provider-tests.md)** - SonoffCacheProvider coverage
- **[Handler Factory Tests](handler-factory-tests.md)** - SonoffHandlerFactory coverage
- **[Discovery Service Tests](discovery-service-tests.md)** - SonoffDiscoveryService coverage

## Test Classes by Component

### SonoffBaseBridgeHandler (2 test classes)
- `SonoffBaseBridgeHandlerTest.java` - Unit tests
- `SonoffBaseBridgeHandlerIntegrationTest.java` - Integration tests

### SonoffCacheProvider (3 test classes)
- `SonoffCacheProviderTest.java` - Core functionality
- `SonoffCacheProviderIntegrationTest.java` - Integration tests
- `SonoffCacheProviderErrorHandlingTest.java` - Error scenarios

### SonoffHandlerFactory (2 test classes)
- `SonoffHandlerFactoryTest.java` - Factory tests
- `SonoffHandlerFactoryIntegrationTest.java` - Factory integration

### SonoffDiscoveryService (1 test class)
- `SonoffDiscoveryServiceTest.java` - Discovery service tests

## Test Categories

- **Unit Tests** - Individual component testing
- **Integration Tests** - Component interaction testing
- **Error Handling Tests** - Exception scenarios
- **Bridge Handler Tests** - Abstract base class testing

## Running Tests

```bash
# Run all tests
mvn test

# Run by component
mvn test -Dtest="SonoffBridgeHandler*"
mvn test -Dtest="SonoffCacheProvider*"
mvn test -Dtest="SonoffHandlerFactory*"

# Run by test type
mvn test -Dtest="*IntegrationTest"
```

### PowerShell Scripts (Windows)
```powershell
.\task-run-unit-tests.ps1    # Run tests
.\task-run-deploy.ps1        # CI/CD pipeline
```