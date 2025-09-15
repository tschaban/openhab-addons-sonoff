# Development Documentation

This directory contains comprehensive development documentation for the Sonoff OpenHAB Binding Smart'nyDom Enhanced Edition project.

## 📚 Documentation Index

### Getting Started
- **[Setup Guide](setup-guide.md)** - Complete development environment setup
  - Prerequisites and software requirements
  - IDE configuration and project setup
  - Local OpenHAB installation and configuration
  - Development workflow and best practices

### Testing & Quality Assurance
- **[Testing Framework](testing-framework.md)** - Comprehensive testing system
  - 8 test classes with 130+ test methods
  - Unit, Integration, and Error Handling test suites
  - Mock-based testing with Mockito
  - PowerShell automation scripts
  - Cross-platform test execution

### Automation & CI/CD
- **[CI/CD Automation](cicd-automation.md)** - Automated testing and deployment
  - GitHub Actions workflows
  - Quality gates and deployment pipelines
  - Automated releases and artifact generation
  - Cross-platform compatibility verification

- **[PowerShell Scripts](powershell-scripts.md)** - Local development automation
  - Enhanced test runner with progress tracking
  - Complete CI/CD pipeline for local development
  - Error handling and troubleshooting guides
  - Performance monitoring and metrics

- **[GitHub Actions Guide](github-actions-guide.md)** - CI/CD workflow details
  - Workflow configuration and triggers
  - Build matrix and platform testing
  - Artifact generation and release automation
  - Security and dependency management

### Project Structure & Integration
- **[Parent POM Integration](parent-pom-integration.md)** - OpenHAB integration
  - Parent POM configuration and inheritance
  - Dependency management and version control
  - Build configuration and plugin setup
  - OpenHAB compatibility and standards

- **[Workflow Best Practices](workflow-best-practices.md)** - Development guidelines
  - Code style and formatting standards
  - Git workflow and branching strategies
  - Testing requirements and quality gates
  - Documentation and maintenance practices

## 📋 Quick Reference

### Essential Commands
```bash
# Testing
mvn test                                    # Run all tests
mvn test -Dtest="SonoffBaseBridgeHandler*" # Bridge handler tests
mvn test -Dtest="*IntegrationTest"         # Integration tests only

# Development
mvn spotless:apply                         # Format code
mvn clean compile                          # Build project
mvn clean package                          # Create JAR

# PowerShell (Windows)
.\task-run-unit-tests.ps1                 # Enhanced test runner
.\task-run-deploy.ps1                     # Full CI/CD pipeline
```

### Documentation Navigation
- **[Main Documentation](../README.md)** - Complete documentation index
- **[Testing Documentation](../testing/README.md)** - Test suite overview
- **[Bridge Handler Tests](../testing/bridge-handler-tests.md)** - Foundation testing *(NEW)*
- **[Project README](../../README.md)** - Project overview and quick links

### Test Categories
- **Unit Tests** - `SonoffBaseBridgeHandlerTest`, `SonoffCacheProviderTest`, etc.
- **Integration Tests** - `*IntegrationTest.java` files
- **Error Handling** - `*ErrorHandlingTest.java` files
- **Component Tests** - Individual component validation

## 🚀 Quick Start Guide

### 1. Environment Setup
```bash
# Clone the repository
git clone <repository-url>
cd openhab-addons-sonoff

# Verify prerequisites
java -version    # Should be 17+
mvn -version     # Should be 3.6+
```

### 2. Development Workflow
```powershell
# Windows PowerShell (Recommended)
.\task-run-unit-tests.ps1    # Run tests with progress tracking
.\task-run-deploy.ps1        # Full CI/CD pipeline

# Cross-platform Maven
mvn spotless:apply           # Format code
mvn test                     # Run all tests
mvn clean compile           # Build project
```

### 3. Testing
```bash
# Run all tests
mvn test

# Run specific test suites
mvn test -Dtest="SonoffCacheProvider*"
mvn test -Dtest="SonoffHandlerFactory*"
mvn test -Dtest="SonoffBaseBridgeHandler*"
mvn test -Dtest="*IntegrationTest"
```

## 🏗️ Project Architecture

### Core Components
- **SonoffCacheProvider** - Device state caching and persistence
- **SonoffHandlerFactory** - Device handler creation and management
- **SonoffBaseBridgeHandler** - Abstract foundation for all bridge handlers
- **SonoffDiscoveryService** - Device discovery and configuration

### Testing Architecture
- **Unit Tests** - Individual component testing with mocked dependencies
- **Integration Tests** - Component interaction and real operation testing
- **Error Handling Tests** - Exception scenarios and edge case validation
- **Bridge Handler Tests** - Abstract base class foundation testing

### Automation Features
- **Quality Gates** - Automated testing prevents broken deployments
- **Code Formatting** - Spotless integration for consistent style
- **Cross-Platform** - Windows PowerShell and Unix/Linux Maven support
- **CI/CD Integration** - GitHub Actions for automated workflows

## 📊 Project Statistics

### Test Coverage
- **8 comprehensive test classes** with 3,000+ lines of test code
- **130+ individual test methods** covering all major components
- **Multiple test categories:** Unit, Integration, Error Handling, Bridge Handlers
- **Mock-based testing** for external dependencies

### Automation Scripts
- **task-run-unit-tests.ps1** (115 lines) - Enhanced test runner
- **task-run-deploy.ps1** (267 lines) - Complete CI/CD pipeline
- **GitHub Actions workflows** for automated testing and releases

