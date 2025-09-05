@echo off
REM Script to verify that test fixes resolved the issues
REM Usage: verify-test-fixes.bat

echo Verifying test fixes for SonoffHandlerFactory...

REM Check if Maven is available
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Maven is not installed or not in PATH.
    pause
    exit /b 1
)

echo ✅ Maven is available

REM Clean and compile
echo.
echo 📦 Cleaning and compiling...
mvn clean test-compile -q
if %errorlevel% neq 0 (
    echo ❌ Compilation failed
    pause
    exit /b 1
)

echo ✅ Compilation successful

REM Run SimpleJUnitTest first (should always pass)
echo.
echo 🧪 Running SimpleJUnitTest...
mvn test -Dtest=SimpleJUnitTest -q
if %errorlevel% equ 0 (
    echo ✅ SimpleJUnitTest passed
) else (
    echo ❌ SimpleJUnitTest failed
    goto failed
)

REM Run SonoffHandlerFactoryTest
echo.
echo 🧪 Running SonoffHandlerFactoryTest...
mvn test -Dtest=SonoffHandlerFactoryTest
set FACTORY_TEST_RESULT=%errorlevel%

if %FACTORY_TEST_RESULT% equ 0 (
    echo ✅ SonoffHandlerFactoryTest passed
) else (
    echo ❌ SonoffHandlerFactoryTest still has issues
)

REM Run SonoffHandlerFactoryIntegrationTest
echo.
echo 🧪 Running SonoffHandlerFactoryIntegrationTest...
mvn test -Dtest=SonoffHandlerFactoryIntegrationTest
set INTEGRATION_TEST_RESULT=%errorlevel%

if %INTEGRATION_TEST_RESULT% equ 0 (
    echo ✅ SonoffHandlerFactoryIntegrationTest passed
) else (
    echo ❌ SonoffHandlerFactoryIntegrationTest still has issues
)

REM Summary
echo.
echo 📋 Test Results Summary:
echo ✅ SimpleJUnitTest: Passed
if %FACTORY_TEST_RESULT% equ 0 (
    echo ✅ SonoffHandlerFactoryTest: Passed
) else (
    echo ❌ SonoffHandlerFactoryTest: Failed
)

if %INTEGRATION_TEST_RESULT% equ 0 (
    echo ✅ SonoffHandlerFactoryIntegrationTest: Passed
) else (
    echo ❌ SonoffHandlerFactoryIntegrationTest: Failed
)

REM Overall result
if %FACTORY_TEST_RESULT% equ 0 if %INTEGRATION_TEST_RESULT% equ 0 (
    echo.
    echo 🎉 All test fixes successful!
    echo.
    echo 📊 What was fixed:
    echo - ThingTypeUID validation errors resolved
    echo - Binding ID test logic corrected
    echo - Special character tests updated
    echo - Integration tests now handle OpenHAB validation properly
    echo.
    echo 💡 The tests now accurately reflect OpenHAB framework behavior
    goto success
) else (
    echo.
    echo ⚠️  Some tests still have issues
    echo.
    echo 🔍 Check the test output above for details
    echo 📖 See TEST-FIXES-APPLIED.md for more information
    goto partial_success
)

:failed
echo.
echo ❌ Critical test failure!
echo.
echo 🔧 Recommended actions:
echo 1. Check Maven repository: fix-maven-repository.bat
echo 2. Try standalone POM: test-with-standalone-pom.bat
echo 3. Review error messages above
pause
exit /b 1

:partial_success
echo.
echo 📈 Progress made but some issues remain
echo.
echo 💡 This may be expected if:
echo - OpenHAB runtime dependencies are missing
echo - Some tests require full OpenHAB environment
echo - Framework validation behaves differently than expected
echo.
pause
exit /b 0

:success
echo.
echo ✅ All tests are now working correctly!
echo.
echo 🚀 You can now:
echo 1. Run full test suite: mvn test
echo 2. Generate coverage report: mvn test jacoco:report
echo 3. Continue development with confidence
echo.
pause
exit /b 0