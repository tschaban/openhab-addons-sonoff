# GitHub Actions Integration Guide

Complete guide for understanding, using, and customizing the GitHub Actions workflows in the Sonoff Binding Smart'nyDom Enhanced Edition.

## 🎯 Overview

The project includes two main GitHub Actions workflows:
- **CI/CD Pipeline** (`ci.yml`) - Automated testing and quality assurance
- **Release Pipeline** (`release.yml`) - Automated release creation and publishing

These workflows provide enterprise-grade automation for code quality, testing, and deployment.

## 🔄 CI/CD Pipeline Workflow

### File: `.github/workflows/ci.yml`

#### Automatic Triggers
```yaml
on:
  push:
    branches: [ main, master, develop ]
  pull_request:
    branches: [ main, master ]
  workflow_dispatch:
```

**When it runs:**
- Every push to main, master, or develop branches
- Every pull request to main or master
- Manual trigger via GitHub UI

### Workflow Jobs Breakdown

#### 1. Code Quality Checks
```yaml
name: Code Quality Checks
runs-on: ubuntu-latest
```

**What it does:**
- Validates code formatting with Spotless
- Checks Maven POM structure
- Ensures code meets OpenHAB standards

**Commands executed:**
```bash
mvn spotless:check
mvn validate
```

#### 2. Unit Tests (Matrix Strategy)
```yaml
strategy:
  matrix:
    java: [17, 21]
  fail-fast: false
```

**What it does:**
- Runs complete test suite on Java 17 and 21
- Executes 100+ test methods across 6 test classes
- Generates detailed test reports
- Uploads test artifacts for analysis

**Key features:**
- **Parallel execution** on different Java versions
- **Comprehensive reporting** with JUnit XML
- **Artifact collection** for offline analysis
- **Fail-fast disabled** for complete coverage

#### 3. Component-Specific Tests
```yaml
strategy:
  matrix:
    component: 
      - "SonoffCacheProvider*"
      - "SonoffHandlerFactory*" 
      - "SonoffDiscoveryService*"
```

**What it does:**
- Tests each major component independently
- Provides focused feedback on component health
- Enables parallel execution for faster results
- Isolates component failures for easier debugging

#### 4. Integration Tests
```yaml
- name: Run integration tests
  run: mvn test -Dtest="*IntegrationTest" -B
  
- name: Run error handling tests
  run: mvn test -Dtest="*ErrorHandlingTest" -B
```

**What it does:**
- Validates real-world integration scenarios
- Tests error handling and edge cases
- Verifies file system operations
- Confirms resource cleanup

#### 5. Build Verification
```yaml
- name: Compile and package
  run: mvn clean compile package -DskipTests -DskipChecks
  
- name: Verify JAR creation
  run: |
    if [ -f target/*.jar ]; then
      echo "✅ JAR file created successfully"
      ls -la target/*.jar
    else
      echo "❌ JAR file not found"
      exit 1
    fi
```

**What it does:**
- Compiles source code without tests
- Creates deployable JAR file
- Validates build artifacts
- Uploads JAR for download

#### 6. Cross-Platform Tests
```yaml
strategy:
  matrix:
    os: [ubuntu-latest, windows-latest, macos-latest]
```

**What it does:**
- Verifies core functionality across platforms
- Tests Java compatibility on different OS
- Ensures consistent behavior
- Validates environment independence

#### 7. Test Summary
```yaml
- name: Display test summary
  run: |
    echo "## 🧪 Test Execution Summary" >> $GITHUB_STEP_SUMMARY
    echo "- **Unit Tests**: ${{ needs.unit-tests.result }}" >> $GITHUB_STEP_SUMMARY
```

**What it does:**
- Aggregates results from all jobs
- Creates visual summary in GitHub UI
- Reports overall pipeline status
- Provides quick status overview

## 🎉 Release Pipeline Workflow

### File: `.github/workflows/release.yml`

