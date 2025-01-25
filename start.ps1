
clear
mvn spotless:apply
mvn clean install -DskipChecks -DskipTests -pl :org.openhab.binding.sonoff
Copy-Item -Path "D:\Adrian\Projekty\openhab\openhab-addons\bundles\org.openhab.binding.sonoff\target\org.openhab.binding.sonoff-4.3.3-SNAPSHOT.jar" -Destination "O:\configuration\addons\org.openhab.binding.sonoff-4.3.3-SmartnyDom-v0.x.jar"
Set-Content -Path "O:\configuration\logs\openhab.log" -Value ""