package com.bluetrainsoftware.common.config;

import bathe.BatheInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.parser.ParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
public class BatheTimeWatcher extends BaseConfigurationFileWatcher implements BatheInitializer {
  private static final Logger log = LoggerFactory.getLogger(BatheTimeWatcher.class);
  private static final String MINUS_D = "-D";
  private static final String MINUS_P = "-P";

  @PreStart
  public void start() {
    startWatching();
  }

  @Override
  public int getOrder() {
    return -1;
  }

  @Override
  public String getName() {
    return "bathe-system-property";
  }

  @Override
  public String[] initialize(String[] args, String jumpClass) {
    List<String> appArguments = new ArrayList<>();

    for (String arg : args) {
      if (arg.startsWith(MINUS_D)) {
        String property = arg.substring(MINUS_D.length());
        int equals = property.indexOf('=');
        if (equals >= 0)
          System.setProperty(property.substring(0, equals), property.substring(equals + 1));
        else
          System.setProperty(property, Boolean.TRUE.toString());
      } else if (arg.startsWith(MINUS_P)) {
        try {
          loadProperties(arg.substring(MINUS_P.length()));
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else
        appArguments.add(arg);
    }

    return appArguments.toArray(new String[0]);
  }

  private boolean isWindows() {
    return ";".equals(File.pathSeparator);
  }

  private void loadProperties(String originalUrl) throws IOException {
    Properties loadingProperties = new DuplicateProperties();

    String url = decodeUrl(originalUrl);

    if (url.startsWith("classpath:")) {
      singleLoadClasspath(url, loadingProperties);
    } else if (url.contains(":") && (!isWindows() || url.indexOf(':') > 1)) { // deal with c: d: garbage
      singleLoadUrl(url, loadingProperties);
    } else {
      File properties = new File(url);

      if (properties.exists()) {
        if (isYaml(properties) || isProperties(properties)) {
          watchedFiles.add(properties);
        }
      }
    }

    System.getProperties().putAll(loadingProperties);
  }

  /*
   * Allow for ${user.home}, ${jdk.home} and similar.
   */
  protected String decodeUrl(String url) {
    int idx = url.indexOf("${");
    while (idx > -1) {
      String part1 = url.substring(0, idx);
      String part2 = url.substring(idx+2);
      int idx2 = part2.indexOf("}");
      if (idx2 == -1) {
        url = part1 + "\\$\\{" + part2;
      } else {
        String part3 = part2.substring(idx2+1);
        url = part1 + System.getProperty(part2.substring(0, idx2), "") + part3;
      }
      idx = url.indexOf("${");
    }

    return url.replace("\\$\\{", "${");
  }


  private void singleLoadUrl(String url, Properties loadingProperties) throws IOException {
    URL source = new URL(url);
    InputStream is = source.openStream();
    loadingProperties.load(is);
    is.close();
  }

  private void singleLoadClasspath(String url, Properties loadingProperties) throws IOException {
    InputStream is = getClass().getResourceAsStream(url.substring(10));

    if (is == null) {
      System.err.println("Failed to load ${url}  (${url.substring(10)})");
    } else {
      loadingProperties.load(is);
      is.close();
    }
  }

  @Override
  protected MapPropertySource loadPropertyFile(File propertyFile) {
    if (isYaml(propertyFile)) {
      return loadYamlFile(propertyFile);
    } else {
      return loadPropertyFileContents(propertyFile);
    }
  }

  @SuppressWarnings("unchecked")
  protected MapPropertySource loadYamlFile(File yamlFile) {
    Yaml parser = new Yaml(new StrictMapAppenderConstructor());

    try {
      Map<String, Object> yamlProperties = (Map<String, Object>) parser.load(new FileReader(yamlFile));
      Map<String, Object> result = new LinkedHashMap<String, Object>();

      buildFlattenedMap(result, yamlProperties, null);

      return new MapPropertySource(yamlFile.getAbsolutePath(), result);
    } catch (FileNotFoundException e) {
      System.out.println("Unable to find file " + yamlFile.getAbsolutePath());
      return null;
    }
  }

  /*
   * From Spring Boot's YamlPropertySourceLoader with List modifications
   */
  private void buildFlattenedMap(Map<String, Object> result, Map<String, Object> source, String path) {
    for (Map.Entry<String, Object> entry : source.entrySet()) {
      String key = entry.getKey();
      if (StringUtils.hasText(path)) {
        if (key.startsWith("[")) {
          key = path + key;
        }
        else {
          key = path + "." + key;
        }
      }
      Object value = entry.getValue();
      if (value instanceof String) {
        result.put(key, value);
      }
      else if (value instanceof Map) {
        // Need a compound key
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) value;
        buildFlattenedMap(result, map, key);
      }
      else if (value instanceof Collection) {
        // Need a compound key
        @SuppressWarnings("unchecked")
        Collection<Object> collection = (Collection<Object>) value;
        boolean allSimple = collection.stream().allMatch(o -> o != null && o.getClass().getName().startsWith("java.lang."));
        if (allSimple) {
          result.put(key, collection.stream().filter(Objects::nonNull).map(Object::toString).collect(Collectors.joining(",")));
        } else {
          int count = 0;
          for (Object object : collection) {
            buildFlattenedMap(result,
              Collections.singletonMap("[" + (count++) + "]", object), key);
          }
        }
      }
      else {
        result.put(key, value != null ? value : "");
      }
    }
  }



  private MapPropertySource loadPropertyFileContents(File propertyFile) {
    Properties properties = new DuplicateProperties();

    try {
      properties.load(new FileReader(propertyFile));
    } catch (IOException e) {
      log.error("Unable to load file {}", propertyFile.getAbsolutePath());
      return null;
    }

    Map<String, Object> map = new HashMap<>();
    properties.forEach((key, value) -> {
      map.put(key.toString(), value);
    });

    return new MapPropertySource(propertyFile.getName(), map);
  }

  // from Spring's YamlProcessor
  protected static class StrictMapAppenderConstructor extends Constructor {

    // Declared as public for use in subclasses
    public StrictMapAppenderConstructor() {
      super();
    }

    @Override
    protected Map<Object, Object> constructMapping(MappingNode node) {
      try {
        return super.constructMapping(node);
      }
      catch (IllegalStateException ex) {
        throw new ParserException("while parsing MappingNode",
          node.getStartMark(), ex.getMessage(), node.getEndMark());
      }
    }

    @Override
    protected Map<Object, Object> createDefaultMap() {
      final Map<Object, Object> delegate = super.createDefaultMap();
      return new AbstractMap<Object, Object>() {
        @Override
        public Object put(Object key, Object value) {
          if (delegate.containsKey(key)) {
            throw new IllegalStateException("Duplicate key: " + key);
          }
          return delegate.put(key, value);
        }
        @Override
        public Set<Entry<Object, Object>> entrySet() {
          return delegate.entrySet();
        }
      };
    }
  }

  /**
   * This allows us to fail fast for duplicate system properties
   */
  protected static final class DuplicateProperties extends Properties {

    @Override
    public synchronized Object put(Object key, Object value) {
      Object previous = super.put(key, value);

      if (previous != null) {
        throw new IllegalStateException(String.format("Key '%s' has duplicate values as %s and %s", key, previous.toString(), value.toString()));
      }

      return null;
    }
  }
}
