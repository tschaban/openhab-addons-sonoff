# SonoffCacheProvider Test Coverage

This document describes the comprehensive test coverage for the `SonoffCacheProvider` class.

## Test Classes Overview

### 1. SonoffCacheProviderTest
**Primary unit tests covering core functionality**

#### Constructor Tests
- ✅ Constructor with Gson parameter
- ✅ Constructor without Gson parameter (default)
- ✅ Directory creation during initialization
- ✅ Proper field initialization

#### File Operations Tests
- ✅ `newFile()` - Create new cache files
- ✅ `getFile()` - Read file content
- ✅ `checkFile()` - Check file existence
- ✅ `getFiles()` - List all cache files
- ✅ File overwriting behavior
- ✅ Unicode content handling
- ✅ Large file content handling

#### Cache Operations Tests
- ✅ `getStates()` - Get all device states from cache
- ✅ `getState()` - Get single device state
- ✅ JSON parsing with Gson
- ✅ Null JSON handling
- ✅ Invalid JSON handling

#### Thread Safety Tests
- ✅ Concurrent file operations
- ✅ Multiple threads writing different files
- ✅ Race condition handling

### 2. SonoffCacheProviderIntegrationTest
**Integration tests with real file system operations**

#### High-Volume Testing
- ✅ High-volume concurrent file operations (20 threads × 50 operations)
- ✅ Mixed read/write operations (15 threads with different operation types)
- ✅ File system stress test (1000 files)
- ✅ Rapid file updates (100 updates to same file)

#### Real-World Scenarios
- ✅ Real JSON parsing with actual Gson instance
- ✅ Invalid JSON handling with real parser
- ✅ Various file sizes (10B to 100KB)
- ✅ Data integrity under concurrent access

#### Performance Testing
- ✅ Large-scale file creation and retrieval
- ✅ Concurrent access patterns
- ✅ File system performance under load

### 3. SonoffCacheProviderErrorHandlingTest
**Error handling and edge cases**

#### Null Parameter Handling
- ✅ Null device ID in `newFile()`
- ✅ Null content in `newFile()`
- ✅ Null filename in `getFile()`
- ✅ Null device ID in `checkFile()`

#### Edge Cases
- ✅ Empty device IDs and content
- ✅ Very long device IDs (1000 characters)
- ✅ Special characters in device IDs
- ✅ Single character inputs
- ✅ Numeric device IDs

#### Error Scenarios
- ✅ Gson throwing JsonSyntaxException
- ✅ Null Gson instance handling
- ✅ Corrupted cache directory
- ✅ Missing cache directory
- ✅ Read-only directory permissions
- ✅ Non-text files in cache directory

#### Boundary Conditions
- ✅ Extremely large file content (1MB)
- ✅ Empty directory handling
- ✅ Empty files handling
- ✅ Mixed case device IDs

## Methods Covered

### Public Methods
- ✅ `SonoffCacheProvider(Gson gson)` - Constructor with Gson
- ✅ `SonoffCacheProvider()` - Default constructor
- ✅ `newFile(String deviceid, String thing)` - Create/update cache file
- ✅ `getFiles()` - Get all cache file contents
- ✅ `checkFile(String deviceid)` - Check if cache file exists
- ✅ `getFile(String filename)` - Get specific file content
- ✅ `getStates()` - Get all device states (requires Gson)
- ✅ `getState(String deviceid)` - Get specific device state (requires Gson)

### Internal Behavior
- ✅ Directory creation logic
- ✅ File filtering (.txt files only)
- ✅ UTF-8 encoding handling
- ✅ Resource cleanup (BufferedReader/Writer)
- ✅ Exception handling and logging

## Test Coverage Metrics

### Code Paths Covered
- ✅ **Constructor paths**: Both with and without Gson
- ✅ **File creation**: New files and overwrites
- ✅ **File reading**: Existing and non-existing files
- ✅ **Directory operations**: Creation, listing, filtering
- ✅ **JSON operations**: Valid and invalid JSON parsing
- ✅ **Error paths**: IOException handling, null parameters

