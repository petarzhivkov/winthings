package org.pekoo.osgi.springboot.utils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.pekoo.osgi.springboot.http.HttpConsumer;

/**
 * Witnigs OAuth 2.0 configuration POJO
 * @author Petar Zhivkov
 */
public class OAuthConfig {
	
	/* Notice that we are not using any logger. It seems there is a problem with Logger.info and Logger.debug on the OSGI continer (only warn and trace works) */

	private static volatile OAuthConfig instance = null;
	
	private OAuthDetails oauthApiDetails = new OAuthDetails();
	
	private Map<String, OAuthUserData> userSessionData = new HashMap<>();
	
	private OAuthConfig(){}
	
	private boolean isConfigured = false;
	
	private static LoadingCache<String, String> statesEvictionCache;

	public static OAuthConfig getInstance(){
		if(instance==null){
			synchronized(OAuthConfig.class){
				if(instance==null) instance = new OAuthConfig();
			}
		}
		return instance;
	}
	
	public void setOauthProperties(Properties config){
		if(!isConfigured) {			
			oauthApiDetails.setGrantType((String) config.getProperty(OAuthDetails.GRANT_TYPE, "authorization_code"));	
			oauthApiDetails.setClientId((String) config.get(OAuthDetails.CLIENT_ID));
			oauthApiDetails.setClientSecret((String) config.get(OAuthDetails.CLIENT_SECRET));
			oauthApiDetails.setAuthenticationServerUrl((String) config.get(OAuthDetails.AUTHENTICATION_SERVER_URL));
			oauthApiDetails.setTokenEndpointUrl((String) config.get(OAuthDetails.TOKEN_ENDPOINT_URL));
			oauthApiDetails.setRedirectURI((String) config.get(OAuthDetails.REDIRECT_URI));
			oauthApiDetails.setState((String) config.getProperty(OAuthDetails.STATE,"user.metrics"));
			oauthApiDetails.setScope((String) config.get(OAuthDetails.SCOPE));
			oauthApiDetails.setResourceServerUrl((String) config.get(OAuthDetails.RESOURCE_SERVER_URL));
			oauthApiDetails.setThisServerPort((String) config.get(OAuthDetails.THIS_SERVER_PORT));
			oauthApiDetails.setThisServerHost((String) config.getProperty(OAuthDetails.THIS_SERVER_HOST,"localhost"));
			oauthApiDetails.setThisServerHttpScheme((String) config.getProperty(OAuthDetails.THIS_SERVER_HTTP_SCHEME,"http"));
			statesEvictionCache = createSatesEvictionCache(300,TimeUnit.SECONDS);
			isConfigured = true;
		} else System.out.println("OAuthConfig->OAuth settings cant be reconfigured");
	}
	
	private LoadingCache<String, String> createSatesEvictionCache(int evictionDuration, TimeUnit tUnit) {
		CacheLoader<String, String> cacheLoader;
	    cacheLoader = new CacheLoader<String, String>() {
	        @Override
	        public String load(String key) {
	            return key;
	        }
	    };
	    LoadingCache<String, String> cache;
	    cache = CacheBuilder.newBuilder().expireAfterAccess(evictionDuration,tUnit).build(cacheLoader);
		return cache;
	}

	public OAuthDetails getOauthDetails() {
		return oauthApiDetails;
	}
	
	public void registerUserAuthenticatedSession(String id, OAuthUserData userAuthData){
		userSessionData.put(id, userAuthData);
	}
	
	public void unregisterUserAuthenticatedSession(String id){
		if(userSessionData.containsKey(id)) userSessionData.remove(id);
	}
	
	public OAuthUserData getUserAuthData(String id){
		return userSessionData.get(id);
	}
	
	public void clearUserAuthData(){
		userSessionData.clear();
	}
	
	public void putStateToCache(String cstate){
		statesEvictionCache.getUnchecked(cstate);
	}
	
	private String checkStateInCache(String cstate){
		if(cstate==null || cstate.isEmpty()) return null;
		return statesEvictionCache.getIfPresent(cstate);
	}
	
	public boolean isExistsInCache(String cstate){
		String state = checkStateInCache(cstate);
		if(state==null || state.isEmpty()) return false;
		else return true;
	}

	public String getAuthorizationUrl(String accountId) {
		String authorizationUrl="";
		try {
			authorizationUrl = HttpConsumer.authoriztionBrowserRedirectUrl(oauthApiDetails, accountId);
		} catch (UnsupportedEncodingException e) {
			System.out.println("OAuthConfig->"+e.getMessage());
		}
		return authorizationUrl;
	}

	public String getRrrorUrl(String err) {
		String errorUrl=oauthApiDetails.getThisServerHttpScheme()+"://" + oauthApiDetails.getThisServerHost() +":"+oauthApiDetails.getThisServerPort()+"/withings-spring/error";
		if(err!=null && !err.isEmpty()) {
			try{
				errorUrl+="?errorId=" + HttpConsumer.urlEncodedString(err);
			}catch(UnsupportedEncodingException uee){
				//eat that
			}
		}
		return errorUrl;
	}

}
