package org.openhab.binding.withings.internal.api;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.openhab.binding.withings.internal.helper.StringIoUtils;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WithingsAuthenticator implements ManagedService {
	
	private static final Logger logger = LoggerFactory.getLogger(WithingsAuthenticator.class);
    
    private static final String LINE = "#########################################################################################";

    public static final String DEFAULT_ACCOUNT_ID = "DEFAULT_ACCOUNT_ID";

    private ComponentContext componentContext;

    private Map<String, WithingsAccount> accountsCache = new HashMap<String, WithingsAccount>();
    
    private String proxyUrl = null;

    protected void activate(ComponentContext componentContext) {
        this.componentContext = componentContext;
    }
    
    protected void deactivate(ComponentContext componentContext) {
        this.componentContext = null;
        unregisterAccounts();
    }
    
    private WithingsAccount getAccount(String accountId) {
        return accountsCache.get(accountId);
    }
    
    public synchronized void addAuthenticationAccount(String accountId, String proxyUrl){
    	WithingsAccount withingsAccount = getAccount(accountId);
        if (withingsAccount == null) {
            logger.info("Adding account for "+accountId+"");
            withingsAccount = new WithingsAccount(accountId, proxyUrl);
            accountsCache.put(accountId, withingsAccount);
        }else{
        	logger.warn("Account "+accountId+" already exists", accountId);
        }
    }
    
    /**
     * Starts the authentication flow.
     * @param accountId
     */
    public synchronized void startAuthentication(String accountId) {

        WithingsAccount withingsAccount = getAccount(accountId);
        if (withingsAccount == null) {
            logger.warn("Couldn't find Credentials of Account "+accountId+". Please check openhab.cfg or withings.cfg.");
            printSetupInstructions();
            return;
        }
    }
    
    /**
     * Finishes the authentication flow.
     * @param accountId
     * @param userId
     */
    public synchronized void finishAuthentication(String accountId, String userId) {
        WithingsAccount withingsAccount = getAccount(accountId);
        if (withingsAccount == null) {
            logger.error("Couldn't find Credentials of Account '"+accountId+"'. Please check openhab.cfg or withings.cfg.");
            return;
        }
        String accId = withingsAccount.getAccountId();
        if(accId != null && accId.equals(accountId)){
	        withingsAccount.setUserId(userId);
	        withingsAccount.registerAccount(componentContext.getBundleContext());
	        withingsAccount.persist();
        }else{
        	logger.error("Couldn't finish authentication for Account '"+accId+"' current account is set to '"+accountId+"'");
            return;
        }
        printAuthenticationSuccessful();
    }
    
    private void printSetupInstructions() {
    	logger.warn(LINE);
    	logger.warn("# Withings Binding Setup: ");
    	logger.warn("# 1. Install the proxy bundle 'org-pekoo-proxy-{version}.jar' under './openhab-2.5.4/addons' directory");
    	logger.warn("# 2. Open a browser window \"http://localhost:{proxy.port}/withings-spring/register\". Default {proxy.port} is 8190");
    	logger.warn("# 3. Register a new openHab userId (e.g. Thomas) and submit. You will be redirected to Withings grant authorization page.");
    	logger.warn("# 4. In the Withings grant authorization page you have to grant persmission for openHab to access Withings data. You have to have own withings account registred first.");
    	logger.warn(LINE);
    }
    
    private void printAuthenticationSuccessful() {
    	System.out.println(LINE);
    	System.out.println("# Withings authentication SUCCEEDED. Binding is now ready to work.");
    	System.out.println(LINE);
    }
    
	@Override
	public void updated(Dictionary<String, ?> config) throws ConfigurationException {
		if (config != null) {
			
			String proxyBaseUrl = (String) config.get("proxyUrl");
            if (StringIoUtils.isNotBlank(proxyBaseUrl)) {
                proxyUrl = proxyBaseUrl;
            }
			
            Enumeration<String> configKeys = config.keys();
            while (configKeys.hasMoreElements()) {
                String configKey = configKeys.nextElement();

                // the config-key enumeration contains additional keys that we
                // don't want to process here ...
                if ("service.pid".equals(configKey) || "proxyUrl".equals(configKey) || "withingd.proxy.url".equals(configKey)) {
                    continue;
                }

                String accountId;
                String configKeyTail;

                if (configKey.contains(".")) {
                    String[] keyElements = configKey.split("\\.");
                    accountId = keyElements[0];
                    configKeyTail = keyElements[1];

                } else {
                    accountId = DEFAULT_ACCOUNT_ID;
                    configKeyTail = configKey;
                }

                WithingsAccount account = accountsCache.get(accountId);
                if (account == null) {
                	account = new WithingsAccount(accountId, proxyUrl);
                    accountsCache.put(accountId, account);
                }

                String value = (String) config.get(configKeyTail);

                if ("userid".equals(configKeyTail)) {
                    account.setUserId(value);
                } else if ("proxy".equals(configKeyTail)) {
                    account.setProxyUrl(value);
                } else {
                    String msg = "The configuration key '" + configKey + "' is not recognized and will be ignored.";
                    logger.warn(msg);
                    throw new ConfigurationException(configKey, msg);
                }
            }

            registerAccounts();
        }
		
	}
	
	private void registerAccounts() {
        for (Entry<String, WithingsAccount> entry : accountsCache.entrySet()) {

            String accountId = entry.getKey();
            WithingsAccount account = entry.getValue();

            if (account.isAuthenticated()) {
                account.registerAccount(componentContext.getBundleContext());
            } else {
                logger.warn( "Configuration details of Account '"+accountId+"' are invalid. Please check the configuration.");
            }
        }
    }
	
    private void unregisterAccounts() {
        for (WithingsAccount account : accountsCache.values())  account.unregisterAccount();
    }

}
