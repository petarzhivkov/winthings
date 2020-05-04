package org.openhab.binding.withings.internal.api;


import java.util.Dictionary;
import java.util.Hashtable;


import org.openhab.binding.withings.internal.helper.StringIoUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WithingsAccount {

	private static final Logger logger = LoggerFactory.getLogger(WithingsAccount.class);

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
    	WithingsPersist.getNewWithingsPersist().persist(accountId, userId, proxyUrl);
    }

    @Override
    public String toString() {
        return "WithingsAccount [userId=" + userId + "]" + "[accountId=" + accountId + "]" +  "[proxyUrl=" + proxyUrl + "]";
    }


	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	
	public String getAccountId(){
		return this.accountId;
	}

}
