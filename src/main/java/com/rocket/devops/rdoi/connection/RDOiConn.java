package com.rocket.devops.rdoi.connection;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.rocket.devops.rdoi.Messages;
import com.rocket.devops.rdoi.common.def.MessageCodeConstants;
import com.rocket.devops.rdoi.common.RdoResult;
import com.rocket.devops.rdoi.common.exception.RDORuntimeException;
import com.rocket.devops.rdoi.utils.Constants;
import com.rocket.devops.rdoi.utils.PasswordUtil;
import com.rocket.devops.rdoi.utils.RDORequests;
import com.rocket.devops.rdoi.utils.SeparatorHandler;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.gitclient.GitURIRequirementsBuilder;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

import java.util.*;


/**
 * @author zding
 * This class is used to get the connection for RDOi service
 */

public class RDOiConn extends AbstractDescribableImpl<RDOiConn> {

    private final String name;
    private String url;
    private transient String apiToken;
    private String credentialId;
    @DataBoundConstructor
    public RDOiConn(String name, String url, String apiToken, String credentialId) {
        this.name = name;
        this.url = url;
        this.apiToken = apiToken;
        this.credentialId = credentialId;
    }


    public String getName() {
        return name;
    }

    public String getUrl() {
        return SeparatorHandler.appendURLSeparator(url);
    }

    public String getCredentialId() {
        return credentialId;
    }


    public String getRDOpToken(boolean renew) {
        if (renew == false && apiToken !=null)
            return apiToken;

        Jenkins.get().checkPermission(Jenkins.ADMINISTER);

        List<StandardUsernamePasswordCredentials> creds = getCreds();

        Optional<StandardUsernamePasswordCredentials> credOpt = getStandardUsernamePasswordCredentials(this.getCredentialId(), creds);
        Map<String, String> reqJson = getLoginJson(credOpt.orElse(null));
        RdoResult rdoResult = null;
        if (renew == false){
            try {
                rdoResult = RDORequests.getValidToken(Constants.LOGIN_PATH, reqJson);
            } catch (Exception e) {
                throw new RDORuntimeException(e);
            }
        }else {
            try {
                int count = 0;
                do {
                    rdoResult =  RDORequests.getValidToken(Constants.RDOP_RENEW_TOKEN, reqJson);
                    count++;
                } while (rdoResult.getStatus() != Integer.parseInt(MessageCodeConstants.SUCCESS) && count < 3);
            } catch (Exception e) {
                throw new RDORuntimeException(e);
            }
        }

        if (rdoResult.getStatus() != Integer.parseInt(MessageCodeConstants.SUCCESS))
            throw new RDORuntimeException(rdoResult.getMessage());

        LinkedHashMap<String,Object> map = (LinkedHashMap<String, Object>) rdoResult.getPayload();
        if (map != null){
            apiToken = (String) map.get(Constants.LOGIN_TOKEN);
        }
        return apiToken;
    }
    private Optional<StandardUsernamePasswordCredentials> getStandardUsernamePasswordCredentials(String credentialId, List<StandardUsernamePasswordCredentials> creds) {
        Optional<StandardUsernamePasswordCredentials> credOpt = creds.stream()
                .filter(c -> c instanceof StandardUsernamePasswordCredentials
                        && c.getId().equals(credentialId))
                .findFirst();
        return credOpt;
    }


    private List<StandardUsernamePasswordCredentials> getCreds() {
        List<StandardUsernamePasswordCredentials> creds = CredentialsProvider.lookupCredentials(
                StandardUsernamePasswordCredentials.class,
                Jenkins.getInstanceOrNull(),
                ACL.SYSTEM,
                new ArrayList<DomainRequirement>());
        return creds;
    }


