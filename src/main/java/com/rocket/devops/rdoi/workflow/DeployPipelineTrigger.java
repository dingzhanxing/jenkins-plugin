package com.rocket.devops.rdoi.workflow;

import com.rocket.devops.rdoi.Messages;
import com.rocket.devops.rdoi.common.RdoResult;
import com.rocket.devops.rdoi.common.def.MessageCodeConstants;
import com.rocket.devops.rdoi.common.exception.RDORuntimeException;
import com.rocket.devops.rdoi.service.IJsonService;
import com.rocket.devops.rdoi.service.impl.JsonServiceContext;
import com.rocket.devops.rdoi.utils.*;
import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Thread.sleep;

public class DeployPipelineTrigger implements PipelineTriggerBase{

    private EnvVarsExtractor envVarsExtractor;
    private PrintStream logger;

    private TaskListener listener;
    private Launcher launcher;
    private String pipelineID;
    private String pipelineType;

    private Run<?, ?> run;

    public DeployPipelineTrigger(EnvVars env, TaskListener listener, Launcher launcher, String pipelineID, Run<?, ?> run) {
        this.envVarsExtractor = new EnvVarsExtractor(env);
        this.listener = listener;
        this.logger = listener.getLogger();
        this.launcher = launcher;
        this.pipelineID = pipelineID;
        this.pipelineType = "Deploy";
        this.run = run;
    }

    @Override
    public Integer run() {
        AtomicReference<Integer> runResult = new AtomicReference<>(0);

//        String triggerType = envVarsExtractor.getTriggerType();
//        if (triggerType != Constants.SCM_TRIGGER_ALL_PARAMS){
//            throw new RDORuntimeException(Messages.RDOi_Build_unsupported_trigger_type(triggerType));
//        }
//
//        IJsonService jsonService = JsonServiceContext.getJsonService(triggerType);
        //Map<String, String> reqJson = jsonService.getJsonParams(envVarsExtractor);
        Map<String, String> reqJson = new HashMap<>();

        RdoResult triggerResult = null;

        String runUrl = Constants.RDOP_PIPELINE_BASE_URI + "/" + this.pipelineID + "/run";

        try {
            triggerResult =  RDORequests.actionRequest(runUrl, JacksonUtils.serialize(reqJson));
        } catch (Exception e) {
            throw new RDORuntimeException(e);
        }

        String separatorStart = SeparatorHandler.getSeparatorStart();
        String RunContentStart =  "RDOi " +  this.pipelineType + " Starting";
        logger.println(separatorStart + RunContentStart + SeparatorHandler.getSeparatorEnd(RunContentStart));

        if (triggerResult.getStatus() != Integer.parseInt(MessageCodeConstants.SUCCESS))
            throw new RDORuntimeException(triggerResult.getMessage());

        LinkedHashMap<String,Object> historySeqList = (LinkedHashMap<String,Object>) triggerResult.getPayload();
        if (historySeqList.size() <= 0)
            throw new RDORuntimeException(triggerResult.getMessage());

        String historySeqPipepline = Integer.toString((int)historySeqList.get("historySeq"));
        RdoResult getHistoryResult = null;

        String historyUrl = Constants.RDOP_PIPELINE_BASE_URI + "/" +this.pipelineID + "/history/list?historySeq=" + historySeqPipepline;
        try {
            getHistoryResult =  RDORequests.getRequest(historyUrl);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (getHistoryResult.getStatus() != Integer.parseInt(MessageCodeConstants.SUCCESS))
            throw new RDORuntimeException(getHistoryResult.getMessage());

        ArrayList<LinkedHashMap<String,Object>> pipelineHistoryList = (ArrayList<LinkedHashMap<String,Object>>)getHistoryResult.getPayload();

        if (pipelineHistoryList.size() <= 0)
            throw new RDORuntimeException(getHistoryResult.getMessage());

            LinkedHashMap<String,Object> history = pipelineHistoryList.get(0);
            ArrayList stages = (ArrayList) history.get("stages");
            OUT:
            for (int iStage = 0; iStage < stages.size(); iStage++) {
                //ArrayList actions = (LinkedHashMap<String,Object>)e.get("actions");
                LinkedHashMap<String,Object> stage = (LinkedHashMap<String,Object>)stages.get(iStage);
                String stageContent = Messages.RDOi_pipeline_start_running("stage " + ": " + stage.get("stageName"));
                logger.println(separatorStart + stageContent + SeparatorHandler.getSeparatorEnd(stageContent));
                ArrayList actions = (ArrayList)stage.get("actions");
                for (int iAction = 0; iAction < actions.size(); iAction++) {
                    RdoResult getActionRunHistoryResult = null;
                    LinkedHashMap<String,Object> action = (LinkedHashMap<String,Object>)actions.get(iAction);

                    String actionContent = Messages.RDOi_pipeline_start_running("action " + ": " + action.get("actionName"));
                    logger.println(separatorStart + actionContent + SeparatorHandler.getSeparatorEnd(actionContent));

                    String actionStatus = (String)action.get("actionStatus");
                    String pplId = (String)action.get("pplId");
                    String stageTplId = (String)action.get("stageTplId");
                    String actionTplId = (String)action.get("actionTplId");
                    String actionSeq =  Integer.toString ((int) action.get("actionSeq"));
                    String historySeq = Integer.toString((int)action.get("historySeq"));
                    String actionLogId = (String)action.get("logId");
                    while(!(actionStatus.equals("SUCCESS")
                            || actionStatus.equals("FAILED")
                            || actionStatus.equals("TERMINATED")
                            || actionStatus.equals("TIMEOUT")
                    ) || null == actionLogId){
                        try {
                            sleep(10000);
                            String actionRunHistoryUrl = Constants.RDOP_PIPELINE_BASE_URI + "/" +"actionRunHistory"
                                    + "/" + pplId + "/" + stageTplId
                                    + "/" + actionTplId + "/" + actionSeq
                                    + "/" + historySeq;
                            try {
                                getActionRunHistoryResult =  RDORequests.getRequest(actionRunHistoryUrl);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        if (getActionRunHistoryResult.getStatus() != Integer.parseInt(MessageCodeConstants.SUCCESS))
                            throw new RDORuntimeException(getActionRunHistoryResult.getMessage());

                        LinkedHashMap<String,Object> actionRunHistory = (LinkedHashMap<String,Object>)getActionRunHistoryResult.getPayload();
                        actionStatus = (String)actionRunHistory.get("actionStatus");
                        actionLogId = (String)actionRunHistory.get("logId");
                    }

                    RdoResult actionLogResult = null;

                    String actionLogUrl = Constants.RDOP_PIPELINE_BASE_URI + "/" + "log/" + actionLogId;
                    try {
                        actionLogResult =  RDORequests.getRequest(actionLogUrl);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    if (actionLogResult.getStatus() != Integer.parseInt(MessageCodeConstants.SUCCESS)) {
                        throw new RDORuntimeException(actionLogResult.getMessage());
                    }
                    logger.println(actionLogResult.getPayload());

                    if (actionStatus.equals("FAILED") || actionStatus.equals("TIMEOUT") || actionStatus.equals("TERMINATED")) {
                        runResult.set(1);
                        break OUT;
                    }
                }
               //action loop end here
            }
           // stage loop end here
        String RunContentEnd =  "RDOi " +  this.pipelineType + " End";
        logger.println(separatorStart + RunContentEnd + SeparatorHandler.getSeparatorEnd(RunContentEnd));

        return runResult.get();
    }
}
