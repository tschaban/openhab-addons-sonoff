# Comprehensive Testing Framework

This project features a complete JUnit 5 testing framework with comprehensive test coverage, automated scripts, and quality assurance tools.

## Framework Overview

### Testing Technologies
- **JUnit 5 (Jupiter)** version 5.10.1 - Modern testing framework
- **Mockito** version 5.8.0 - Mocking framework for unit tests
- **Maven Surefire Plugin** version 3.2.2 - Test execution and reporting
- **Spotless** - Code formatting and style enforcement

### Test Coverage Statistics
- **8 comprehensive test classes** with 3,000+ lines of test code
- **130+ individual test methods** covering all major components
- **Unit, Integration, and Error Handling** test suites
- **Mock-based testing** for external dependencies

### Directory Structure
```
src/
├── main/java/                 # Main source code
└── test/java/                 # Comprehensive test suite
    └── org/openhab/binding/sonoff/internal/
        ├── SonoffCacheProviderTest.java              # Core cache functionality (436 lines)
        ├── SonoffCacheProviderIntegrationTest.java   # Integration tests (413 lines)
        ├── SonoffCacheProviderErrorHandlingTest.java # Error scenarios (396 lines)
        ├── SonoffHandlerFactoryTest.java             # Handler factory tests (481 lines)
        ├── SonoffHandlerFactoryIntegrationTest.java  # Factory integration (300 lines)
        ├── handler/
        │   ├── SonoffBaseBridgeHandlerTest.java      # Bridge handler unit tests (600+ lines)
        │   └── SonoffBaseBridgeHandlerIntegrationTest.java # Bridge handler integration (350+ lines)
        └── discovery/
            └── SonoffDiscoveryServiceTest.java       # Discovery service tests
```

## Running Tests

### Automated Test Scripts

#### 1. Unit Test Runner (Recommended)
**Purpose:** Run tests with detailed progress tracking and error handling

**Windows PowerShell:**
```powershell
.\task-run-unit-tests.ps1
```

**Features:**
- 4-step progress tracking with colored output
- Automatic code formatting with Spotless
- Comprehensive error handling and troubleshooting
- Execution timing and performance metrics
- Success/failure summaries with next steps

#### 2. Full Deployment with Testing
**Purpose:** Complete CI/CD workflow with testing, compilation, and deployment

**Windows PowerShell:**
```powershell
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

### Manual Test Execution

#### In Full OpenHAB Environment
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=SonoffCacheProviderTest

# Run test suites by pattern
mvn test -Dtest="SonoffCacheProvider*"
mvn test -Dtest="*IntegrationTest"
mvn test -Dtest="*ErrorHandlingTest"
mvn test -Dtest="SonoffBaseBridgeHandler*"

# Run with detailed output
mvn test -X

# Generate test reports
mvn test jacoco:report
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

# Bridge Handler Tests (30+ test methods)
mvn test -Dtest="SonoffBaseBridgeHandlerTest"
mvn test -Dtest="SonoffBaseBridgeHandlerIntegrationTest"

# Discovery Service Tests
mvn test -Dtest="SonoffDiscoveryServiceTest"
```

## Test Architecture

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

#### 4. Bridge Handler Tests
**Purpose:** Test abstract bridge handler foundation
- **SonoffBaseBridgeHandlerTest.java** - Core bridge functionality
- **SonoffBaseBridgeHandlerIntegrationTest.java** - Bridge component interactions
- Mock OpenHAB framework integration and device communication

#### 5. Discovery Tests
**Purpose:** Test device discovery functionality
- **SonoffDiscoveryServiceTest.java** - Discovery service logic
- Mock network operations and device responses

### Writing Tests

#### Basic Test Structure with Mockito
```java
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MyClassTest {
    
    @Mock
    private DependencyClass mockDependency;
    
    private MyClass classUnderTest;
    
    @BeforeEach
    void setUp() {
        classUnderTest = new MyClass(mockDependency);
    }
    
    @Test
    @DisplayName("Should perform expected behavior when valid input provided")
    void testValidInput() {
        // Arrange
        when(mockDependency.someMethod()).thenReturn("expected");
        
        // Act
        String result = classUnderTest.performAction();
        
        // Assert
        assertEquals("expected", result);
        verify(mockDependency).someMethod();
    }
}
```

