package com.rocket.devops.rdoi.service.impl;

import com.rocket.devops.rdoi.service.IJsonService;
import com.rocket.devops.rdoi.utils.Constants;
import com.rocket.devops.rdoi.utils.EnvVarsExtractor;
import com.rocket.devops.rdoi.utils.JacksonUtils;
import hudson.EnvVars;

import java.util.Map;

public class JsonSCMService implements IJsonService {
    @Override
    public Map<String, String> getJsonParams(EnvVarsExtractor envVarsExtractor) {
        String strParams = envVarsExtractor.getSingleEnv(Constants.SCM_TRIGGER_ALL_PARAMS);
        Map<String, String> map = JacksonUtils.deserialize(strParams, Map.class);
        return map;
    }

    @Override
    public boolean isValidHookType(String json) {
        return false;
    }
}
