package org.pekoo.osgi.springboot.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.pekoo.osgi.springboot.http.HttpConsumer;
import org.pekoo.osgi.springboot.http.IHttpConsumer;
import org.pekoo.osgi.springboot.utils.OAuthConfig;
import org.pekoo.osgi.springboot.utils.OAuthDetails;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Service;

/**
 * Witnigs Proxy Service Bean
 * @author Petar Zhivkov
 */
@Service
public class ProxyServiceImpl {
	
	/* Notice that we are not using any logger. It seems there is a problem with Logger.info and Logger.debug on the OSGI continer (only warn and trace works) */
	
	private OAuthConfig appOAuthConfig = null;

	private BundleContext bundleContext;

	private Properties configProperties=new Properties();
	
	private static String bundleName;
	
	private static String serviceComponentName;
	
	private static String serviceComponentClass;
	
	private static final String SERVICE_COMPONENT_KEY = "component.name";
	
	public static final String ENV_PROPERTY_SERVICE_COMPONENT= "withings.authenticator.service";
	
	public static final String ENV_PROPERTY_BUNDLE_NAME = "withings.authenticator.name";
	
	public static final String ENV_PROPERTY_BUNDLE_CLASS = "withings.authenticator.class";

	public void setProxyEnvironment(ConfigurableEnvironment env, BundleContext bundleContext){
		this.bundleContext = bundleContext;
		if(env.containsProperty(ENV_PROPERTY_SERVICE_COMPONENT)) {
			serviceComponentName = env.getProperty(ENV_PROPERTY_SERVICE_COMPONENT);
		} else System.out.println("ERROR: cannot authenticator service component name");
		if(env.containsProperty(ENV_PROPERTY_BUNDLE_CLASS)){
			serviceComponentClass = env.getProperty(ENV_PROPERTY_BUNDLE_CLASS);
		} else System.out.println("ERROR: cannot authenticator service component class");
		if(env.containsProperty(ENV_PROPERTY_BUNDLE_NAME)){
			bundleName = env.getProperty(ENV_PROPERTY_BUNDLE_NAME);
		} else System.out.println("ERROR: cannot resolve authenticator bundle name");
		if(env.containsProperty(OAuthDetails.STATE)) configProperties.put(OAuthDetails.STATE, env.getProperty(OAuthDetails.STATE));
		if(env.containsProperty(OAuthDetails.SCOPE)) configProperties.put(OAuthDetails.SCOPE, env.getProperty(OAuthDetails.SCOPE));
		if(env.containsProperty(OAuthDetails.AUTHENTICATION_SERVER_URL)) configProperties.put(OAuthDetails.AUTHENTICATION_SERVER_URL, env.getProperty(OAuthDetails.AUTHENTICATION_SERVER_URL));
		if(env.containsProperty(OAuthDetails.CLIENT_ID)) configProperties.put(OAuthDetails.CLIENT_ID, env.getProperty(OAuthDetails.CLIENT_ID));
		if(env.containsProperty(OAuthDetails.CLIENT_SECRET)) configProperties.put(OAuthDetails.CLIENT_SECRET, env.getProperty(OAuthDetails.CLIENT_SECRET));
		if(env.containsProperty(OAuthDetails.GRANT_TYPE)) configProperties.put(OAuthDetails.GRANT_TYPE, env.getProperty(OAuthDetails.GRANT_TYPE));
		if(env.containsProperty(OAuthDetails.REDIRECT_URI)) configProperties.put(OAuthDetails.REDIRECT_URI, env.getProperty(OAuthDetails.REDIRECT_URI));
		if(env.containsProperty(OAuthDetails.RESOURCE_SERVER_URL)) configProperties.put(OAuthDetails.RESOURCE_SERVER_URL, env.getProperty(OAuthDetails.RESOURCE_SERVER_URL));
		if(env.containsProperty(OAuthDetails.TOKEN_ENDPOINT_URL)) configProperties.put(OAuthDetails.TOKEN_ENDPOINT_URL, env.getProperty(OAuthDetails.TOKEN_ENDPOINT_URL));
		if(env.containsProperty(OAuthDetails.THIS_SERVER_HOST)) configProperties.put(OAuthDetails.THIS_SERVER_HOST, env.getProperty(OAuthDetails.THIS_SERVER_HOST));
		else configProperties.put(OAuthDetails.THIS_SERVER_HOST, "localhost");
		if(env.containsProperty(OAuthDetails.THIS_SERVER_PORT)) configProperties.put(OAuthDetails.THIS_SERVER_PORT, env.getProperty(OAuthDetails.THIS_SERVER_PORT));
		else configProperties.put(OAuthDetails.THIS_SERVER_PORT, "8190");
		if(env.containsProperty(OAuthDetails.THIS_SERVER_HTTP_SCHEME)) {
			String scheme = env.getProperty(OAuthDetails.THIS_SERVER_HTTP_SCHEME);
			if(scheme.equalsIgnoreCase("https")) configProperties.put(OAuthDetails.THIS_SERVER_HTTP_SCHEME, scheme);
			else configProperties.put(OAuthDetails.THIS_SERVER_HTTP_SCHEME, "http");
		} else configProperties.put(OAuthDetails.THIS_SERVER_HTTP_SCHEME, "http");
		System.out.println("ProxyServiceImpl->Setting configuration properties to: " + configProperties.toString() );
		appOAuthConfig = OAuthConfig.getInstance();
		appOAuthConfig.setOauthProperties(configProperties);
	}
	
