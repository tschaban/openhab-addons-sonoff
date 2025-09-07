# SonoffHandlerFactory Test Coverage

This document describes the comprehensive test coverage for the `SonoffHandlerFactory` class.

## Test Classes

### 1. SonoffHandlerFactoryTest
**Primary unit tests covering core functionality**

#### Constructor Tests
- ✅ Dependency injection with WebSocketFactory and HttpClientFactory
- ✅ Proper initialization of WebSocket and HTTP clients

#### supportsThingType() Tests
- ✅ All supported thing types from `SUPPORTED_THING_TYPE_UIDS`
- ✅ Unsupported thing types (different binding, invalid device IDs)
- ✅ Individual verification of 40+ supported device types

#### createHandler() Tests by Device Category

**Bridge Handlers:**
- ✅ `account` → SonoffAccountHandler
- ✅ `28` → SonoffRfBridgeHandler  
- ✅ `66`, `168`, `243` → SonoffZigbeeBridgeHandler

**Single Switch Handlers:**
- ✅ `1`, `6`, `14`, `27`, `81`, `107`, `160`, `209`, `256`, `260` → SonoffSwitchSingleHandler

**Multi Switch Handlers:**
- ✅ `2`, `3`, `4`, `7`, `8`, `9`, `29`, `30`, `31`, `77`, `78`, `82`, `83`, `84`, `126`, `161`, `162`, `210`, `211`, `212` → SonoffSwitchMultiHandler

**Specialized Device Handlers:**
- ✅ `5` → SonoffSwitchPOWHandler
- ✅ `15`, `181` → SonoffSwitchTHHandler
- ✅ `24` → SonoffGSMSocketHandler
- ✅ `32` → SonoffSwitchPOWR2Handler
- ✅ `59` → SonoffRGBStripHandler
- ✅ `102` → SonoffMagneticSwitchHandler
- ✅ `104` → SonoffRGBCCTHandler
- ✅ `138` → SonoffSwitchSingleMiniHandler
- ✅ `190` → SonoffSwitchPOWUgradedHandler
- ✅ `237` → SonoffGateHandler

**Sensor Handlers:**
- ✅ `1770`, `7014` → SonoffZigbeeDeviceTemperatureHumiditySensorHandler
- ✅ `2026` → SonoffZigbeeDeviceMotionSensorHandler
- ✅ `7003` → SonoffZigbeeContactSensorHandler

**RF Device Handlers:**
- ✅ `rfremote1`, `rfremote2`, `rfremote3`, `rfremote4`, `rfsensor` → SonoffRfDeviceHandler

#### Error Handling Tests
- ✅ Unsupported device types return null
- ✅ Null thing type throws NullPointerException
- ✅ Null thing parameter throws NullPointerException
- ✅ Different binding ID handling (supportsThingType vs createHandler behavior)
- ✅ ThingTypeUID validation (empty/invalid IDs rejected by OpenHAB framework)

### 2. SonoffHandlerFactoryIntegrationTest
**Integration tests and edge cases**

#### Comprehensive Coverage Tests
- ✅ All thing types from `SUPPORTED_THING_TYPE_UIDS` are supported
- ✅ Verification of 30+ supported device types
- ✅ Bridge vs Thing handler creation patterns

#### Dependency Injection Tests
- ✅ Multiple factory instances with same dependencies
- ✅ Null dependency handling (WebSocketFactory, HttpClientFactory)
- ✅ Null client handling (WebSocketClient, HttpClient)

#### Edge Case Tests
- ✅ Case sensitivity in device IDs
- ✅ Invalid device IDs (handled by ThingTypeUID validation)
- ✅ ThingTypeUID validation rules (empty, whitespace, special characters)
- ✅ Consistent behavior across multiple calls
- ✅ Binding ID validation through supportsThingType() method

#### Device Category Validation
- ✅ Single switch device category (10 device types)
- ✅ Multi switch device category (20 device types)
- ✅ Sensor device category (3 device types)
- ✅ RF device category (5 device types)

## Test Coverage Metrics

