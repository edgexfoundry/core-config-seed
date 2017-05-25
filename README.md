Main Author:  Cloud Tsai

Copyright 2016-17, Dell, Inc.

This repository is for initializing the Configuration Management micro service.
It loads the default configuration from property or YAML files, and push values to the Consul Key/Value store.


### Configuration Guidelines ###

The configuration of this tool is located in src/main/java/resources/application.properties.
There are six properties in it, and here are the default values and explanation:

\#The root path of the configuration files which would be loaded by this tool
configPath=./config

\#The global prefix namespace which will be created on the Consul Key/Value store
globalPrefix=config

\#The communication protocol of the Consul server   
consul.protocol=http

\#The hostname of the Consul server  
consul.host=localhost

\#The communication port number of the Consul server  
consul.port=8500

\#If isReset=true, it will remove all the original values under the globalPrefix and import the configuration data
\#If isReset=false, it will check the globalPrefix exists or not, and it only imports configuration data when the globalPrefix doesn't exist. 
isReset=false

### Configuration File Structure ###

In /config folder, there are some sample files for testing.
The structure of the keys on the Consul server will be the same as the folders of the configPath, and the folder name must be the same as the microservice id  registered on the Consul server.

For example, the files under /config/edgex-core-data folder will be loaded and create /{global_prefix}/edgex-core-data/{property_name} on the Consul server.
In addition, "edgex-core-data" is the micro service id of Core Data micro service.

However, you can use different profile name to categorize the usage on the same microservice. For instance,
"/config/edgex-core-data" contains the default configuration of Core Data Microservice.
"/config/edgex-core-data,dev" contains the specific configuration for development time, and "dev" is the profile name.
"/config/edgex-core-data,test" contains the specific configuration for test time, and "test" is the profile name.

### How to execute this tool? ###

After building this tool via Maven, just execute "Run As > Spring Boot App" in eclipse or "java -jar edgex-core-config-seed-{version_number}.jar  --configPath={folder_path}" on command line