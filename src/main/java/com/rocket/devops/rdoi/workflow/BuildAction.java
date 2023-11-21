package com.rocket.devops.rdoi.workflow;

import hudson.model.Run;
import jenkins.model.RunAction2;


/**
 * @author zding
 * This is the action point,
 * could add more action based on the business requirement
 */
public class BuildAction implements RunAction2 {

    public String name;

    public String url;

    private transient Run run;

    @Override
    public void onAttached(Run<?, ?> r) {
        this.run = r;
    }

    @Override
    public void onLoad(Run<?, ?> r) {
        this.run = r;
    }

    public Run getRun() {
        return run;
    }
    public BuildAction(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getIconFileName() {
        return "document.png";
    }

    @Override
    public String getDisplayName() {
        return "RDOi";
    }

    @Override
    public String getUrlName() {
        return "RDOi";
    }

}
