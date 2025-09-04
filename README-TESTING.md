# JUnit 5 Testing Framework Setup

This project has been configured with JUnit 5 for unit testing.

## Configuration

### Dependencies Added
- **JUnit 5 (Jupiter)** version 5.10.1 with test scope
- **Maven Surefire Plugin** version 3.2.2 for test execution

### Directory Structure
```
src/
├── main/java/                 # Main source code
└── test/java/                 # Unit tests
    └── org/openhab/binding/sonoff/internal/
        ├── SimpleJUnitTest.java           # Example test demonstrating JUnit 5 features
        └── SonoffBindingConstantsTest.java # Test for binding constants
```

## Running Tests

### In Full OpenHAB Environment
When this project is part of the full OpenHAB build environment:

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=SimpleJUnitTest

# Run tests with specific pattern
mvn test -Dtest="*Test"
```

### Verification Script
Use the provided verification script to check the setup:

```bash
./verify-tests.sh
```

## Writing Tests

### Basic Test Structure
```java
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

class MyClassTest {
    
    @BeforeEach
    void setUp() {
        // Initialize test data
    }
    
    @Test
    @DisplayName("Test description")
    void testMethod() {
        // Test implementation
        assertEquals(expected, actual);
        assertTrue(condition);
        assertNotNull(object);
    }
}
```

### Available Assertions
- `assertEquals(expected, actual)`
- `assertTrue(condition)`
- `assertFalse(condition)`
- `assertNotNull(object)`
- `assertNull(object)`
- `assertThrows(ExceptionClass.class, () -> { /* code */ })`
- `assertDoesNotThrow(() -> { /* code */ })`

### Test Lifecycle Annotations
- `@BeforeEach` - Run before each test method
- `@AfterEach` - Run after each test method
- `@BeforeAll` - Run once before all tests (static method)
- `@AfterAll` - Run once after all tests (static method)

## Notes

- The parent POM configuration is preserved for OpenHAB integration
- Tests are configured to run independently of main source compilation
- All test files should follow the `*Test.java` naming convention
- Tests are located in the same package structure as the classes they test

## Example Tests Included

1. **SimpleJUnitTest.java** - Demonstrates basic JUnit 5 features
2. **SonoffBindingConstantsTest.java** - Tests for the binding constants class

These examples show how to structure tests and use various JUnit 5 features.