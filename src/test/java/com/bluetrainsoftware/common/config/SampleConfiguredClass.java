package com.bluetrainsoftware.common.config;


import net.stickycode.stereotype.configured.PostConfigured;
import net.stickycode.stereotype.configured.PreConfigured;

import java.util.ArrayList;
import java.util.List;

public class SampleConfiguredClass {
  @ConfigKey("my.armpit")
  String armpit;

  @ConfigKey
  Integer defaultValue = 5;

  @ConfigKey("no.default.value")
  Integer noDefaultValue;

  @ConfigKey
  Integer reallyNoDefaultValue;

  @ConfigKey("Manifest-Version")
  String monkey;
  
  List<String> configs = new ArrayList<>();

  @PreConfigured
	public void preconfig() {
  	configs.add("preconfigured");
  }

	@PostConfigured
	public void postconfig() {
		configs.add("postconfigured");
	}
}
