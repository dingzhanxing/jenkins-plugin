package com.rocket.devops.rdoi.service.impl;

import com.rocket.devops.rdoi.common.AppConfiguration;
import com.rocket.devops.rdoi.service.IJsonService;
import com.rocket.devops.rdoi.utils.Constants;
import com.rocket.devops.rdoi.utils.EnvVarsExtractor;

import java.util.HashMap;
import java.util.Map;

public class JsonGitLabService implements IJsonService {
    @Override
    public Map<String, String> getJsonParams(EnvVarsExtractor envVarsExtractor) {
        Map<String, String> retMap = new HashMap<>();
        retMap.put(Constants.GITLAB_BRANCH, envVarsExtractor.getSingleEnv(Constants.GITLAB_BRANCH));
        retMap.put(Constants.GIT_COMMIT, envVarsExtractor.getSingleEnv(Constants.GIT_COMMIT));
        retMap.put(Constants.GITLAB_ACTION_TYPE, envVarsExtractor.getSingleEnv(Constants.GITLAB_ACTION_TYPE));
        retMap.put(Constants.GITLAB_AFTER, envVarsExtractor.getSingleEnv(Constants.GITLAB_AFTER));
        retMap.put(Constants.GITLAB_BEFORE, envVarsExtractor.getSingleEnv(Constants.GITLAB_BEFORE));
        retMap.put(Constants.GITLAB_SOURCE_REPO_HTTP_URL, envVarsExtractor.getSingleEnv(Constants.GITLAB_SOURCE_REPO_HTTP_URL));
        retMap.put(Constants.GITLAB_SOURCE_REPO_NAME, envVarsExtractor.getSingleEnv(Constants.GITLAB_SOURCE_REPO_NAME));
        retMap.put(Constants.GITLAB_USER_USERNAME, envVarsExtractor.getSingleEnv(Constants.GITLAB_USER_USERNAME));
        retMap.put(Constants.WORKSPACE, envVarsExtractor.getSingleEnv(Constants.WORKSPACE));
        retMap.put(Constants.TRIGGER_SOURCE_KEY, Constants.TRIGGER_VALUE);
        retMap.put(Constants.JENKINS_LOGPATH_KEY, AppConfiguration.getProperty("logger.path"));
        retMap.put(Constants.JENKINS_JOB_NAME, envVarsExtractor.getSingleEnv("JOB_NAME"));
        retMap.put(Constants.JENKINS_BUILD_NUMBER, envVarsExtractor.getSingleEnv("BUILD_NUMBER"));
        retMap.put(Constants.JOB_URL, envVarsExtractor.getSingleEnv("JOB_URL"));
        return retMap;
    }

    @Override
    public boolean isValidHookType(String json) {
        return true;
    }
}
