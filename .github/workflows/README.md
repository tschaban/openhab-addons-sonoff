# GitHub Actions Workflows

This directory contains automated CI/CD workflows for the Sonoff Binding Smart'nyDom Enhanced Edition.

## 🔄 Available Workflows

### 1. CI/CD Pipeline (`ci.yml`)
**Triggers:** Push to main/master/develop, Pull Requests
**Purpose:** Comprehensive testing and quality assurance

**Jobs:**
- **Code Quality Checks** - Spotless formatting validation
- **Unit Tests** - Full test suite on Java 17 & 21
- **Component Tests** - Individual component testing
- **Integration Tests** - Real-world scenario testing
- **Build Verification** - JAR creation and validation
- **Cross-Platform Tests** - Ubuntu, Windows, macOS compatibility
- **Test Summary** - Comprehensive results overview

### 2. Release Pipeline (`release.yml`)
**Triggers:** Git tags (v*), Manual workflow dispatch
**Purpose:** Automated release creation and artifact publishing

**Jobs:**
- **Pre-Release Tests** - Quality gate before release
- **Create Release** - GitHub release with JAR artifact
- **Post-Release Tasks** - Summary and documentation links

## 🎯 Integration with Existing Scripts

The workflows mirror your existing PowerShell scripts:
- **CI Pipeline** ≈ `task-run-unit-tests.ps1` functionality
- **Release Pipeline** ≈ `task-run-deploy.ps1` functionality

## 📊 Quality Gates

All workflows enforce quality gates:
- ✅ Code formatting must pass (Spotless)
- ✅ All unit tests must pass (100+ test methods)
- ✅ Integration tests must pass
- ✅ Build must succeed
- ✅ Cross-platform compatibility verified

## 🔗 Status Badges

Add these badges to your README for workflow status:

```markdown
[![CI/CD Pipeline](https://github.com/tschaban/openhab-addons-sonoff/actions/workflows/ci.yml/badge.svg)](https://github.com/tschaban/openhab-addons-sonoff/actions/workflows/ci.yml)
[![Release Pipeline](https://github.com/tschaban/openhab-addons-sonoff/actions/workflows/release.yml/badge.svg)](https://github.com/tschaban/openhab-addons-sonoff/actions/workflows/release.yml)
```