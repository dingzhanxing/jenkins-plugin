package com.rocket.devops.rdoi.workflow;

import com.rocket.devops.rdoi.Messages;
import com.rocket.devops.rdoi.common.AppConfiguration;
import com.rocket.devops.rdoi.common.RdoResult;
import com.rocket.devops.rdoi.common.exception.RDORuntimeException;
import com.rocket.devops.rdoi.service.IJsonService;
import com.rocket.devops.rdoi.service.impl.JsonServiceContext;
import com.rocket.devops.rdoi.utils.*;
import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;


public class BuildStage implements BaseStage{
    private EnvVarsExtractor envVarsExtractor;
    private PrintStream logger;

    private TaskListener listener;
    private Launcher launcher;

    private Run<?, ?> run;
    public BuildStage(EnvVars env, TaskListener listener, Launcher launcher, Run<?,?> run) {
        this.envVarsExtractor = new EnvVarsExtractor(env);
        this.listener = listener;
        this.logger = listener.getLogger();
        this.launcher = launcher;
        this.run = run;
    }

    @Override
    public Integer run() {

        Integer runResult = 0;

        String triggerType = envVarsExtractor.getTriggerType();
        if (triggerType != Constants.GIT_LAB){
            throw new RDORuntimeException(Messages.RDOi_Build_unsupported_trigger_type(triggerType));
        }

        IJsonService jsonService = JsonServiceContext.getJsonService(triggerType);
        Map<String, String> reqJson = jsonService.getJsonParams(envVarsExtractor);

        RdoResult result = null;

        try {
            result =  RDORequests.actionRequest(Constants.SCM_GITLAB_JENKINS_BUILD_URI, JacksonUtils.serialize(reqJson));
        } catch (Exception e) {
            throw new RDORuntimeException(e);
        }

        logger.println("=".repeat(56) + "RDOi Build starting" + "=".repeat(56));

        String rootPath = AppConfiguration.getProperty("logger.path");

        LogHandler logHandler = new LogHandler(rootPath, envVarsExtractor.getSingleEnv("JOB_NAME"), envVarsExtractor.getSingleEnv("BUILD_NUMBER"));

        AgentAccess aa = new LogReader(listener,logHandler.getLogFileName());
        if (launcher.getChannel() != null) {

            try {
                int slaveReturn = launcher.getChannel().call(aa);
                if (slaveReturn != 0) {
                    throw new RDORuntimeException(Messages.RDOi_Build_errors());
                }
            } catch (RDORuntimeException e) {
                throw e;
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return runResult;
    }

}
