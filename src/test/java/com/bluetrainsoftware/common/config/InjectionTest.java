package com.bluetrainsoftware.common.config;

import net.stickycode.bootstrap.StickySystem;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.fest.assertions.api.Assertions.assertThat;

@EnableStickyConfiguration
@Import({OrchestratedConfigured.class, SampleConfiguredClass.class})
public class InjectionTest {

	@Test
	public void onlyOne() {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("/META-INF/bluetrain/sticky-components.xml");

		assertThat(ctx.getBeansOfType(StickySystem.class).size()).isEqualTo(1);

    List<String> config = new ArrayList<>();
		for(String s : ctx.getBeanDefinitionNames()) {
		  Object o = ctx.getBean(s);
		  if (o.getClass().getName().startsWith("net.stickycode.") || o.getClass().getName().startsWith("com.bluetrainsoftware.")) {
		    config.add(o.getClass().getSimpleName());
        System.out.printf("import %s;\n", o.getClass().getName());
      }
    }

    System.out.printf("@Import({%s})", config.stream().collect(Collectors.joining(".class,")));
  }

  @Test
  public void test() {
    System.setProperty("my.armpit", "armpit");
    System.setProperty("no.default.value", "17");
    System.setProperty("sampleConfiguredClass.reallyNoDefaultValue", "18");
    System.setProperty("sampleConfiguredClass.simpleValue", "simpleValue");
    System.setProperty("sampleConfiguredClass.simpleMap", "one=1, two=2, three=3");
    System.setProperty("Manifest-Version", "1.62b");

    ApplicationContext ctx = new AnnotationConfigApplicationContext(getClass());

    SampleConfiguredClass sample = ctx.getBean(SampleConfiguredClass.class);

    ctx.getBean(OrchestratedConfigured.class);

    assertThat(sample.armpit).isEqualTo("armpit");
    assertThat(sample.defaultValue).isEqualTo(5);
    assertThat(sample.monkey).isNotNull();
    assertThat(sample.noDefaultValue).isEqualTo(17);
    assertThat(sample.reallyNoDefaultValue).isEqualTo(18);
    assertThat(sample.configs).isEqualTo(Arrays.asList("preconfigured", "postconfigured", "cc"));
//    assertThat(sample.simpleValue).isEqualTo("simpleValue");
//    assertThat(sample.simpleMap.size()).isEqualTo(3);
//    assertThat(sample.simpleMap.get("one")).isEqualTo("1");
//    assertThat(sample.simpleMap.get("two")).isEqualTo("2");
//    assertThat(sample.simpleMap.get("three")).isEqualTo("3");
  }
}