#### Automatic Triggers
```yaml
on:
  push:
    tags:
      - 'v*'
  workflow_dispatch:
    inputs:
      version:
        description: 'Release version (e.g., v1.0.0)'
        required: true
        type: string
```

**When it runs:**
- When you create a git tag starting with 'v' (e.g., `git tag v1.0.0`)
- Manual trigger with custom version input

### Release Jobs Breakdown

#### 1. Pre-Release Tests
**Mirrors:** `task-run-unit-tests.ps1` functionality

```yaml
- name: Run comprehensive test suite
  run: |
    echo "🧪 Running comprehensive test suite..."
    mvn spotless:apply
    mvn test -B
```

**Quality gates:**
- All tests must pass
- Code formatting must be valid
- Build must succeed

#### 2. Create Release
**Mirrors:** `task-run-deploy.ps1` functionality

```yaml
- name: Build release artifacts
  run: |
    mvn spotless:apply
    mvn test -B
    mvn clean install -DskipChecks -DskipTests
```

**What it creates:**
- GitHub release with professional notes
- JAR artifact with Smart'nyDom branding
- Download links and documentation
- Version-specific release information

#### 3. Professional Release Notes
```yaml
body: |
  ## 🚀 Sonoff Binding - Smart'nyDom Enhanced Edition ${{ steps.version.outputs.version }}
  
  ### 📦 What's Included
  - Enhanced Sonoff binding with extended device support
  - Comprehensive testing framework with 100+ test methods
  - Quality-assured build with automated CI/CD pipeline
```

**Features:**
- Professional formatting with emojis
- Comprehensive feature descriptions
- Installation instructions
- Quality assurance information
- Attribution to Smart'nyDom and original authors

## 🛠️ Using the Workflows

### For Developers

#### Running Tests Automatically
1. **Push code** to main, master, or develop branch
2. **Watch GitHub Actions** tab for automatic execution
3. **Review results** in the workflow summary
4. **Download artifacts** if needed for analysis

#### Creating Pull Requests
1. **Create feature branch** from main
2. **Make changes** and push to feature branch
3. **Create pull request** to main branch
4. **GitHub Actions runs automatically** on PR
5. **Review test results** before merging

#### Manual Workflow Execution
1. Go to **Actions** tab in GitHub
2. Select **CI/CD Pipeline** workflow
3. Click **Run workflow** button
4. Choose branch and click **Run workflow**

### For Maintainers

#### Creating Releases
**Method 1: Git Tags**
```bash
# Create and push tag
git tag v1.0.0
git push origin v1.0.0
```

**Method 2: Manual Workflow**
1. Go to **Actions** tab
2. Select **Release Pipeline**
3. Click **Run workflow**
4. Enter version (e.g., v1.0.0)
5. Click **Run workflow**

#### Monitoring Workflow Health
- **Check Actions tab** regularly for failures
- **Review test trends** over time
- **Monitor artifact sizes** and retention
- **Update workflows** as needed

## 📊 Understanding Workflow Results

### Success Indicators
- ✅ **Green checkmarks** on all jobs
- 📊 **Test summary** showing passed tests
- 📦 **Artifacts uploaded** successfully
- 🎯 **All quality gates passed**

### Failure Analysis
- ❌ **Red X marks** indicate failures
- 📋 **Click job name** to see detailed logs
- 🔍 **Review error messages** and stack traces
- 📁 **Download artifacts** for offline analysis

### Workflow Status Badges
Add to README for visual status:
```markdown
[![CI/CD Pipeline](https://github.com/tschaban/openhab-addons-sonoff/actions/workflows/ci.yml/badge.svg)](https://github.com/tschaban/openhab-addons-sonoff/actions/workflows/ci.yml)
[![Release Pipeline](https://github.com/tschaban/openhab-addons-sonoff/actions/workflows/release.yml/badge.svg)](https://github.com/tschaban/openhab-addons-sonoff/actions/workflows/release.yml)
```

## 🔧 Customizing Workflows