	/**
	 * Frequently used proxy App Objects getters
	 * @return
	 */
	public OAuthConfig getAppOAuthConfig(){
		return appOAuthConfig;
	}
	
	public String getAuthorizationUrl(String accountId){
		return appOAuthConfig.getAuthorizationUrl(accountId);		
	}
	
	public String getRrrorUrl(String err){
		return appOAuthConfig.getRrrorUrl(err);		
	}

	public IHttpConsumer getHttpConsumer(){
		return new HttpConsumer();
	}

	/**
	 * Calls 'addAuthenticationAccount' operation from Winthings Binding Bundle installed on the OSGI container with simple reflection
	 */
	public String addAuthenticationAccountResult(String openhabAccointId, String baseUrl){
		Object authenticator = this.getWithingsAuthenticator();
		if(authenticator != null) {
			try{
				Method methodethod  = authenticator.getClass().getDeclaredMethod("addAuthenticationAccount", String.class, String.class);
				methodethod.invoke(authenticator, openhabAccointId, baseUrl);
				return "SUCCESS";
			}catch(NoSuchMethodException | NullPointerException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException  ex){
				System.out.println("ProxyServiceImpl->Error getting/invoke declaredmethod 'addAuthenticationAccount' on the instance: " + authenticator.getClass().getName() +" reason: " + ex.getMessage());
				return "ERROR";
			}
		}
		return "ERROR";
	}
	
	/**
	 * Calls 'startAuthentication' operation from Winthings Binding Bundle installed on the OSGI container with simple reflection
	 */
	public String startAuthenticationResult(String openhabAccointId){
		Object authenticator = this.getWithingsAuthenticator();
		if(authenticator != null) {
			try{
				Method methodethod  = authenticator.getClass().getDeclaredMethod("startAuthentication", String.class);
				methodethod.invoke(authenticator, openhabAccointId);
				return "SUCCESS";
			}catch(NoSuchMethodException | NullPointerException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException  ex){
				System.out.println("ProxyServiceImpl->Error getting/invoke declaredmethod 'startAuthentication' on the instance: " + authenticator.getClass().getName() +" reason: " + ex.getMessage());
				return "ERROR";
			}
		}
		return "ERROR";
	}
	
	/**
	 * Calls 'finishAuthentication' operation from Winthings Binding Bundle installed on the OSGI container with simple reflection
	 */
	public String finishAuthenticationResult(String openhabAccointId,String userId){
		Object authenticator = this.getWithingsAuthenticator();
		if(authenticator != null) {
			try{
				Method methodethod  = authenticator.getClass().getDeclaredMethod("finishAuthentication", String.class, String.class);
				methodethod.invoke(authenticator, openhabAccointId, userId);
				return "SUCCESS";
			}catch(NoSuchMethodException | NullPointerException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException  ex){
				System.out.println("ProxyServiceImpl->Error getting/invoke declaredmethod 'finishAuthentication' on the instance: " + authenticator.getClass().getName() +" reason: " + ex.getMessage());
				return "ERROR";
			}
		}
		return "ERROR";
	}
	
	/**
	 * Gets the remote Winthings Binding Bundle
	 * Very simillar to remote EJB
	 */
	private Object getWithingsAuthenticator(){
		if(this.bundleContext == null){
			System.out.println("ProxyServiceImpl->Can't get OSGI bundle context");
			return null;
		}
		Bundle withingsBundle=null;
		for(Bundle bundle: this.bundleContext.getBundles()){
			String symbolicName = bundle.getSymbolicName();
			if(symbolicName.equals(bundleName)){
				withingsBundle = bundle;
				break;
			}
		}
		if(withingsBundle == null){
			System.out.println("ProxyServiceImpl->Can't find '"+bundleName+"'");
			return null;
		}
		long bId = withingsBundle.getBundleId();
		String location = withingsBundle.getLocation();
		try{
			ServiceReference<?>[] servicesRegistered = withingsBundle.getRegisteredServices();
			if(servicesRegistered.length==0){
				System.out.println("ProxyServiceImpl->Bunlde ("+bId+":"+location+") has no registered services");
				return null;
			}
			ServiceReference<?> authentocatorRefernce = null;
			for(ServiceReference<?> sReference : servicesRegistered){
				String[] keys = sReference.getPropertyKeys();
				if(keys != null && keys.length>0){
					List<String> keyList = Arrays.asList( keys );
					if(keyList.contains(SERVICE_COMPONENT_KEY)){
						String cName = (String)sReference.getProperty(SERVICE_COMPONENT_KEY);
						if(cName != null && cName.equals(serviceComponentName)){
							authentocatorRefernce = sReference;
							break;
						}
					}
				}
			}
			if(authentocatorRefernce != null){
				Object authenticatorService = withingsBundle.getBundleContext().getService(authentocatorRefernce);
				if(authenticatorService != null){
					Class<?> clazz = authenticatorService.getClass();
					if(clazz.getName().equals(serviceComponentClass)) return authenticatorService;
					else System.out.println("ProxyServiceImpl->authenticator instance does not match the required class");	
				} else System.out.println("ProxyServiceImpl->Can't get Withings Authenticator Managed service");
			}
		} catch(Exception e) {
			System.out.println("ProxyServiceImpl->Can't enlist Registered Services for Bunlde ("+bId+") reason: " + e.getMessage());
		}
		return null;
	}

}
