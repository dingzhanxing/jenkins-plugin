package com.rocket.devops.rdoi.utils;

import com.rocket.devops.rdoi.common.exception.RDORuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class LogHandler {

    private static final Logger logger = LoggerFactory.getLogger(LogHandler.class);

    private String jobName;

    private String buildNumber;

    private String logRootPath;

    private String logFileName;

    private FileWriter fw;

    public LogHandler(String logRootPath, String jobName, String buildNumber) {
        this.jobName = jobName;
        this.buildNumber = buildNumber;
        this.logRootPath = logRootPath;

    }

    public String getLogFileName() {

        String path = this.logRootPath;
        int pos = path.lastIndexOf("/");
        if (pos != -1 && pos != (path.length() - 1)) {
            path += "/";
        }
        logFileName = logRootPath + jobName + "_" +  buildNumber;
        return logFileName;
    }

}

