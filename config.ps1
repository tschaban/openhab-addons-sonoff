# Sonoff Binding Default Configuration
# This file contains default configuration parameters for build and deployment scripts
# 
# To override these settings:
# 1. Create a .ps1 file in the conf.d/ directory
# 2. Define only the variables you want to override
# 3. Custom configurations in conf.d/ take precedence over defaults here

# JAR File Names
$sourceJAR = "org.openhab.binding.sonoff-5.0.3-SNAPSHOT.jar"
$targetJAR = "org.openhab.binding.sonoff-5.0.3-SNAPSHOT.jar"

# Project Paths
$projectPath = "openhab-addons\bundles\org.openhab.binding.sonoff"

# OpenHAB Deployment Paths
$openhabAddonsPath = "addons"

# OpebHAB Logs file
$resetOpenhabLog = 0  # 1 = clear log, 0 = keep log
$logPath = "openhab.log"

