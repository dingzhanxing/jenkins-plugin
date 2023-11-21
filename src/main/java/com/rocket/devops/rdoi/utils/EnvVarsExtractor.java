package com.rocket.devops.rdoi.utils;

import hudson.EnvVars;

/**
 * This class is used to get the ENV variables.
 *
 */

public class EnvVarsExtractor {

    EnvVars env;

    public EnvVarsExtractor(EnvVars env) {
        this.env = env;
    }

    public EnvVars getEnv() {
        return env;
    }

    public void setEnv(EnvVars env) {
        this.env = env;
    }

    public String getSingleEnv(String key){
        if (env != null){
            return env.get(key);
        }
        return null;
    }

    public String getTriggerType(){
        String triggerType = null;
        if (getSingleEnv(Constants.GITLAB_ACTION_TYPE) != null){
            triggerType = Constants.GIT_LAB;
        }else if (getSingleEnv("BITBUCKET_ACTION_TYPE") != null) {
            triggerType = Constants.BITBUCKET;
        }else if(getSingleEnv("GIT_HUB_ACTION_TYPE") != null) {
            triggerType = Constants.GIT_HUB;
        } else if (getSingleEnv(Constants.SCM_TRIGGER_ALL_PARAMS) != null) {
            triggerType = Constants.SCM_TRIGGER_ALL_PARAMS;
        } else{
            triggerType = Constants.MANUAL;
        }
        return triggerType;
    }
}