### Adding New Test Categories
```yaml
# In ci.yml, add to component-tests matrix
strategy:
  matrix:
    component: 
      - "SonoffCacheProvider*"
      - "SonoffHandlerFactory*" 
      - "SonoffDiscoveryService*"
      - "YourNewComponent*"  # Add here
```

### Modifying Java Versions
```yaml
# In ci.yml, modify unit-tests matrix
strategy:
  matrix:
    java: [17, 21, 22]  # Add Java 22
```

### Adding Notification Integrations
```yaml
# Add to end of jobs
- name: Notify Slack
  if: failure()
  uses: 8398a7/action-slack@v3
  with:
    status: failure
    webhook_url: ${{ secrets.SLACK_WEBHOOK }}
```

### Custom Artifact Retention
```yaml
# Modify retention-days in upload-artifact actions
- name: Upload test results
  uses: actions/upload-artifact@v3
  with:
    name: test-results
    path: target/surefire-reports/
    retention-days: 7  # Change from 30 to 7
```

## 🔒 Security and Secrets

### Required Secrets
The workflows use these GitHub secrets:
- `GITHUB_TOKEN` - Automatically provided by GitHub
- No additional secrets required for basic functionality

### Optional Secrets for Enhancements
- `SLACK_WEBHOOK` - For Slack notifications
- `DISCORD_WEBHOOK` - For Discord notifications
- `SONAR_TOKEN` - For SonarQube integration

### Setting Up Secrets
1. Go to **Settings** → **Secrets and variables** → **Actions**
2. Click **New repository secret**
3. Add name and value
4. Reference in workflow: `${{ secrets.SECRET_NAME }}`

## 📈 Performance Optimization

### Caching Strategies
```yaml
# Maven dependency caching
- name: Cache Maven dependencies
  uses: actions/cache@v3
  with:
    path: ~/.m2
    key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
    restore-keys: ${{ runner.os }}-m2
```

### Parallel Execution
- **Matrix strategies** for concurrent testing
- **Independent jobs** for parallel execution
- **Fail-fast disabled** for complete coverage
- **Component isolation** for focused testing

### Resource Management
- **Artifact retention policies** for storage optimization
- **Conditional job execution** to save resources
- **Efficient caching** for faster builds
- **Optimized Docker images** when applicable

## 🚀 Advanced Features

### Conditional Execution
```yaml
# Only run on specific conditions
- name: Deploy to staging
  if: github.ref == 'refs/heads/develop'
  run: echo "Deploying to staging"
```

### Environment-Specific Jobs
```yaml
# Different behavior per environment
jobs:
  test-dev:
    if: github.ref == 'refs/heads/develop'
    # Development-specific testing
    
  test-prod:
    if: github.ref == 'refs/heads/main'
    # Production-specific testing
```

### Dynamic Matrix Generation
```yaml
# Generate matrix from file or API
strategy:
  matrix:
    include: ${{ fromJson(needs.setup.outputs.matrix) }}
```

## 🛠️ Troubleshooting

### Common Issues

#### Workflow Not Triggering
- **Check branch names** in trigger configuration
- **Verify push permissions** for the repository
- **Review workflow file syntax** for YAML errors

#### Test Failures
- **Review job logs** for specific error messages
- **Check test reports** in artifacts
- **Verify local test execution** before pushing

#### Artifact Upload Failures
- **Check file paths** in upload configuration
- **Verify artifact size limits** (GitHub has limits)
- **Review retention policies** for storage issues

#### Permission Issues
- **Verify GITHUB_TOKEN** permissions
- **Check repository settings** for Actions permissions
- **Review branch protection rules** if applicable

### Getting Help

#### GitHub Actions Documentation
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Workflow Syntax](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions)
- [Marketplace Actions](https://github.com/marketplace?type=actions)

#### Project-Specific Support
- **GitHub Issues** for workflow problems
- **Discussion section** for questions
- **Local PowerShell scripts** for testing workflow logic

This comprehensive guide provides everything needed to understand, use, and customize the GitHub Actions workflows for professional development and deployment of the Sonoff Binding Smart'nyDom Enhanced Edition.