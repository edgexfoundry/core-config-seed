/*******************************************************************************
 * Copyright 2016-2017 Dell Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @microservice:  core-config-seed
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
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.orbitz.consul.Consul;
import com.orbitz.consul.ConsulException;
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

	private PropertiesProvider propertyBasedPropertiesProvider = new PropertyBasedPropertiesProvider();
	private PropertiesProvider yamlBasedPropertiesProvider = new YamlBasedPropertiesProvider();

	public void run(String... args) throws Exception {

		LOGGER.info("Connecting to Consul client at " + host + ":" + port);

		Consul consul;
		try {
			consul = connectToConsul();
		} catch (Exception e) {
			LOGGER.error("cannot connect to Consul");
			throw e;
		}

		KeyValueClient kvClient = consul.keyValueClient();

		if (!isReset) {
			try {
				List<String> configList = kvClient.getKeys(globalPrefix);
				if (!configList.isEmpty()) {
					LOGGER.info(globalPrefix + " exists! The configuration data has been initialized.");
					return;
				}
			} catch (NotFoundException e) {
				LOGGER.info(globalPrefix + " doesn't exist! Start importing configuration data.");
			}
		} else
			kvClient.deleteKeys(globalPrefix);

		Path start = Paths.get(configPath);
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

				if (file.toString().endsWith(".yml") || file.toString().endsWith(".yaml")
						|| file.toString().endsWith("properties")) {
					LOGGER.info("found config file: " + file.getFileName().toString() + " in context "
							+ start.relativize(file.getParent()).toString());

					try (InputStream input = new FileInputStream(file.toFile())) {

						Properties properties = new Properties();
						PropertiesProvider provider = selectPropertiesProviderByExtention(
								file.getFileName().toString());
						properties.putAll(provider.getProperties(input));

						String prefix = start.relativize(file.getParent()).toString();
						
						properties.entrySet().stream().parallel().forEach( prop -> {
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

	private Consul connectToConsul() throws MalformedURLException, InterruptedException {
		Consul consul;

		RestTemplate restTemplate = new RestTemplate();
		int connectFailedCount = 0;
		while (true) {
			try {
				Thread.sleep(3000L);
				ResponseEntity<String> response = restTemplate
						.getForEntity(new URL(protocol, host, port, CONSUL_STATUS_PATH).toString(), String.class);
				if (!response.getStatusCode().is2xxSuccessful())
					continue;
				consul = Consul.builder().withUrl(new URL(protocol, host, port, "")).build();
				break;
			} catch (ResourceAccessException | ConsulException e) {
				if (connectFailedCount > 30) {
					LOGGER.error("timeout (90 seconds) for connecting to Consul");
					throw e;
				}
				LOGGER.info("waiting for Consul fully starts up...");
				Thread.sleep(3000L);
				connectFailedCount++;
				continue;
			}
		}

		return consul;
	}

	private PropertiesProvider selectPropertiesProviderByExtention(String fileName) {
		if (fileName.endsWith(".yml") || fileName.endsWith(".yaml")) {
			return yamlBasedPropertiesProvider;
		} else {
			return propertyBasedPropertiesProvider;
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(EdgeXConfigSeedApplication.class, args);
	}
}
