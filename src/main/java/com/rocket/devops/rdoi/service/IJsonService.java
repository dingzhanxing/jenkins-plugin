package com.rocket.devops.rdoi.service;

import com.rocket.devops.rdoi.utils.EnvVarsExtractor;

import java.util.Map;

public interface IJsonService {

    Map<String, String> getJsonParams(EnvVarsExtractor envVarsExtractor);

    boolean isValidHookType(String json);

}
