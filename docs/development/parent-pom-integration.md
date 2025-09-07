# Parent POM Integration Guide

Comprehensive guide for handling the OpenHAB parent POM dependency in the Sonoff Binding Smart'nyDom Enhanced Edition CI/CD pipeline.

## 🎯 Overview

The Sonoff binding depends on the OpenHAB parent POM from the [openhab/openhab-addons](https://github.com/openhab/openhab-addons) repository. This guide explains how the CI/CD pipeline handles this dependency and provides solutions for different scenarios.

## 📋 Parent POM Structure

### Current Dependency
```xml
<parent>
  <groupId>org.openhab.addons.bundles</groupId>
  <artifactId>org.openhab.addons.reactor.bundles</artifactId>
  <version>5.0.2-SNAPSHOT</version>
</parent>
```

### Parent Repository
- **Repository:** https://github.com/openhab/openhab-addons
- **Branch:** main
- **Path:** `/bundles/pom.xml` (reactor POM)
- **Root:** `/pom.xml` (main parent POM)

## 🔄 CI/CD Solutions

### 1. Enhanced CI Pipeline (`ci-with-parent.yml`)

#### Parent POM Setup Job
```yaml
setup-parent-pom:
  name: Setup Parent POM
  runs-on: ubuntu-latest
  outputs:
    parent-available: ${{ steps.check-parent.outputs.available }}
```

**What it does:**
1. **Clones OpenHAB repository** with minimal depth for efficiency
2. **Installs parent POMs** to local Maven repository
3. **Caches parent installation** for subsequent jobs
4. **Verifies availability** and sets output flag

**Commands executed:**
```bash
# Clone OpenHAB addons repository
git clone --depth 1 --branch main https://github.com/openhab/openhab-addons.git ../openhab-addons

# Install root parent POM
cd ../openhab-addons
mvn install -N -DskipTests -DskipChecks -q

# Install bundles reactor POM
cd bundles
mvn install -N -DskipTests -DskipChecks -q
```

#### Conditional Execution
All subsequent jobs check parent availability:
```yaml
- name: Check code formatting with Spotless
  run: |
    if [ "${{ needs.setup-parent-pom.outputs.parent-available }}" == "true" ]; then
      echo "🎨 Checking code formatting with parent POM..."
      mvn spotless:check
    else
      echo "⚠️ Skipping Spotless check - parent POM not available"
    fi
```

### 2. Release Pipeline (`release-with-parent.yml`)

#### Pre-Release Quality Gate
```yaml
setup-parent-and-test:
  name: Setup Parent POM and Pre-Release Tests
```

**Enhanced features:**
- **Mandatory parent POM** setup for releases
- **Comprehensive testing** before release creation
- **Quality gate enforcement** - release fails if tests fail
- **JAR verification** with parent POM integration

#### Release Creation
```yaml
create-release:
  name: Create GitHub Release
  needs: setup-parent-and-test
```

**Professional release artifacts:**
- **OpenHAB-compatible JAR** built with parent POM
- **Comprehensive release notes** with device lists
- **Quality assurance information** in release description
- **Professional naming** with Smart'nyDom branding

### 3. Fallback Strategy

#### Standalone Build Job
```yaml
fallback-build:
  name: Fallback Build (No Parent POM)
  if: failure() || needs.setup-parent-pom.outputs.parent-available == 'false'
```

**Fallback POM creation:**
```xml
<!-- Minimal standalone POM for testing -->
<project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.openhab.binding</groupId>
  <artifactId>org.openhab.binding.sonoff</artifactId>
  <version>5.0.2-SNAPSHOT</version>
  <packaging>jar</packaging>
  
  <!-- Essential dependencies only -->
  <dependencies>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <version>5.10.1</version>
      <scope>test</scope>
    </dependency>
    <!-- ... other essential dependencies -->
  </dependencies>
</project>
```

## 🛠️ Local Development Integration

### PowerShell Scripts Enhancement

#### Updated task-run-unit-tests.ps1
```powershell
# Check for parent POM availability
Write-Host "Checking OpenHAB parent POM availability..." -ForegroundColor Yellow

try {
    $null = mvn help:effective-pom -q 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] Parent POM is available" -ForegroundColor Green
        $parentAvailable = $true
    } else {
        throw "Parent POM not available"
    }
} catch {
    Write-Host "[WARNING] Parent POM not available in current environment" -ForegroundColor Yellow
    Write-Host "This is expected when running outside the full OpenHAB project" -ForegroundColor Yellow
    $parentAvailable = $false
}

# Conditional Spotless execution
if ($parentAvailable) {
    Write-Host "Running Spotless with parent POM configuration..." -ForegroundColor Cyan
    mvn spotless:apply
} else {
    Write-Host "Skipping Spotless - parent POM configuration not available" -ForegroundColor Yellow
}
```

#### Updated task-run-deploy.ps1
```powershell
# Enhanced parent POM handling in deployment script
Write-Host "[INFO] Checking OpenHAB integration compatibility..." -ForegroundColor Cyan

if ($parentAvailable) {
    Write-Host "[OK] Full OpenHAB integration available" -ForegroundColor Green
    Write-Host "Building with complete parent POM configuration" -ForegroundColor Gray
    mvn clean install -DskipChecks -DskipTests -pl :org.openhab.binding.sonoff
} else {
    Write-Host "[WARNING] Limited integration - building standalone JAR" -ForegroundColor Yellow
    Write-Host "JAR will be compatible but may lack some OpenHAB-specific features" -ForegroundColor Gray
    mvn clean install -DskipChecks -DskipTests -Dmaven.javadoc.skip=true
}
```

## 📊 Caching Strategy

### Maven Repository Caching
```yaml
- name: Cache Maven dependencies
  uses: actions/cache@v3
  with:
    path: ~/.m2
    key: ${{ runner.os }}-m2-parent-${{ hashFiles('**/pom.xml') }}
    restore-keys: |
      ${{ runner.os }}-m2-parent-
      ${{ runner.os }}-m2-
```

### OpenHAB Parent POM Caching
```yaml
- name: Cache parent POM installation
  uses: actions/cache@v3
  with:
    path: ~/.m2/repository/org/openhab
    key: ${{ runner.os }}-openhab-parent-${{ env.OPENHAB_VERSION }}
    restore-keys: ${{ runner.os }}-openhab-parent-
```

**Benefits:**
- **Faster builds** - Parent POM only downloaded once per version
- **Reduced network usage** - Cached across workflow runs
- **Improved reliability** - Less dependency on external repository availability

## 🔧 Configuration Options

### Environment Variables
```yaml
env:
  MAVEN_OPTS: -Xmx1024m
  OPENHAB_VERSION: 5.0.2-SNAPSHOT
  OPENHAB_REPO: https://github.com/openhab/openhab-addons.git
  OPENHAB_BRANCH: main
```

### Customizable Parameters
- **OpenHAB version** - Update when OpenHAB releases new versions
- **Repository URL** - Can point to forks if needed
- **Branch selection** - Use different branches for testing
- **Cache duration** - Adjust retention policies

## 🚀 Advanced Features

### Multi-Version Support
```yaml
strategy:
  matrix:
    openhab-version: [5.0.2-SNAPSHOT, 5.1.0-SNAPSHOT]
```

**Benefits:**
- **Forward compatibility** testing
- **Version migration** validation
- **Regression detection** across versions

### Conditional Parent POM Updates
```yaml
- name: Check for parent POM updates
  run: |
    CURRENT_COMMIT=$(git ls-remote https://github.com/openhab/openhab-addons.git HEAD | cut -f1)
    CACHED_COMMIT=$(cat ~/.m2/.openhab-commit 2>/dev/null || echo "none")
    
    if [ "$CURRENT_COMMIT" != "$CACHED_COMMIT" ]; then
      echo "🔄 Parent POM updated, refreshing cache..."
      # Re-install parent POM
      echo "$CURRENT_COMMIT" > ~/.m2/.openhab-commit
    else
      echo "✅ Parent POM cache is up to date"
    fi
```

## 🛠️ Troubleshooting

### Common Issues

#### Parent POM Not Found
```bash
# Error message
[ERROR] Non-resolvable parent POM for org.openhab.binding:org.openhab.binding.sonoff

# Solution
git clone --depth 1 https://github.com/openhab/openhab-addons.git ../openhab-addons
cd ../openhab-addons
mvn install -N -DskipTests -DskipChecks
cd bundles
mvn install -N -DskipTests -DskipChecks
```

#### Spotless Configuration Missing
```bash
# Error message
[ERROR] Unable to load the mojo 'spotless:check'

# Solution - Run with fallback
mvn test -DskipChecks -Dmaven.javadoc.skip=true
```

#### Version Mismatch
```bash
# Error message
[ERROR] The project org.openhab.binding:org.openhab.binding.sonoff:5.0.2-SNAPSHOT 
has 1 error: Non-resolvable parent POM

# Solution - Update parent version
# Check latest version in openhab-addons repository
curl -s https://api.github.com/repos/openhab/openhab-addons/contents/pom.xml | \
  grep -o '"version":[^,]*' | head -1
```

### Debug Commands
```bash
# Check effective POM
mvn help:effective-pom

# Verify parent POM installation
ls -la ~/.m2/repository/org/openhab/addons/

# Check dependency tree
mvn dependency:tree

# Validate POM structure
mvn validate -X
```

## 📈 Performance Optimization

### Build Time Optimization
- **Shallow clones** (`--depth 1`) for faster repository cloning
- **Parallel builds** where possible
- **Selective installation** (`-N` flag) for parent POMs only
- **Skip unnecessary phases** (`-DskipTests -DskipChecks`)

### Cache Optimization
- **Layered caching** - Separate caches for dependencies and parent POM
- **Version-specific keys** - Avoid cache invalidation on version updates
- **Restore fallbacks** - Multiple restore keys for cache hits

### Network Optimization
- **Single repository clone** per workflow run
- **Cached parent installations** across jobs
- **Minimal data transfer** with depth-limited clones

## 🔄 Maintenance

### Regular Updates
1. **Monitor OpenHAB releases** for parent POM version updates
2. **Update OPENHAB_VERSION** environment variable when needed
3. **Test compatibility** with new parent POM versions
4. **Update documentation** when parent structure changes

### Version Management
```bash
# Check current OpenHAB version
curl -s https://api.github.com/repos/openhab/openhab-addons/releases/latest | \
  grep '"tag_name"' | cut -d'"' -f4

# Update environment variable in workflows
sed -i 's/OPENHAB_VERSION: .*/OPENHAB_VERSION: NEW_VERSION/' .github/workflows/*.yml
```

## 🎯 Best Practices

### Development Workflow
1. **Test locally** with parent POM when possible
2. **Use fallback mode** for quick development iterations
3. **Validate integration** before creating releases
4. **Monitor CI/CD** for parent POM resolution issues

### CI/CD Management
1. **Use conditional execution** for parent-dependent features
2. **Implement fallback strategies** for reliability
3. **Cache aggressively** for performance
4. **Monitor external dependencies** for availability

### Release Management
1. **Ensure parent POM** availability before releases
2. **Test full integration** in release pipeline
3. **Document compatibility** in release notes
4. **Maintain version alignment** with OpenHAB releases

This comprehensive parent POM integration ensures reliable CI/CD operation while maintaining full OpenHAB compatibility and providing fallback options for development flexibility.