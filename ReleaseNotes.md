# v0.2 (10/20/2017)
# Release Notes

## Notable Changes
The Barcelona Release (v 0.2) of the Core Config Seed utility includes the following:
* Application of Google Style Guidelines to the code base
* POM changes for appropriate repository information for distribution/repos management, checkstyle plugins, etc.
* Increase in unit/intergration tests from 0 tests to 10 tests
* Added Dockerfile for creation of micro service targeted for ARM64 
* Added Bluetooth configuration properties
* Combined Consul and config seed into one Docker container

## Bug Fixes
* Fixed virtual device service Docker host name
* Fixed default schedule provisioning for Docker
* Fixed Logging out of memory (default to mongo vs file) configuration

## Pull Request/Commit Details
 - [#19](https://github.com/edgexfoundry/core-config-seed/pull/19) - Remove staging plugin contributed by Jeremy Phelps ([JPWKU](https://github.com/JPWKU))
 - [#18](https://github.com/edgexfoundry/core-config-seed/pull/18) - Fixed an issue discovered in arch64 contributed by Chencho ([chenchix](https://github.com/chenchix))
 - [#17](https://github.com/edgexfoundry/core-config-seed/pull/17) - Fixed consul execution. Missed "agent" keyword in previous PR contributed by Chencho ([chenchix](https://github.com/chenchix))
 - [#16](https://github.com/edgexfoundry/core-config-seed/pull/16) - Merged consul and config-seed into one docker contributed by Chencho ([chenchix](https://github.com/chenchix))
 - [#15](https://github.com/edgexfoundry/core-config-seed/pull/15) - Fixes Maven artifact dependency path contributed by Tyler Cox ([trcox](https://github.com/trcox))
 - [#14](https://github.com/edgexfoundry/core-config-seed/pull/14) - added staging and snapshots repos to pom along with nexus staging mav… contributed by Jim White ([jpwhitemn](https://github.com/jpwhitemn))
 - [#13](https://github.com/edgexfoundry/core-config-seed/pull/13) - Removed device manager url refs in properties files contributed by Jim White ([jpwhitemn](https://github.com/jpwhitemn))
 - [#12](https://github.com/edgexfoundry/core-config-seed/pull/12) - Adds Bluetooth Auto Configuration Properties contributed by Tyler Cox ([trcox](https://github.com/trcox))
 - [#11](https://github.com/edgexfoundry/core-config-seed/pull/11) - added changes for scheduler config properties and tried out logging p… contributed by Jim White ([jpwhitemn](https://github.com/jpwhitemn))
 - [#10](https://github.com/edgexfoundry/core-config-seed/pull/10) - Add aarch64 docker file contributed by ([feclare](https://github.com/feclare))
 - [#9](https://github.com/edgexfoundry/core-config-seed/pull/9) - Docker build contributed by Tyler Cox ([trcox](https://github.com/trcox))
 - [#8](https://github.com/edgexfoundry/core-config-seed/pull/8) - Added unit tests, applied google styles, added checkstyles to pom, ma… contributed by Jim White ([jpwhitemn](https://github.com/jpwhitemn))
 - [#7](https://github.com/edgexfoundry/core-config-seed/pull/7) - Fixes Log File Paths contributed by Tyler Cox ([trcox](https://github.com/trcox))
 - [#6](https://github.com/edgexfoundry/core-config-seed/issues/6) - Log File Path not Platform agnostic
 - [#5](https://github.com/edgexfoundry/core-config-seed/pull/5) - Fixes Logging OOM Configuration contributed by Tyler Cox ([trcox](https://github.com/trcox))
 - [#4](https://github.com/edgexfoundry/core-config-seed/pull/4) - Add distributionManagement for artifact storage contributed by Andrew Grimberg ([tykeal](https://github.com/tykeal))
 - [#3](https://github.com/edgexfoundry/core-config-seed/pull/3) - Fixes default schedule provisioning for docker contributed by Tyler Cox ([trcox](https://github.com/trcox))
 - [#2](https://github.com/edgexfoundry/core-config-seed/pull/2) - Fix virtual device service Docker host name contributed by Tyler Cox ([trcox](https://github.com/trcox))
 - [#1](https://github.com/edgexfoundry/core-config-seed/pull/1) - Contributed Project Fuse source code contributed by Tyler Cox ([trcox](https://github.com/trcox))
