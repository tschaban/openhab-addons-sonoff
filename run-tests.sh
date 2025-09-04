#!/bin/bash

echo "Running SonoffHandlerFactory test suite..."

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed or not in PATH."
    echo "Please install Maven and ensure it's in your PATH."
    exit 1
fi

echo "✅ Maven is available"

# Clean and compile test classes
echo "📦 Compiling test classes..."
mvn clean test-compile -q

if [ $? -ne 0 ]; then
    echo "❌ Test compilation failed"
    exit 1
fi

echo "✅ Test compilation successful"

# Run specific SonoffHandlerFactory tests
echo "🧪 Running SonoffHandlerFactory tests..."
mvn test -Dtest="SonoffHandlerFactoryTest,SonoffHandlerFactoryIntegrationTest" -q

if [ $? -eq 0 ]; then
    echo "✅ All SonoffHandlerFactory tests passed!"
    
    # Generate coverage report if JaCoCo is available
    if [ -f "target/site/jacoco/index.html" ]; then
        echo "📊 Test coverage report generated: target/site/jacoco/index.html"
    fi
    
    # Show test summary
    echo ""
    echo "📋 Test Summary:"
    echo "- SonoffHandlerFactoryTest: Unit tests for core functionality"
    echo "- SonoffHandlerFactoryIntegrationTest: Integration and edge case tests"
    echo ""
    echo "🎯 Coverage includes:"
    echo "- Constructor and dependency injection"
    echo "- supportsThingType() for all supported/unsupported types"
    echo "- createHandler() for all 40+ device types"
    echo "- Error handling and edge cases"
    echo "- Bridge vs Thing handler creation"
    echo "- Null parameter handling"
    echo "- Case sensitivity and special characters"
    
else
    echo "❌ Some tests failed. Check the output above for details."
    exit 1
fi