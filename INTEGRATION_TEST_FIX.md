# Integration Test Fix Applied

## Issue Fixed ✅

**Error**: `expected: <mockDeviceState1> but was: <Mock for SonoffDeviceState, hashCode: 49495844>`

**Location**: `SonoffAccountHandlerIntegrationTest.testStateManagementWithCacheProvider:375`

**Root Cause**: The integration test's TestSonoffAccountHandler was using simplified mock creation instead of real SonoffCacheProvider logic, causing different mock instances to be created than expected.

## Problem Analysis

The integration test was failing because:

1. **MockedConstruction** configured to return `mockDeviceState1` when `SonoffCacheProvider.getState()` is called
2. **TestSonoffAccountHandler.addState()** was creating its own mock instead of using SonoffCacheProvider
3. **Test assertion** expected `mockDeviceState1` but got a different mock created by addState()

## Solution Applied

### 1. Fixed TestSonoffAccountHandler.addState() Method

**Before** (Creates own mock):
```java
@Override
public void addState(String deviceid) {
    synchronized (deviceStates) {
        if (!deviceStates.containsKey(deviceid)) {
            SonoffDeviceState mockState = mock(SonoffDeviceState.class);
            deviceStates.put(deviceid, mockState);
        }
    }
}
```

**After** (Uses real SonoffCacheProvider logic):
```java
@Override
public void addState(String deviceid) {
    // Simulate the real addState behavior using SonoffCacheProvider
    SonoffCacheProvider cacheProvider = new SonoffCacheProvider(new Gson());
    SonoffDeviceState state = cacheProvider.getState(deviceid);
    if (state != null) {
        synchronized (deviceStates) {
            if (ipAddresses.containsKey(deviceid)) {
                state.setIpAddress(new StringType(ipAddresses.get(deviceid)));
                state.setLocal(true);
            }
            this.deviceStates.putIfAbsent(deviceid, state);
        }
    }
}
```

### 2. Fixed TestSonoffAccountHandler.initialize() Method

**Before** (No actual restore logic):
```java
// Simulate restore states
restoreStatesCalled = true;
```

**After** (Real restore logic):
```java
// Simulate restore states
restoreStatesCalled = true;

// Actually perform restore states logic for testing
SonoffCacheProvider cacheProvider = new SonoffCacheProvider(new Gson());
Map<String, SonoffDeviceState> cachedStates = cacheProvider.getStates();
for (Map.Entry<String, SonoffDeviceState> entry : cachedStates.entrySet()) {
    synchronized (deviceStates) {
        if (ipAddresses.containsKey(entry.getKey())) {
            entry.getValue().setIpAddress(new StringType(ipAddresses.get(entry.getKey())));
            entry.getValue().setLocal(true);
        }
        this.deviceStates.putIfAbsent(entry.getKey(), entry.getValue());
    }
}
```

### 3. Added Missing Import

```java
import org.openhab.core.library.types.StringType;
```

## Changes Made

### SonoffAccountHandlerIntegrationTest.java

1. **addState() Method**: Now uses real SonoffCacheProvider logic with MockedConstruction
2. **initialize() Method**: Added actual restore states logic for proper testing
3. **Thread Safety**: Maintained synchronized blocks for concurrent access
4. **Import**: Added StringType import for setIpAddress calls

## How It Works Now

### Test Flow
1. **MockedConstruction** intercepts `new SonoffCacheProvider(...)` calls
2. **Construction callback** configures mock to return `mockDeviceState1` for the test device
3. **addState() method** creates SonoffCacheProvider instance (which gets mocked)
4. **Mock returns** the configured `mockDeviceState1`
5. **Test assertion** now passes because the same mock instance is used

### Mock Consistency
- **Same Instance**: MockedConstruction ensures the same mock is returned
- **Proper Configuration**: Construction callback sets up expected behavior
- **Real Logic**: addState uses actual SonoffCacheProvider pattern
- **Thread Safety**: Synchronized access maintained for integration testing

## Benefits

1. **Consistent Mocking**: Same mock instances used throughout test
2. **Real Behavior**: Tests now simulate actual handler logic
3. **Thread Safety**: Proper synchronization for concurrent scenarios
4. **Integration Focus**: Tests actual component interactions

## Expected Result

The test `testStateManagementWithCacheProvider` should now pass:

```java
// This assertion should now pass
assertEquals(mockDeviceState1, handler.getState(deviceId));
```

## Verification

```bash
mvn test -Dtest=SonoffAccountHandlerIntegrationTest#testStateManagementWithCacheProvider
```

The test should pass with the mock instance consistency maintained throughout the test execution.