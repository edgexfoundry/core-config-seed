###############################################################################
# Copyright 2016-2017 Dell Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
###############################################################################
# Docker image for EdgeX Foundry Config Seed 
# FROM java:8
FROM maven:3.3-jdk-8-alpine

COPY docker-files/pom.xml .

RUN mvn dependency:copy -q

# Consul Docker image for EdgeX Foundry
FROM consul:0.7.3

RUN apk --update add openjdk8-jre

# environment variables
ENV APP_DIR=/edgex/core-config-seed
ENV APP=core-config-seed.jar

ENV WAIT_FOR_A_WHILE=10

ENV CONSUL_ARGS="-server -client=0.0.0.0 -bootstrap -ui"

#copy JAR and default config files to the image
COPY --from=0 *.jar $APP_DIR/$APP
COPY docker-files/*.properties $APP_DIR/
COPY ./config $APP_DIR/config
COPY docker-files/launch-consul-config.sh $APP_DIR/

#set the working directory
WORKDIR $APP_DIR

#call the wrapper to launch consul and main app
CMD $APP_DIR/launch-consul-config.sh
