package com.rocket.devops.rdoi.utils;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.rocket.devops.rdoi.common.dto.Instance;
import com.rocket.devops.rdoi.connection.RDOiConn;
import com.rocket.devops.rdoi.workflow.InstanceSelection;
import hudson.model.Job;
import hudson.model.Run;
import hudson.security.ACL;
import jenkins.model.Jenkins;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PluginUtils {

	/**
	 * Get configured instance from Job definition
	 * @param job
	 * @return
	 */
	public static Optional<Instance> getInstance(Job<?, ?> job) {
		Optional<InstanceSelection> instanceSelection = Optional.ofNullable(job.getProperty(InstanceSelection.class));
		return getInstance(instanceSelection);
	}

	/**
	 * Get configured instance from running job
	 * @param run
	 * @return
	 */
	public static Optional<Instance> getInstance(Run<?, ?> run) {
		return getInstance(InstanceSelection.getFromJob(run));
	}

	/**
	 * Get configured instance from InstanceSelection
	 * @param instanceSelection
	 * @return
	 */
	public static Optional<Instance> getInstance(Optional<InstanceSelection> instanceSelection) {
		Optional<RDOiConn> rdOiConnectionOpt = instanceSelection
				.map(i -> RDOiConn.getByName(i.getInstanceName()).orElse(null));
		Optional<StandardUsernamePasswordCredentials> credOpt = rdOiConnectionOpt.map(r -> {
			List<StandardUsernamePasswordCredentials> creds = CredentialsProvider.lookupCredentials(
					StandardUsernamePasswordCredentials.class,
					Jenkins.getInstanceOrNull(),
					ACL.SYSTEM,
					new ArrayList<DomainRequirement>());
			return creds.stream()
					.filter(c -> c instanceof StandardUsernamePasswordCredentials && c.getId().equals(r.getCredentialId()))
					.findFirst()
					.orElse(null);
		});


		return credOpt.map(c -> {
			Instance instance = new Instance();
			instance.setInstanceName(rdOiConnectionOpt.map(r -> r.getName()).orElse(null));
			instance.setUrl(rdOiConnectionOpt.map(r -> r.getUrl()).orElse(null));
			instance.setUsername(c.getUsername());
			instance.setPassword(c.getPassword().getPlainText());
			return instance;
		});
	}

}
