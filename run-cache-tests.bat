@echo off
REM Script to run SonoffCacheProvider test suite
REM Usage: run-cache-tests.bat

echo Running SonoffCacheProvider test suite...

REM Check if Maven is available
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Maven is not installed or not in PATH.
    pause
    exit /b 1
)

echo ✅ Maven is available

REM Clean and compile test classes
echo.
echo 📦 Compiling test classes...
mvn clean test-compile -q

if %errorlevel% neq 0 (
    echo ❌ Test compilation failed
    pause
    exit /b 1
fi

echo ✅ Test compilation successful

REM Run SonoffCacheProvider tests
echo.
echo 🧪 Running SonoffCacheProvider unit tests...
mvn test -Dtest="SonoffCacheProviderTest" -q
set UNIT_TEST_RESULT=%errorlevel%

echo.
echo 🧪 Running SonoffCacheProvider integration tests...
mvn test -Dtest="SonoffCacheProviderIntegrationTest" -q
set INTEGRATION_TEST_RESULT=%errorlevel%

echo.
echo 🧪 Running SonoffCacheProvider error handling tests...
mvn test -Dtest="SonoffCacheProviderErrorHandlingTest" -q
set ERROR_TEST_RESULT=%errorlevel%

REM Summary
echo.
echo 📋 Test Results Summary:
if %UNIT_TEST_RESULT% equ 0 (
    echo ✅ SonoffCacheProviderTest: Passed
) else (
    echo ❌ SonoffCacheProviderTest: Failed
)

if %INTEGRATION_TEST_RESULT% equ 0 (
    echo ✅ SonoffCacheProviderIntegrationTest: Passed
) else (
    echo ❌ SonoffCacheProviderIntegrationTest: Failed
)

if %ERROR_TEST_RESULT% equ 0 (
    echo ✅ SonoffCacheProviderErrorHandlingTest: Passed
) else (
    echo ❌ SonoffCacheProviderErrorHandlingTest: Failed
)

REM Overall result
if %UNIT_TEST_RESULT% equ 0 if %INTEGRATION_TEST_RESULT% equ 0 if %ERROR_TEST_RESULT% equ 0 (
    echo.
    echo 🎉 All SonoffCacheProvider tests passed!
    echo.
    echo 📊 Test Coverage Summary:
    echo - Unit Tests: Core functionality and basic operations
    echo - Integration Tests: Real file system operations and concurrency
    echo - Error Handling Tests: Edge cases and error scenarios
    echo.
    echo 🎯 Coverage includes:
    echo - Constructor initialization (with and without Gson)
    echo - File operations (create, read, check, list)
    echo - Cache operations (getStates, getState)
    echo - Thread safety and concurrent access
    echo - Error handling and edge cases
    echo - File system stress testing
    echo - JSON parsing with real and mock Gson
    echo - Null parameter handling
    echo - Boundary conditions
    echo.
    goto success
) else (
    echo.
    echo ⚠️  Some tests failed
    echo.
    echo 🔍 Check the test output above for details
    echo 📖 See SONOFF-CACHE-PROVIDER-TESTS.md for more information
    goto partial_success
)

:partial_success
echo.
echo 📈 Some tests passed but issues remain
echo.
echo 💡 This may be expected if:
echo - File system permissions are restricted
echo - Temporary directory access is limited
echo - Some edge cases behave differently on this platform
echo.
pause
exit /b 1

:success
echo.
echo ✅ All SonoffCacheProvider tests are working correctly!
echo.
echo 🚀 You can now:
echo 1. Run full test suite: mvn test
echo 2. Generate coverage report: mvn test jacoco:report
echo 3. Continue development with confidence in cache operations
echo.
pause
exit /b 0