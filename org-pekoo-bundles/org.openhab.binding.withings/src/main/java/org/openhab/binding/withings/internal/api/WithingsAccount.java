package org.openhab.binding.withings.internal.api;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openhab.binding.withings.internal.helper.StringIoUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WithingsAccount {

	private static final Logger logger = LoggerFactory.getLogger(WithingsAccount.class);

    private static final String SERVICE_NAME = "withings";

    private static final String CONFIG_DIR = "." + File.separator + "configurations";

    private static final String CONTENT_DIR = CONFIG_DIR + File.separator + "services";

    private String accountId;
    
    private String userId;
    
    public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	ServiceRegistration<?> clientServiceRegistration;

	private String proxyUrl = null;
    
    public WithingsAccount(String accountId, String proxyUrl) {
        this.accountId = accountId;
        this.proxyUrl = proxyUrl;
    }
    
    public String getProxyUrl(){
    	return this.proxyUrl;
    }
    
    public void setProxyUrl(String proxyUrl){
    	this.proxyUrl = proxyUrl;
    }
    
    public void registerAccount(BundleContext bundleContext) {

        Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>();
        serviceProperties.put("withings.accountid", this.accountId);
        serviceProperties.put("withings.userid", this.userId);

        if (this.clientServiceRegistration != null) this.clientServiceRegistration.unregister();

        this.clientServiceRegistration = bundleContext.registerService(WithingsApiClient.class.getName(), new WithingsApiClient(this.userId, this.accountId, this.proxyUrl), serviceProperties);
    }
    
    public boolean isAuthenticated() {
        return StringIoUtils.isNotBlank(userId);
    }
    
    public WithingsApiClient getAccountApiClient(BundleContext bundleContext){
    	WithingsApiClient client = null;
    	if (this.clientServiceRegistration != null){
    		try{
	    		client = (WithingsApiClient) bundleContext.getService(this.clientServiceRegistration.getReference());
    		} catch(IllegalStateException ise){
    			logger.error("(85) WithingsAccount->error reason"+ise.getMessage());
    		}
    	}else{
    		logger.error("(86) WithingsAccount->clientServiceRegistration object is null!!");
    	}
		return client;	
    }
    
    public void unregisterAccount() {
        if (this.clientServiceRegistration != null) {
            this.clientServiceRegistration.unregister();
        }
    }
    
    public void persist() {
        File file = null;
        String prefix = "";

        try {
            // store properties either to openhab.cfg (legacy) or
            // services/withings.cfg
            if (isLegacyConfiguration()) {
                file = new File(CONFIG_DIR + File.separator + "openhab.cfg");
                // in legacy case each property has to be prefixed with
                prefix = SERVICE_NAME + ":";
            } else {
                file = new File(CONTENT_DIR + File.separator + SERVICE_NAME + ".cfg");
            }

            if (!file.exists()) {
                logger.warn("Configuration file '{}' was not found. Creating it now...", file.getAbsolutePath());
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            // if an account different from the default account is used
            // it gets prefixed separated by "."
            if (!WithingsAuthenticator.DEFAULT_ACCOUNT_ID.equals(accountId)) {
                prefix += accountId + ".";
            }

            Map<String, String> config = load(file);

            config.put(prefix + "userid", userId);
            
            config.put(prefix + "proxy", proxyUrl);

            store(config, file);

            logger.warn("Saved Withings account to file '{}'.", file.getAbsolutePath());
        } catch (IOException ioe) {
            logger.warn("Couldn't write Withings account to file '{}'.", file.getAbsolutePath());
        }
    }

    /**
     * Checks whether the Binding configuration has been stored to
     * openhab.cfg rather than services/withings.cfg.
     * 
     * @return true, if there is a configuration key like "withings-oauth"
     *         in openhab.cfg and false in all other cases.
     */
    private boolean isLegacyConfiguration() {
        File file = new File(CONFIG_DIR + File.separator + "openhab.cfg");

        try {
            if (file.exists()) {
                Map<String, String> config = load(file);
                for (Object key : config.keySet()) {
                    if (key.toString().startsWith(SERVICE_NAME)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            logger.warn("Couldn't open Configuration File '{}'", file.getAbsolutePath());
        }

        return false;
    }

	private Map<String, String> load(File file) throws IOException {
        Map<String, String> config = new LinkedHashMap<String, String>();
        FileInputStream is = new FileInputStream(file);
		List<String> lines = StringIoUtils.readLines(is, Charset.defaultCharset());
        for (String line : lines) {
            String[] parameterPair = line.split("=");
            if (parameterPair.length == 2) {
                config.put(parameterPair[0].trim(), parameterPair[1].trim());
            } else {
                config.put(parameterPair[0], "");
            }
        }
        if(is != null) is.close();
        return config;
    }
    
	private Map<String, String> store(Map<String, String> config, File file) throws IOException {
        FileOutputStream os = new FileOutputStream(file);

        List<String> lines = new ArrayList<String>();
        for (Entry<String, String> line : config.entrySet()) {
            String value = StringIoUtils.isBlank(line.getValue()) ? "" : "=" + line.getValue();
            lines.add(line.getKey() + value);
        }

        StringIoUtils.writeLines(lines, System.getProperty("line.separator"), os, Charset.defaultCharset());
        if(os != null) os.close();
        return config;
    }

    @Override
    public String toString() {
        return "WithingsAccount [userId=" + userId + "]";
    }


	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	
	public String getAccountId(){
		return this.accountId;
	}

}
