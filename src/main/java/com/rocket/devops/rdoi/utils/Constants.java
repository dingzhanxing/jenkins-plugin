package com.rocket.devops.rdoi.utils;

/**
 * This class is used to define the constants used in this project.
 * It includes the payload from gitlab webhook and other constants.
 */
public final class Constants {

    //Gitlab webhook payload constants
    public static final String GITLAB_AFTER = "gitlabAfter";
    public static final String GITLAB_BEFORE = "gitlabBefore";
    public static final String GITLAB_SOURCE_REPO_HTTP_URL = "gitlabSourceRepoHttpUrl";
    public static final String GITLAB_ACTION_TYPE = "gitlabActionType";

    public static final String SCM_TRIGGER_ALL_PARAMS = "scmTriggerAllParams";
    public static final String GITLAB_USER_USERNAME = "gitlabUserUsername";
    public static final String GITLAB_SOURCE_REPO_NAME = "gitlabSourceRepoName";
    public static final String GIT_COMMIT = "GIT_COMMIT";
    public static final String GITLAB_BRANCH = "gitlabBranch";
    public static final String WORKSPACE = "WORKSPACE";
    public static final String JOB_URL = "jobUrl";

    public static final String TRIGGER_VALUE = "jenkins";
    public static final String TRIGGER_SOURCE_KEY = "triggerSource";
    public static final String JENKINS_LOGPATH_KEY = "logPath";

    public static final String JENKINS_JOB_NAME = "jobName";

    public static final String JENKINS_BUILD_NUMBER = "buildNumber";

    //Rest request URL
    public static final String LOGIN_PATH = "login";
    public static final String SCM_GITLAB_JENKINS_BUILD_URI= "api/rdop/scm/jenkins/build?scmType=GITLAB";
    public static final String SCM_JENKINS_TRIGGER_URI= "api/rdop/scm/webhook/invoke?scmType=GITLAB&scmToken=46fa433c8167548dbe94c8c9eeb7b7c60707266e1e4ee632950228a6c479cc9e&triggerType=ON_PUSH";
    public static final String RDOP_PIPELINE_LIST= "api/rdop/pipeline/list";

    public static final String RDOP_RENEW_TOKEN= "api/rdop/auth/token/new";
    public static final String RDOP_PIPELINE_BASE_URI= "api/rdop/pipeline";

    //Other constants added here
    public static final String RDOP_TOKEN = "rdopToken";
    public static final String LOGIN_TOKEN = "token";

    public static final String JENKINS_API_KEY_HEADER = "X-JENKINS-API-KEY";
    public static final String JENKINS_API_VALUE_HEADER = "X-JENKINS-API-VALUE";
    public static final String RDOP_REFRESH_TOKEN = "rdop_refresh_token";
    public static final String RDOP_INVALID_TOKEN = "RDOP01010";

    //SCM type
    public static final String GIT_LAB = "GITLAB";
    public static final String BITBUCKET = "BITBUCKET";
    public static final String GIT_HUB = "GITHUB";
    public static final String MANUAL = "MANUAL";

    //Flags for judging a jenkins job build success or fail
    public static final String JENKINS_BUILD_FAILED = "JENKINS_BUILD_FAILED";
    public static final String JENKINS_BUILD_SUCCESS = "JENKINS_BUILD_SUCCESS";

    public static final String JENKINS_BUILD_COMPLETED = "JENKINS_BUILD_COMPLETED";

    // Separator related
    public static final String LOG_SEPARATOR_SYMBOL = "=";

    public static final String LOG_SEPARATOR_SYMBOL_REPEAT_TIMES_START = "50";
    public static final String LOG_SEPARATOR_SYMBOL_REPEAT_TIMES_END = "83";

}
