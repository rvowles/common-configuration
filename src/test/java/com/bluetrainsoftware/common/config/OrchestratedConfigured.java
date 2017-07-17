package com.bluetrainsoftware.common.config;

import javax.inject.Inject;

/**
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
public class OrchestratedConfigured {
	@Inject
	SampleConfiguredClass scc;

//	@ConfigKey("orch.key")
//	String config;
//
//	@PostConfigured
//	public void postconfigured() {
//		config += "post";
//	}

	@PreStart
	public void blah() {
		scc.configs.add("cc");
	}
}
