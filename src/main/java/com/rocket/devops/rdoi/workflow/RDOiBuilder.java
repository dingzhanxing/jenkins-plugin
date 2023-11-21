package com.rocket.devops.rdoi.workflow;

import com.rocket.devops.rdoi.Messages;
import com.rocket.devops.rdoi.common.RdoResult;
import com.rocket.devops.rdoi.common.dto.Instance;
import com.rocket.devops.rdoi.common.exception.RDORuntimeException;
import com.rocket.devops.rdoi.service.impl.JsonSCMService;
import com.rocket.devops.rdoi.service.impl.JsonServiceContext;
import com.rocket.devops.rdoi.utils.Constants;
import com.rocket.devops.rdoi.utils.PluginUtils;
import com.rocket.devops.rdoi.utils.RDORequests;
import hudson.Launcher;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.JobPropertyDescriptor;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;

import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Optional;

import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * @author zding
 * This is the build step entry point
 */

public class RDOiBuilder extends Builder implements SimpleBuildStep {
    private final String name;
    private final String url;
    private final String pipeline;

    private static  Run<?, ?> run;
    private String loadDeployPipelineError = "test";

    private boolean deploy;
    private BuildStage buildStage;
    @DataBoundConstructor
    public RDOiBuilder(String name, String url, String pipeline) {
        this.name = name;
        this.url = url;
        this.pipeline = pipeline;
    }

    public String getName() {
        return name;
    }

    public String getPipeline() {
        return pipeline;
    }

    public String getUrl() {
        return url;
    }

    public boolean isDeploy() {
        return deploy;
    }

    public String getLoadDeployPipelineError() {
        return loadDeployPipelineError;
    }

    @DataBoundSetter
    public void setDeploy(boolean deploy) {
        this.deploy = deploy;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {


        //printWebHookParameters(listener.getLogger(), env);
        //listener.getLogger().println("Pipeline name=" + getPipeline());
        //buildStage = new BuildStage(env, listener, launcher);

        this.run = run;
        DeployPipelineTrigger bpt = new DeployPipelineTrigger(env, listener, launcher, getPipeline(), run);
        if (bpt.run() != 0){
            RDORuntimeException exception = new RDORuntimeException(Messages.RDOi_pipeline_run_failed());
            throw exception;
        }
        //buildStage.run();
        //run.addAction(new BuildAction(name, url));
    }


    public void printWebHookParameters(PrintStream logger, EnvVars envVars) {
        logger.println("-".repeat(50) + "env parameters" + "-".repeat(50));
        envVars.entrySet().forEach(e -> {
            logger.println(e.getKey() + " : " + e.getValue());
        });
    }
    @Symbol("RDOi")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckName(@QueryParameter String value, @QueryParameter boolean useFrench)
                throws IOException, ServletException {
//            if (value.length() == 0)
//                return FormValidation.error(Messages.RDOiBuilder_DescriptorImpl_errors_missingName());
//            if (value.length() < 4)
//                return FormValidation.warning(Messages.RDOiBuilder_DescriptorImpl_warnings_tooShort());
            return FormValidation.ok();
        }

        public  ListBoxModel doFillPipelineItems(){

            RdoResult result = null;

            try {
                result =  RDORequests.getRequest(Constants.RDOP_PIPELINE_LIST);
            } catch (Exception e) {
                throw new RDORuntimeException(e);
            }

            //if (result.getStatus() != Integer.parseInt(MessageCodeConstants.SUCCESS)) {
//            if (true) {
//                //loadDeployPipelineError = "Some errors happened.";
//                return null;
//            }
            ListBoxModel pipelineList = new ListBoxModel();
            ArrayList<LinkedHashMap<String,Object>> pipelineListRDO = (ArrayList<LinkedHashMap<String,Object>>)result.getPayload();
            if (pipelineListRDO != null){
                pipelineListRDO.forEach(
                    e->{
                        if (e.get("pplType") != null && e.get("pplType").equals("NON_BUILD")
                         && e.get("triggerModel") != null  && e.get("triggerModel").equals("JENKINS")) {
                            String pplDesc = (String) e.get("pplDesc");
                            String leftSeparator = "(";
                            String rightSeparator = ")";
                            if (pplDesc != null && pplDesc.equals("")){
                                leftSeparator = "";
                                rightSeparator = "";
                            }
                            String pplName = (String) e.get("pplName") + leftSeparator + pplDesc + rightSeparator;
                            pipelineList.add(pplName, (String) e.get("pplId"));
                        }
                    }
                );
            }
            return pipelineList;
        }

//        public FormValidation doCheckPipeline(@QueryParameter String value)
//                throws IOException, ServletException {
////            if (value.length() == 0 && loadDeployPipelineError != null && loadDeployPipelineError != "")
////                return FormValidation.error(loadDeployPipelineError);
//            return FormValidation.ok();
//        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @PostConstruct
        public void init(){
            JsonSCMService jsonSCMService = new JsonSCMService();
            JsonServiceContext.addJsonService(JsonServiceContext.JSON_SCM_TRIGGER_ALL_PARAMS, jsonSCMService);
        }
        @Override
        public String getDisplayName() {
            return Messages.RDOiBuilder_DescriptorImpl_DisplayName();
        }
    }

}
