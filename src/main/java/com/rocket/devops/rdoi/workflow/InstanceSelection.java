package com.rocket.devops.rdoi.workflow;

import com.rocket.devops.rdoi.Messages;
import com.rocket.devops.rdoi.connection.RDOiConn;
import com.rocket.devops.rdoi.connection.RDOiConnCfg;
import hudson.Extension;
import hudson.model.*;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.util.List;
import java.util.Optional;

@Extension
public class InstanceSelection extends JobProperty<AbstractProject<FreeStyleProject, FreeStyleBuild>> {

	private String instanceName;

	public InstanceSelection() {}

	@DataBoundConstructor
	public InstanceSelection(String instanceName) {
		this.instanceName = instanceName;
	}

	public String getInstanceName() {
		return instanceName;
	}

	@DataBoundSetter
	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	@Extension
	public static class DescriptorImpl extends JobPropertyDescriptor {

		public DescriptorImpl() {
			super(InstanceSelection.class);
		}

		@Override
		public String getDisplayName() {
			return Messages.RDOi_connection_selection_title();
		}

		public FormValidation doCheckInstanceName(@QueryParameter String instanceName) {
//			if (StringUtils.isBlank(instanceName))
//				return FormValidation.error(Messages.RDOi_connection_selection_empty());
			return FormValidation.ok();
		}

		public ListBoxModel doFillInstanceNameItems() {
			ListBoxModel instanceNameList = new ListBoxModel();
			List<RDOiConn> connectionList = RDOiConnCfg.get().getConn();
			connectionList.forEach(c -> instanceNameList.add(c.getName(), c.getName()));
			return instanceNameList;
		}
	}

	public static Optional<InstanceSelection> getFromJob(Run<?, ?> run) {
		Job<?, ?> job = run.getParent();
		return Optional.ofNullable(job.getProperty(InstanceSelection.class));
	}

}