#### Advanced Testing Patterns

**Parameterized Tests:**
```java
@ParameterizedTest
@ValueSource(strings = {"value1", "value2", "value3"})
void testWithMultipleValues(String input) {
    assertNotNull(classUnderTest.process(input));
}
```

**Exception Testing:**
```java
@Test
void testExceptionHandling() {
    assertThrows(IllegalArgumentException.class, () -> {
        classUnderTest.methodWithInvalidInput(null);
    });
}
```

**Timeout Testing:**
```java
@Test
@Timeout(value = 5, unit = TimeUnit.SECONDS)
void testPerformance() {
    classUnderTest.performLongRunningOperation();
}
```

### Available Assertions
- `assertEquals(expected, actual)` - Value equality
- `assertTrue(condition)` / `assertFalse(condition)` - Boolean conditions
- `assertNotNull(object)` / `assertNull(object)` - Null checks
- `assertThrows(ExceptionClass.class, executable)` - Exception testing
- `assertDoesNotThrow(executable)` - No exception expected
- `assertTimeout(duration, executable)` - Performance testing
- `assertAll(executables...)` - Multiple assertions

### Mockito Features Used
- `@Mock` - Create mock objects
- `when().thenReturn()` - Stub method behavior
- `verify()` - Verify method calls
- `ArgumentCaptor` - Capture method arguments
- `@Spy` - Partial mocking of real objects

## Cross-Platform Support

### Available Scripts
- **task-run-unit-tests.ps1** - Windows PowerShell (Enhanced with 4-step progress)
- **task-run-deploy.ps1** - Windows PowerShell (7-step CI/CD pipeline)

### Prerequisites
- **Java 17+** installed and in PATH
- **Maven 3.6+** installed and in PATH
- **Git** (for repository operations)

### Platform-Specific Notes

**Windows (Recommended):**
- Use PowerShell for full feature support
- Enhanced scripts with colored output and progress tracking
- Comprehensive error handling and troubleshooting
- Timing measurements and performance metrics

**Linux/macOS:**
- Full Maven commands work natively
- Use standard Maven commands: `mvn test`, `mvn spotless:apply`

## Development Workflow

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

### Continuous Integration Features

#### Automated Scripts
- **task-run-unit-tests.ps1** - Standalone test execution with detailed progress
- **task-run-deploy.ps1** - Full CI/CD pipeline with testing quality gate

#### Quality Gates
- **Code formatting** must pass (Spotless)
- **All unit tests** must pass before deployment
- **Compilation** must succeed without warnings
- **Deployment** only proceeds if all checks pass

#### Error Handling
- Detailed error messages with troubleshooting steps
- Execution timing for performance monitoring
- Comprehensive logging and reporting

### Enhanced Scripts Features

#### task-run-unit-tests.ps1 (115 lines)
- **4-step progress tracking** with colored output
- **Timing measurements** for Spotless and test execution
- **Comprehensive error handling** with troubleshooting guidance
- **Success/failure summaries** with execution times
- **Next steps guidance** for both scenarios

#### task-run-deploy.ps1 (267 lines)
- **7-step CI/CD workflow** with quality gates
- **Unit test execution** as mandatory step 4
- **Deployment abortion** if any tests fail
- **JAR file validation** and deployment verification
- **OpenHAB log clearing** for clean monitoring

## Test Coverage Summary

### Comprehensive Test Suite
- **8 test classes** with 3,000+ lines of test code
- **130+ individual test methods** covering all major components
- **Multiple test categories:** Unit, Integration, Error Handling, Bridge Handlers, Discovery

### Component Coverage

#### SonoffCacheProvider (3 test classes)
- **Core functionality:** File operations, cache management, JSON handling
- **Integration testing:** Real file system operations, concurrent access
- **Error handling:** Exception scenarios, boundary conditions, cleanup

#### SonoffHandlerFactory (2 test classes)  
- **Unit testing:** Handler creation, type validation, configuration
- **Integration testing:** Real OpenHAB integration, lifecycle management

