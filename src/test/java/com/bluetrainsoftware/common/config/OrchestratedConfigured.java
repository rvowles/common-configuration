package com.bluetrainsoftware.common.config;

import net.stickycode.stereotype.configured.CompleteConfigured;

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

	@CompleteConfigured
	public void blah() {
		scc.configs.add("cc");
	}
}