### Code Quality
- **Spotless integration** for automatic code formatting
- **Mockito framework** for reliable unit testing
- **JUnit 5** for modern testing capabilities
- **Maven Surefire** for test execution and reporting

## 🔧 Development Tools

### Required Software
- **Java 17+** - Primary development and runtime environment
- **Maven 3.6+** - Build and dependency management
- **Git** - Version control and repository operations
- **PowerShell** (Windows) - Enhanced automation scripts

### Recommended IDEs
- **Visual Studio Code** with Java extensions
- **IntelliJ IDEA** with OpenHAB plugin
- **Eclipse** with OpenHAB development tools

### Testing Tools
- **JUnit 5** - Modern testing framework
- **Mockito** - Mocking framework for unit tests
- **Maven Surefire** - Test execution and reporting

## 🎯 Key Features

### Enhanced Testing Framework
- **Comprehensive Coverage** - All major components thoroughly tested
- **Mock-Based Testing** - Reliable unit tests with external dependency isolation
- **Integration Testing** - Real operation validation with file system and network
- **Error Handling** - Exception scenarios and edge case coverage
- **Performance Testing** - Timeout validation and concurrent operation testing

### Professional Automation
- **PowerShell Scripts** - Enhanced local development with progress tracking
- **GitHub Actions** - Automated CI/CD with quality gates
- **Quality Assurance** - Code formatting and testing requirements
- **Cross-Platform** - Windows, Linux, and macOS compatibility

### OpenHAB Integration
- **Parent POM** - Proper OpenHAB project integration
- **Standards Compliance** - Follows OpenHAB coding and testing standards
- **Framework Integration** - Proper Thing, Handler, and Discovery service implementation
- **Documentation** - Comprehensive guides for all development aspects

## 📋 Development Workflow

### 1. Code Development
1. **Setup Environment** - Follow [Setup Guide](setup-guide.md)
2. **Write Code** - Implement features following best practices
3. **Format Code** - Use Spotless for consistent formatting
4. **Write Tests** - Comprehensive test coverage required

### 2. Quality Assurance
1. **Run Tests** - All tests must pass before deployment
2. **Code Review** - Follow [Workflow Best Practices](workflow-best-practices.md)
3. **Integration Testing** - Validate component interactions
4. **Performance Validation** - Ensure acceptable performance

### 3. Deployment
1. **Local Testing** - Use PowerShell scripts for validation
2. **CI/CD Pipeline** - Automated testing and building
3. **Quality Gates** - Automated prevention of broken deployments
4. **Release Management** - Automated artifact generation

## 🔍 Troubleshooting

### Common Issues
- **Test Failures** - Check [Testing Framework](testing-framework.md) for debugging
- **Build Issues** - Verify [Setup Guide](setup-guide.md) prerequisites
- **CI/CD Problems** - Review [CI/CD Automation](cicd-automation.md) guide
- **Script Errors** - Consult [PowerShell Scripts](powershell-scripts.md) documentation

### Getting Help
- **Documentation** - Comprehensive guides for all aspects
- **Error Messages** - Enhanced scripts provide detailed troubleshooting
- **Best Practices** - Follow established patterns and guidelines
- **Testing** - Use test suites to validate functionality

## 📈 Recent Enhancements

### Bridge Handler Test Suite
- **Comprehensive Coverage** - 32 test methods for abstract base class
- **Advanced Patterns** - Lenient mocking and real configuration usage
- **Concurrent Testing** - Thread safety and deadlock prevention
- **Lifecycle Validation** - Complete initialization to disposal workflows

### Enhanced Documentation
- **Testing Index** - Complete overview of all test suites
- **Development Guide** - This comprehensive documentation index
- **Technical Details** - Advanced testing patterns and strategies
- **Migration Notes** - Evolution from simple to comprehensive testing

### Automation Improvements
- **Quality Gates** - Testing integrated into deployment pipeline
- **Error Handling** - Detailed error messages and troubleshooting
- **Performance Monitoring** - Execution timing and metrics
- **Cross-Platform** - Enhanced Windows PowerShell with Unix/Linux Maven

## 🎯 Next Steps

### For New Developers
1. **Read [Setup Guide](setup-guide.md)** - Complete environment setup
2. **Review [Testing Framework](testing-framework.md)** - Understand testing approach
3. **Study [Workflow Best Practices](workflow-best-practices.md)** - Learn development standards
4. **Practice with [PowerShell Scripts](powershell-scripts.md)** - Local automation

### For Experienced Developers
1. **Review [CI/CD Automation](cicd-automation.md)** - Understand deployment pipeline
2. **Study [Parent POM Integration](parent-pom-integration.md)** - OpenHAB integration details
3. **Explore [GitHub Actions Guide](github-actions-guide.md)** - Advanced CI/CD features
4. **Contribute** - Follow established patterns and quality standards

## 📝 Documentation Standards

Each documentation file includes:
- **Clear overview** with purpose and scope
- **Detailed instructions** with command examples
- **Troubleshooting sections** with common issues
- **Best practices** and recommended approaches
- **Cross-references** to related documentation

## 🔄 Continuous Improvement

The development documentation is continuously updated to reflect:
- **New features** and enhancements
- **Testing improvements** and coverage expansion
- **Automation enhancements** and script improvements
- **Best practice evolution** and standard updates
- **Community feedback** and contribution guidelines

For the most current information, always refer to the individual documentation files linked above.