#### SonoffBaseBridgeHandler (2 test classes)
- **Unit testing:** Bridge initialization, status management, command handling
- **Integration testing:** Bridge communication, lifecycle transitions, concurrent operations

#### SonoffDiscoveryService (1 test class)
- **Discovery logic:** Device detection, configuration parsing
- **Network operations:** Mock-based testing of discovery protocols

### Quality Metrics
- **Mock-based testing** for external dependencies
- **Thread safety testing** for concurrent operations
- **Performance testing** with timeout validation
- **Exception handling** for all error scenarios
- **Resource cleanup** verification

## Notes

- **Parent POM integration** preserved for OpenHAB compatibility
- **Independent test execution** without main source compilation dependency
- **Naming convention:** All test files follow `*Test.java` pattern
- **Package structure:** Tests mirror main source package organization
- **Continuous integration** ready with automated quality gates
- **Cross-platform support** with enhanced Windows PowerShell scripts

## Recent Additions: Bridge Handler Test Suite

### SonoffBaseBridgeHandler Testing
The latest addition to the testing framework provides comprehensive coverage for the abstract bridge handler foundation:

#### Test Coverage
- **SonoffBaseBridgeHandlerTest.java** - 27 unit test methods (600+ lines)
- **SonoffBaseBridgeHandlerIntegrationTest.java** - 5 integration test methods (350+ lines)

#### Key Features
- **Abstract Class Testing** - Uses concrete test implementations for abstract methods
- **Real Configuration Objects** - Uses actual DeviceConfig objects with public fields
- **Lenient Mock Strategy** - Prevents unnecessary stubbing warnings with `lenient()`
- **Side Effect Handling** - Accounts for initialization call chains (`initialize()` → `checkBridge()` → `bridgeStatusChanged()`)
- **Concurrent Testing** - Thread safety validation with concurrent operations

#### Coverage Areas
- **Initialization Scenarios** - Valid/invalid configurations, bridge relationships, device state handling
- **Status Management** - Online/offline transitions, local/cloud mode switching, error scenarios
- **Command Handling** - Channel commands (SLED), message queuing, unknown channel handling
- **Bridge Communication** - Account handler integration, LAN service management, message routing
- **Lifecycle Management** - Complete initialization to disposal workflows with resource cleanup

#### Testing Innovations
- **Null-Safe Bridge Testing** - Proper simulation of "no bridge" scenarios without NPE
- **Real Constants Usage** - Uses actual `SonoffBindingConstants.LAN_IN/LAN_OUT` values
- **Mock Reset Strategy** - Strategic mock clearing for clean verification
- **Direct Field Access** - Simplified test handler with direct protected field access

### Documentation
- **[Bridge Handler Tests](../testing/bridge-handler-tests.md)** - Comprehensive documentation
- **[Testing Index](../testing/README.md)** - Complete testing documentation overview

## Migration from Simple Setup

The testing framework has evolved significantly from the initial simple setup:

### Previous State
- 2 basic test files (SimpleJUnitTest.java, SonoffBindingConstantsTest.java)
- Basic verification scripts (no longer needed)
- Simple JUnit 5 configuration

### Current State
- **8 comprehensive test classes** with full component coverage
- **Enhanced PowerShell scripts** with detailed progress tracking
- **CI/CD integration** with quality gates and deployment automation
- **Mock-based testing** with Mockito framework
- **Multiple test categories** (Unit, Integration, Error Handling, Bridge Handlers)
- **Performance and thread safety** testing
- **Comprehensive error handling** and troubleshooting

### Latest Enhancements (Bridge Handler Tests)
- **Abstract class testing** patterns for inheritance hierarchies
- **Lenient mocking strategies** for complex initialization workflows
- **Real object usage** where mocking adds unnecessary complexity
- **Concurrent operation validation** for thread safety assurance
- **Comprehensive lifecycle testing** from initialization to disposal

### Upgrade Benefits
- **Quality assurance** through automated testing gates
- **Faster development** with immediate feedback
- **Reliable deployments** with pre-deployment validation
- **Better code coverage** with comprehensive test suites
- **Professional workflow** with CI/CD automation
- **Foundation validation** ensuring all device handlers inherit solid base functionality