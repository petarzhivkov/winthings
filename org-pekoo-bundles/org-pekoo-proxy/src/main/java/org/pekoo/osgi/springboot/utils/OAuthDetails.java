package org.pekoo.osgi.springboot.utils;

/**
 * Witnigs OAuth 2.0 Detais POJO
 * @author Petar Zhivkov
 */
public class OAuthDetails {
	
	//APP PROPERTIES KEYS
	public static final String ACCESS_TOKEN = "access_token"; 
	public static final String CLIENT_ID = "client_id";		
	public static final String CLIENT_SECRET = "client_secret"; 
	public static final String REFRESH_TOKEN = "refresh_token";
	public static final String CODE = "code";
	public static final String REDIRECT_URI = "redirect_uri";
	public static final String RESPONSE_TYPE = "response_type";
	public static final String AUTHENTICATION_SERVER_URL = "authentication_server_url";
	public static final String TOKEN_ENDPOINT_URL = "token_endpoint_url";
	public static final String GRANT_TYPE = "grant_type";
	public static final String SCOPE = "scope";
	public static final String STATE = "state";
	public static final String RESOURCE_SERVER_URL = "resource_server_url";
	public static final String THIS_SERVER_PORT = "server.port";
	public static final String THIS_SERVER_HOST= "this_server_host";
	public static final String THIS_SERVER_HTTP_SCHEME = "this_server_http_scheme";
	
	//HTTP STAUS CODES
	public static final int HTTP_OK = 200;
	public static final int HTTP_FORBIDDEN = 403;
	public static final int HTTP_ERROR = 400;
	public static final int HTTP_UNAUTHORIZED = 401;
	public static final int HTTP_SEND_REDIRECT = 302;
	
	//HTTP HEADER CONSTANTS
	public static final String AUTHORIZATION = "Authorization";
	public static final String BEARER = "Bearer";
	public static final String TEXT_JSON_CONTENT = "text/json";	
	
	private String scope;
	private String state;
	private String grantType;
	private String clientId;
	private String clientSecret;
	private String redirectURI;
	private String authenticationServerUrl;
	private String tokenEndpointUrl;
	private String resourceServerUrl;
	private String thisServerPort;
	private String thisServerHost;
	private String thisServerHttpScheme;

	public String getTokenEndpointUrl() {
		return tokenEndpointUrl;
	}
	
	public void setTokenEndpointUrl(String tokenEndpointUrl) {
		this.tokenEndpointUrl = tokenEndpointUrl;
	}
	
	public String getRedirectURI() {
		return redirectURI;
	}
	
	public void setRedirectURI(String redirectURI) {
		this.redirectURI = redirectURI;
	}

	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	
	public String getScope() {
		return scope;
	}
	
	public void setScope(String scope) {
		this.scope = scope;
	}
	
	public String getGrantType() {
		return grantType;
	}
	
	public void setGrantType(String grantType) {
		this.grantType = grantType;
	}
	
	public String getClientId() {
		return clientId;
	}
	
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	public String getClientSecret() {
		return clientSecret;
	}
	
	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getAuthenticationServerUrl() {
		return authenticationServerUrl;
	}
	
	public void setAuthenticationServerUrl(String authenticationServerUrl) {
		this.authenticationServerUrl = authenticationServerUrl;
	}

	public String getResourceServerUrl() {
		return resourceServerUrl;
	}
	
	public void setResourceServerUrl(String resourceServerUrl) {
		this.resourceServerUrl = resourceServerUrl;
	}

	public String getThisServerPort() {
		return thisServerPort;
	}

	public void setThisServerPort(String thisServerPort) {
		this.thisServerPort = thisServerPort;
	}

	public String getThisServerHost() {
		return thisServerHost;
	}

	public void setThisServerHost(String thisServerHost) {
		this.thisServerHost = thisServerHost;
	}

	public String getThisServerHttpScheme() {
		return thisServerHttpScheme;
	}

	public void setThisServerHttpScheme(String thisServerHttpScheme) {
		this.thisServerHttpScheme = thisServerHttpScheme;
	}

}
