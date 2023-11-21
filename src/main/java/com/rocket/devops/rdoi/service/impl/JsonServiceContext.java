package com.rocket.devops.rdoi.service.impl;

import com.rocket.devops.rdoi.service.IJsonService;
import com.rocket.devops.rdoi.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class JsonServiceContext {

    private static final Logger logger = LoggerFactory.getLogger(JsonServiceContext.class);

    public static final String BASE = "JSON_";
    public static final String JSON_GITLAB = BASE + Constants.GIT_LAB;

    public static final String JSON_SCM_TRIGGER_ALL_PARAMS = BASE + Constants.SCM_TRIGGER_ALL_PARAMS;
    public static final String JSON_BITBUCKET = BASE + Constants.BITBUCKET;
    public static final String JSON_GITHUB = BASE + Constants.GIT_HUB;
    public static final String JSON_MANUAL = BASE + Constants.MANUAL;

    public static Map<String, IJsonService> jsonServiceMap = new HashMap<>();

    public static void addJsonService(String key, IJsonService service){
        jsonServiceMap.put(key, service);
    }
    public static IJsonService getJsonService(String type) {
        return jsonServiceMap.get("JSON_" + type);
    }
}
