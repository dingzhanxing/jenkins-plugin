package com.rocket.devops.rdoi.utils;

import com.rocket.devops.rdoi.common.exception.RDORuntimeException;
import hudson.model.TaskListener;
import jenkins.security.MasterToSlaveCallable;
import org.kohsuke.stapler.export.Exported;

import java.io.*;

public abstract class AgentAccess extends MasterToSlaveCallable<Integer, IOException> implements Serializable {

    @Exported
    protected TaskListener listener;

    public AgentAccess(TaskListener listener) {
        this.listener = listener;
    }
}
