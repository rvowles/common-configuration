package com.bluetrainsoftware.common.config;

import net.stickycode.configured.ConfigurationSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.MapPropertySource;

import javax.inject.Inject;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
public class BaseConfigurationFileWatcher {
  private static final Logger log = LoggerFactory.getLogger(BatheTimeWatcher.class);
  protected static Set<File> watchedFiles = new HashSet<>();
  protected static Map<File, Long> lastModified = new HashMap<>();
  protected static boolean requiresReloading = false;
  protected static int watchTimeout = 15; // seconds

  @Inject
  public ConfigurationSystem system;

  public void startWatching() {
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


  protected boolean isYaml(File f) {
    String name = f.getName().toLowerCase();
    return name.endsWith(".yml") || name.endsWith(".yaml");
  }

  protected boolean isProperties(File f) {
    String name = f.getName().toLowerCase();
    return name.endsWith(".properties");
  }

  protected void loadWatchedFiles() {
    requiresReloading = false;

    Set<File> newWatchedFiles = watchedFiles.stream().map(propertyFile -> {
      File newFile = new File(propertyFile.getAbsolutePath());

      Long previousLastModifiedTime = lastModified.get(propertyFile);

      if (newFile.exists() && (previousLastModifiedTime == null || newFile.lastModified() != previousLastModifiedTime)) {
        log.info("Loading configuration `{}` into system properties", newFile.getAbsolutePath());

        MapPropertySource propertySource = loadPropertyFile(newFile);

        lastModified.remove(propertyFile);

        if (propertySource == null) {
          return null;
        } else {
          lastModified.put(newFile, newFile.lastModified());
        }

        // merge them in
        propertySource.getSource().forEach((key, value) -> System.setProperty(key, value.toString()));

        requiresReloading = true;

        return newFile;
      }

      return propertyFile;
    })
      .filter(Objects::nonNull) // clear out failed ones
      .collect(Collectors.toSet());

    watchedFiles = newWatchedFiles;

    checkForTimerOverride();
  }

  protected MapPropertySource loadPropertyFile(File propertyFile) {
    return null;
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
}
