/*******************************************************************************
 * Copyright 2016-2017 Dell Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 * @microservice: core-config-seed
 * @author: Cloud Tsai, Dell
 * @version: 1.0.0
 *******************************************************************************/

package org.edgexfoundry;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.NotFoundException;

import org.cfg4j.source.context.propertiesprovider.PropertiesProvider;
import org.cfg4j.source.context.propertiesprovider.PropertyBasedPropertiesProvider;
import org.cfg4j.source.context.propertiesprovider.YamlBasedPropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;

@SpringBootApplication
public class EdgeXConfigSeedApplication implements CommandLineRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(EdgeXConfigSeedApplication.class);

  private static final String CONSUL_STATUS_PATH = "/v1/agent/self";

  @Value("${configPath}")
  private String configPath;

  @Value("${globalPrefix}")
  private String globalPrefix;

  @Value("${consul.protocol}")
  private String protocol;

  @Value("${consul.host}")
  private String host;

  @Value("${consul.port}")
  private int port;

  @Value("${isReset}")
  private boolean isReset;

  @Value("${fail.limit:30}")
  private int failLimit;

  @Value("${fail.waittime:3}")
  private int waitTimeBetweenFails;

  @Value("#{'${acceptable.property.extensions}'.split(',')}")
  private List<String> acceptablePropertyExtensions = new ArrayList<>();

  @Value("#{'{$yaml.extensions}'.split(',')}")
  private List<String> yamlExtensions = new ArrayList<>();

  private PropertiesProvider propertyBasedPropertiesProvider =
      new PropertyBasedPropertiesProvider();
  private PropertiesProvider yamlBasedPropertiesProvider = new YamlBasedPropertiesProvider();

  public void run(String... args) throws InterruptedException, IOException {
    Consul consul = consulWithRetry();
    if (consul != null) {
      KeyValueClient kvClient = consul.keyValueClient();
      if (isReset) {
        kvClient.deleteKeys(globalPrefix);
      } else if (!isConfigInitialized(kvClient)) {
        loadConfigFromPath(kvClient);
      }
    }
  }

  public void loadConfigFromPath(KeyValueClient kvClient) throws IOException {
    Path start = Paths.get(configPath);
    Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

        if (acceptablePropertyExtensions.stream()
            .filter(s -> file.toString().toLowerCase().contains(s)).count() > 0) {
          LOGGER.info("found config file: " + file.getFileName().toString() + " in context "
              + start.relativize(file.getParent()).toString());

          try (InputStream input = new FileInputStream(file.toFile())) {

            Properties properties = new Properties();
            PropertiesProvider provider =
                selectPropertiesProviderByExtention(file.getFileName().toString());
            properties.putAll(provider.getProperties(input));

            String prefix = start.relativize(file.getParent()).toString();

            properties.entrySet().stream().parallel().forEach(prop -> {
              LOGGER.debug("found property name: " + prop.getKey().toString() + " ; and value: "
                  + prop.getValue().toString());
              kvClient.putValue(globalPrefix + "/" + prefix + "/" + prop.getKey().toString(),
                  prop.getValue().toString());
            });

          } catch (IOException e) {
            throw new IllegalStateException("Unable to load properties from file: " + file, e);
          }
        }
        return FileVisitResult.CONTINUE;
      }
    });
  }

  public boolean isConfigInitialized(KeyValueClient kvClient) {
    try {
      List<String> configList = kvClient.getKeys(globalPrefix);
      if (!configList.isEmpty()) {
        LOGGER.info("%s exists! The configuration data has been initialized.", globalPrefix);
        return true;
      }
    } catch (NotFoundException e) {
      LOGGER.info("%s doesn't exist! Start importing configuration data.", globalPrefix);
    }
    return false;
  }

  public Consul consulWithRetry() throws InterruptedException {
    int fails = 0;
    LOGGER.info("Connecting to Consul at %s: %d", host, port);
    Consul consul;
    while (fails < failLimit) {
      consul = getConsul();
      if (consul != null)
        return consul;
      fails++;
      Thread.sleep(waitTimeBetweenFails * 1000L);
    }
    LOGGER.error("Failed to connect to Consul in allowed retries.");
    return null;
  }

  public Consul getConsul() {
    LOGGER.info("Attempting to get consul connection.");
    RestTemplate restTemplate = new RestTemplate();
    try {
      ResponseEntity<String> response = restTemplate
          .getForEntity(new URL(protocol, host, port, CONSUL_STATUS_PATH).toString(), String.class);
      if (response.getStatusCode().is2xxSuccessful())
        return Consul.builder().withUrl(new URL(protocol, host, port, "")).build();
      return null;
    } catch (RestClientException | MalformedURLException e) {
      LOGGER.error(
          "Malformed URL or other issue when attempting to get to Consul: " + e.getMessage());
      return null;
    }
  }

  public PropertiesProvider selectPropertiesProviderByExtention(String fileName) {
    if (yamlExtensions.stream().filter(s -> fileName.toLowerCase().contains(s))
        .count() > 0) {
      return yamlBasedPropertiesProvider;
    } else {
      return propertyBasedPropertiesProvider;
    }
  }

  public static void main(String[] args) {
    SpringApplication.run(EdgeXConfigSeedApplication.class, args);
  }
}
