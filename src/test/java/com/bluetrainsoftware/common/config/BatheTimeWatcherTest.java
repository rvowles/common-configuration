package com.bluetrainsoftware.common.config;

import org.junit.Test;
import org.springframework.core.env.MapPropertySource;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
public class BatheTimeWatcherTest {
  @Test
  public void yamlFlattensAsExpected() throws FileNotFoundException {
    BatheTimeWatcher btWatcher = new BatheTimeWatcher();
    MapPropertySource mapPropertySource = btWatcher.loadYamlFile(new File("src/test/resources/sample2.yaml"));

    assertThat(mapPropertySource.getPropertyNames()).isNotEmpty();
    assertThat(mapPropertySource.getName()).isNotNull();

    assertThat(mapPropertySource.getProperty("my.nana").toString()).isEqualTo("grace");
    assertThat(mapPropertySource.getProperty("my.died").toString()).isEqualTo("92");
    assertThat(mapPropertySource.getProperty("my.packages").toString()).isEqualTo("one,two,three");
  }

  @Test
  public void decoderCheck() {
    String userHome = System.getProperty("user.home");
    String url = "my.${user.home}.home";
    assertThat(new BatheTimeWatcher().decodeUrl(url)).isEqualTo("my." + userHome + ".home");
    url = "simple.sample";
    assertThat(new BatheTimeWatcher().decodeUrl(url)).isEqualTo(url);
    url = "simple${sample";
    assertThat(new BatheTimeWatcher().decodeUrl(url)).isEqualTo(url);
    url = "${user.home}";
    assertThat(new BatheTimeWatcher().decodeUrl(url)).isEqualTo(userHome);
  }
}