    private static Map<String, String> getLoginJson(StandardUsernamePasswordCredentials cred) {
        Map<String, String> reqJson = new HashMap<>();
        String username = cred.getUsername();
        String password = cred.getPassword().getPlainText();
        String encPassword = PasswordUtil.encryptWithENC(password);
        reqJson.put("password", encPassword);
        reqJson.put("username", username);
        return reqJson;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<RDOiConn> {

        public FormValidation doCheckName(@QueryParameter String name) {
            if (StringUtils.isBlank(name)) return FormValidation.error(Messages.RDOi_connection_name_empty());
            return null;
        }

        public FormValidation doCheckUrl(@QueryParameter String url) {
            if (StringUtils.isBlank(url)) return FormValidation.error(Messages.RDOi_connection_url_empty());
            return null;
        }

        public FormValidation doCheckCredentialId(@QueryParameter String credentialId) {
            if (StringUtils.isBlank(credentialId)) return FormValidation.error(Messages.RDOi_connection_credential_empty());
            return null;
        }

        @RequirePOST
        @Restricted(DoNotUse.class)
        public FormValidation doTestConnection(
                @QueryParameter String url,
                @QueryParameter String credentialId) {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);

            List<StandardUsernamePasswordCredentials> creds = CredentialsProvider.lookupCredentials(
                    StandardUsernamePasswordCredentials.class,
                    Jenkins.getInstanceOrNull(),
                    ACL.SYSTEM,
                    new ArrayList<DomainRequirement>());

            Optional<StandardUsernamePasswordCredentials> credOpt = creds.stream()
                    .filter(c -> c instanceof StandardUsernamePasswordCredentials
                            && c.getId().equals(credentialId))
                    .findFirst();

            return testConnection(credOpt.orElse(null), url);
        }

        private List<StandardUsernamePasswordCredentials> getCreds(){
            List<StandardUsernamePasswordCredentials> creds = CredentialsProvider.lookupCredentials(
                    StandardUsernamePasswordCredentials.class,
                    Jenkins.getInstanceOrNull(),
                    ACL.SYSTEM,
                    new ArrayList<DomainRequirement>());
            return creds;
        }

        private FormValidation testConnection(StandardUsernamePasswordCredentials cred, String baseURL) {
            if (cred == null) {
               return FormValidation.error(Messages.RDOi_connection_credential_not_exist());
            }

            Map<String, String> reqJson = getLoginJson(cred);

            RdoResult rdoResult = null;
            try {
                rdoResult = RDORequests.testConnection(Constants.LOGIN_PATH, reqJson, SeparatorHandler.appendURLSeparator(baseURL));
            } catch (Exception e) {
                return FormValidation.error(e.getMessage());
            }
            if (rdoResult.getStatus() == 200) {
                return FormValidation.ok(Messages.RDOi_connection_succeed());
            } else {
                return FormValidation.error(rdoResult.getMessage());
            }
        }

        public ListBoxModel doFillCredentialIdItems(@AncestorInPath Item project,
                                                    @QueryParameter String url,
                                                    @QueryParameter String credentialId) {
            return new StandardListBoxModel()
                    .includeEmptyValue()
                    .includeMatchingAs(
                            ACL.SYSTEM,
                            project,
                            StandardUsernamePasswordCredentials.class,
                            GitURIRequirementsBuilder.fromUri(url).build(),
                            CredentialsMatchers.instanceOf(StandardUsernamePasswordCredentials.class))
                    .includeCurrentValue(credentialId);
        }
    }

    /**
     * Get rdoi connection by name
     *
     * @param connectionName
     * @return
     */
    public static Optional<RDOiConn> getByName(String connectionName) {
        RDOiConnCfg rdOiConnectionConfig = RDOiConnCfg.get();
        var rdoiConnectionOpt = Optional.ofNullable(rdOiConnectionConfig)
                .map(RDOiConnCfg::getConn)
                .map(cl -> cl.stream()
                        .filter(c -> connectionName.equals(c.getName()))
                        .findFirst()
                        .orElse(null));
        return rdoiConnectionOpt;
    }
}