### Scenarios Tested
- ✅ **Normal operations**: Standard file cache operations
- ✅ **Concurrent access**: Multiple threads accessing cache
- ✅ **Error conditions**: Various failure scenarios
- ✅ **Edge cases**: Boundary conditions and special inputs
- ✅ **Performance**: High-volume operations
- ✅ **Integration**: Real file system and JSON parsing

## Test Dependencies

### Required Dependencies
- **JUnit 5** (jupiter) - Test framework
- **Mockito Core** - Mocking framework
- **Mockito JUnit Jupiter** - Mockito-JUnit integration
- **Gson** - JSON parsing (real instance for integration tests)

### Mock Objects Used
- `Gson` - For JSON parsing operations
- `JsonObject` - For JSON object representation
- `SonoffDeviceState` - For device state objects
- `OpenHAB` - For user data folder path

### Test Infrastructure
- **@TempDir** - Temporary directories for file operations
- **MockedStatic** - Static method mocking for OpenHAB
- **ExecutorService** - Thread pool for concurrent testing
- **CountDownLatch** - Synchronization for concurrent tests

## Running the Tests

### Command Line
```bash
# Run all cache provider tests
run-cache-tests.bat

# Run individual test classes
mvn test -Dtest=SonoffCacheProviderTest
mvn test -Dtest=SonoffCacheProviderIntegrationTest
mvn test -Dtest=SonoffCacheProviderErrorHandlingTest

# Run all three test classes together
mvn test -Dtest="SonoffCacheProviderTest,SonoffCacheProviderIntegrationTest,SonoffCacheProviderErrorHandlingTest"
```

### Test Categories
```bash
# Unit tests only
mvn test -Dtest=SonoffCacheProviderTest

# Integration tests only
mvn test -Dtest=SonoffCacheProviderIntegrationTest

# Error handling tests only
mvn test -Dtest=SonoffCacheProviderErrorHandlingTest
```

## Coverage Report

When JaCoCo is configured, test coverage reports are generated at:
- **HTML Report:** `target/site/jacoco/index.html`
- **XML Report:** `target/site/jacoco/jacoco.xml`

## Test Quality Metrics

### Test Statistics
- **Total Test Methods:** 50+ test methods across 3 classes
- **Unit Tests:** 20+ methods in SonoffCacheProviderTest
- **Integration Tests:** 15+ methods in SonoffCacheProviderIntegrationTest
- **Error Tests:** 15+ methods in SonoffCacheProviderErrorHandlingTest

### Assertions per Test
- **Average:** 3-4 assertions per test method
- **Range:** 1-8 assertions depending on test complexity
- **Total:** 150+ assertions across all tests

### Concurrent Testing
- **Thread Counts:** Up to 20 concurrent threads
- **Operations:** Up to 1000 file operations per test
- **Stress Testing:** High-volume concurrent access patterns

## Key Features Tested

### File System Operations
- ✅ **File Creation:** UTF-8 encoded text files
- ✅ **File Reading:** Content retrieval with encoding
- ✅ **File Checking:** Existence verification
- ✅ **Directory Management:** Auto-creation and listing
- ✅ **File Filtering:** .txt extension filtering

### Cache Management
- ✅ **Device State Caching:** JSON-based device state storage
- ✅ **Bulk Operations:** Multiple device state retrieval
- ✅ **State Persistence:** File-based persistence
- ✅ **Cache Integrity:** Data consistency verification

### Error Resilience
- ✅ **Graceful Degradation:** Continues operation despite errors
- ✅ **Resource Cleanup:** Proper file handle management
- ✅ **Exception Handling:** IOException and JsonSyntaxException
- ✅ **Null Safety:** Handles null parameters gracefully

### Performance Characteristics
- ✅ **Concurrent Access:** Thread-safe file operations
- ✅ **Large Files:** Handles files up to 1MB+
- ✅ **High Volume:** Supports 1000+ cached devices
- ✅ **Rapid Updates:** Handles frequent file updates

This comprehensive test suite ensures that the `SonoffCacheProvider` is thoroughly tested for reliability, performance, and error handling in various scenarios.