### Methods Covered
- ✅ `SonoffHandlerFactory(WebSocketFactory, HttpClientFactory)` - Constructor
- ✅ `supportsThingType(ThingTypeUID)` - Thing type support validation
- ✅ `createHandler(Thing)` - Handler creation for all device types

### Code Paths Covered
- ✅ All 40+ switch cases in `createHandler()`
- ✅ Default case (unsupported device types)
- ✅ Bridge casting for bridge handlers
- ✅ Regular Thing handling for device handlers

### Error Scenarios Covered
- ✅ Null parameters
- ✅ Invalid device IDs (ThingTypeUID validation)
- ✅ Unsupported thing types
- ✅ Different binding IDs (supportsThingType vs createHandler)
- ✅ Case sensitivity issues
- ✅ OpenHAB framework validation rules

## Running the Tests

### Command Line
```bash
# Linux/macOS
./run-tests.sh

# Windows PowerShell
.\run-tests.ps1

# Direct Maven
mvn test -Dtest="SonoffHandlerFactoryTest,SonoffHandlerFactoryIntegrationTest"
```

### Individual Test Classes
```bash
# Run only unit tests
mvn test -Dtest=SonoffHandlerFactoryTest

# Run only integration tests
mvn test -Dtest=SonoffHandlerFactoryIntegrationTest
```

## Test Dependencies

### Required Dependencies
- **JUnit 5** (jupiter) - Test framework
- **Mockito Core** - Mocking framework
- **Mockito JUnit Jupiter** - Mockito-JUnit integration

### Mock Objects Used
- `WebSocketFactory` - For WebSocket client creation
- `HttpClientFactory` - For HTTP client creation
- `WebSocketClient` - WebSocket client instance
- `HttpClient` - HTTP client instance
- `Thing` - OpenHAB Thing objects
- `Bridge` - OpenHAB Bridge objects

## Coverage Report

When JaCoCo is configured, test coverage reports are generated at:
- **HTML Report:** `target/site/jacoco/index.html`
- **XML Report:** `target/site/jacoco/jacoco.xml`

## Test Quality Metrics

### Assertions per Test
- **Average:** 2-3 assertions per test method
- **Range:** 1-5 assertions depending on test complexity
- **Total:** 100+ assertions across all tests

### Test Categories
- **Unit Tests:** 25+ test methods
- **Integration Tests:** 15+ test methods
- **Parameterized Tests:** 8+ parameterized test methods
- **Error Case Tests:** 10+ error scenario tests

### Device Type Coverage
- **Total Device Types Tested:** 40+
- **Bridge Types:** 5 types
- **Device Types:** 35+ types
- **Handler Classes Verified:** 15+ different handler classes

## 🔍 Key Learnings from Test Execution

### OpenHAB Framework Validation
The tests revealed important aspects of OpenHAB's validation:

1. **ThingTypeUID Validation**: OpenHAB's `ThingTypeUID` class enforces strict validation rules:
   - Empty device IDs throw `IllegalArgumentException`
   - Invalid characters (like `.` or spaces) are rejected
   - Only alphanumeric characters, hyphens, and underscores are allowed

2. **Factory Method Separation**: 
   - `supportsThingType()` is the proper gatekeeper for binding validation
   - `createHandler()` focuses on device-specific logic and assumes valid input
   - The OpenHAB framework calls `supportsThingType()` before `createHandler()`

3. **Test Design Implications**:
   - Tests must account for OpenHAB's built-in validation
   - Invalid inputs are caught by the framework, not our factory
   - Tests should verify both framework integration and factory logic

### Updated Test Strategy
- ✅ **Framework Integration**: Test how our factory works with OpenHAB validation
- ✅ **Boundary Testing**: Test valid but non-existent device types
- ✅ **Documentation**: Tests now document actual behavior vs. expected behavior
- ✅ **Error Handling**: Distinguish between framework errors and factory errors

This comprehensive test suite ensures that the `SonoffHandlerFactory` is thoroughly tested within the OpenHAB framework context and maintains high code quality standards.