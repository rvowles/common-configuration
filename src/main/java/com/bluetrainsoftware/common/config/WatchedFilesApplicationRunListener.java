package com.bluetrainsoftware.common.config;

import net.stickycode.configured.ConfigurationRepository;
import net.stickycode.configured.ConfigurationSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This looks for configuration parameters that have been specified on the command line. It is specifically designed
 * to work with Spring Boot style applications.
 *
 * Deliberately NOT using the WatchService as it is too complex and incorrect events for this.
 *
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
public class WatchedFilesApplicationRunListener implements SpringApplicationRunListener {
  private static final Logger log = LoggerFactory.getLogger(WatchedFilesApplicationRunListener.class);

  protected ConfigurationSystem system;
  protected Set<File> watchedFiles = new HashSet<>();
  protected Map<File, Long> lastModified = new HashMap<>();
  protected int watchTimeout = 15; // seconds
  protected YamlPropertySourceLoader yamlPropertySourceLoader = new YamlPropertySourceLoader();
  protected PropertiesPropertySourceLoader propertiesPropertySourceLoader = new PropertiesPropertySourceLoader();
  protected boolean requiresReloading = false;

  public WatchedFilesApplicationRunListener(SpringApplication application, String []args) {
    determineWatchedFileListFromCommandLineArguments(args);
  }

  protected void determineWatchedFileListFromCommandLineArguments(String[] args) {
    Arrays.stream(args).forEach(arg -> {
      if (arg.startsWith("-P")) {
        File props = new File(arg.substring(2));

        if (props.exists() && props.canRead() && ( props.getName().endsWith(".yml") || props.getName().endsWith(".properties")) ) {
          watchedFiles.add(props);
        } else {
          log.error("Requested property file `{}` to be loaded but does not exist, cannot read or not yaml or properties.", props.getAbsolutePath());
        }
      }
    });
  }

  protected void loadWatchedFiles() {
    requiresReloading = false;

    Set<File> newWatchedFiles = watchedFiles.stream().map(propertyFile -> {
      File newFile = new File(propertyFile.getAbsolutePath());

      Long previousLastModifiedTime = lastModified.get(propertyFile);

      if (newFile.exists() && (previousLastModifiedTime == null || newFile.lastModified() != previousLastModifiedTime)) {
        log.info("Loading configuration `{}` into system properties", newFile.getAbsolutePath());

        MapPropertySource propertySource = loadPropertyFile(newFile.getName().endsWith(".yml")
          ? yamlPropertySourceLoader : propertiesPropertySourceLoader, newFile);

        lastModified.remove(propertyFile);

        if (propertySource == null) {
          return null;
        } else {
          lastModified.put(newFile, newFile.lastModified());
        }

        // merge them in
	      propertySource.getSource().entrySet().stream().forEach(prop -> {
		      System.setProperty(prop.getKey(), prop.getValue().toString());
	      });

        requiresReloading = true;

        return newFile;
      }

      return propertyFile;
    })
      .filter(pf -> pf != null) // clear out failed ones
      .collect(Collectors.toSet());

    watchedFiles = newWatchedFiles;

    checkForTimerOverride();
  }

  protected void checkForTimerOverride() {
    String timeout = System.getProperty("sticky.timeout");
    if (timeout != null) {
      try {
        watchTimeout = Integer.parseInt(timeout);
      } catch (Exception ex) {
        // ignore failures
      }
    }
  }

  private MapPropertySource loadPropertyFile(PropertySourceLoader loader, File newFile) {
    try {
      return MapPropertySource.class.cast(loader.load(newFile.getName(), new FileSystemResource(newFile), null));
    } catch (IOException e) {
      log.error("Unable to load `{}`", newFile.getAbsolutePath());
      return null;
    }
  }

  // 1.3 and 1.4
  public void started() {
  }

	// 1.5
	public void starting() {
	}

	@Override
  public void environmentPrepared(ConfigurableEnvironment configurableEnvironment) {
    loadWatchedFiles();
  }

  @Override
  public void contextPrepared(ConfigurableApplicationContext configurableApplicationContext) {
  }

  @Override
  public void contextLoaded(ConfigurableApplicationContext configurableApplicationContext) {
  }

  @Override
  public void finished(ConfigurableApplicationContext configurableApplicationContext, Throwable throwable) {
    system = configurableApplicationContext.getBean(ConfigurationSystem.class);

    if (system != null && watchedFiles.size() > 0 && watchTimeout > 0) {
      new Thread(() -> {
        while (true) {
          try {
            Thread.sleep(watchTimeout * 1000);
          } catch (InterruptedException e) {
            return;
          }

          loadWatchedFiles();

          if (requiresReloading) {
            system.start();
          }
        }
      }).start();
    }
  }
}
