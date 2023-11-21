package com.rocket.devops.rdoi.connection;

import hudson.Extension;
import hudson.ExtensionList;
import jenkins.model.GlobalConfiguration;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zding
 * This class is used to set the global configuration for RDOi service
 * Set the connection name
 * RDOi service URL
 * And set credential which is used to access RDOi service
 */

@Extension
public class RDOiConnCfg extends GlobalConfiguration {

    //private Boolean useAuthenticatedEndpoint = true;
    private List<RDOiConn> conn = new ArrayList<>();
    private transient Map<String, RDOiConn> connMap = new HashMap<>();

    @DataBoundConstructor
    public RDOiConnCfg() {
        load();
        refreshConnectionMap();
    }

    public List<RDOiConn> getConn() {
        return conn;
    }

    public RDOiConn getSelectedConnectionByName(String name) {
        return connMap.get(name);
    }

    public void addConn(RDOiConn connection) {
        conn.add(connection);
        connMap.put(connection.getName(), connection);
    }

    @DataBoundSetter
    public void setConn(List<RDOiConn> newConnections) {
        conn = new ArrayList<>();
        connMap = new HashMap<>();
        for (RDOiConn connection : newConnections) {
            addConn(connection);
        }
        save();
    }

    private void refreshConnectionMap() {
        connMap.clear();
        for (RDOiConn connection : conn) {
            connMap.put(connection.getName(), connection);
        }
    }

    public static RDOiConnCfg get() {
        return ExtensionList.lookupSingleton(RDOiConnCfg.class);
    }
}
