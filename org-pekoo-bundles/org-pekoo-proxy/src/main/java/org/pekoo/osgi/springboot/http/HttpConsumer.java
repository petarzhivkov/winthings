package org.pekoo.osgi.springboot.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.pekoo.osgi.springboot.utils.JsonHelper;
import org.pekoo.osgi.springboot.utils.OAuthConfig;
import org.pekoo.osgi.springboot.utils.OAuthDetails;
import org.pekoo.osgi.springboot.utils.OAuthUserData;

/**
 * Witnigs OAuth 2.0 HTTP Consumer
 * @author Petar Zhivkov
 */
public class HttpConsumer implements IHttpConsumer {

	/**
	 * Creates authorizarion token request URL with custom 'state' query param 
	 */
	public static String authoriztionBrowserRedirectUrl(OAuthDetails oauthDetails, String accountId) throws UnsupportedEncodingException{
		Map<String,String> queryParams = new HashMap<>();
		queryParams.put(OAuthDetails.RESPONSE_TYPE, OAuthDetails.CODE);
		queryParams.put(OAuthDetails.CLIENT_ID, oauthDetails.getClientId());
		queryParams.put(OAuthDetails.REDIRECT_URI, oauthDetails.getRedirectURI());
		queryParams.put(OAuthDetails.SCOPE, oauthDetails.getScope());
		queryParams.put(OAuthDetails.RESPONSE_TYPE, OAuthDetails.CODE);
		
		/* Generate the new state value and temporary put it to guava cache. 
		 * We expect this value to be returned later from withings and this 
		 * value needs to be verified against the cache (spoofing atacks ) */
		String usedState = accountId + "-" +oauthDetails.getState();
		OAuthConfig.getInstance().putStateToCache(usedState);
		
		queryParams.put(OAuthDetails.STATE, usedState);
		return addQueryParams(queryParams, oauthDetails.getAuthenticationServerUrl());
	}

	/**
	 * Get the access token with the received Withings authorization code
	 */
	@Override
	public Map<String, Object> getAccessToken(OAuthDetails oauthDetails, String authorizationCode) throws IOException {
		Map<String, Object> bodyMap = new HashMap<String, Object>();
		bodyMap.put(OAuthDetails.GRANT_TYPE,oauthDetails.getGrantType());
		bodyMap.put(OAuthDetails.CODE, authorizationCode);
		bodyMap.put(OAuthDetails.CLIENT_ID, oauthDetails.getClientId());
		bodyMap.put(OAuthDetails.CLIENT_SECRET, oauthDetails.getClientSecret());
		bodyMap.put(OAuthDetails.REDIRECT_URI,oauthDetails.getRedirectURI());
		byte[] postDataBytes = createPostDataBytes(bodyMap);
		String response = executePostRequest(oauthDetails.getTokenEndpointUrl(), postDataBytes);
		return handleResponse(response);
	}
	
	/**
	 * Get a protected resource from Withings using the access token
	 */
	@Override
	public Map<String, Object> getProtectedResource(OAuthUserData userAuthData, String resourceUrl) throws IOException {
		String response = executeGetRequest(resourceUrl, userAuthData.getAccess_token());
		Map<String, Object> map = handleResponse(response);
		if (map.containsKey("error") && map.containsKey("status")) {
			String err = (String) map.get("error");
			Integer status = (Integer) map.get("status");
			if (err.contains("invalid_token") && status.intValue() == OAuthDetails.HTTP_UNAUTHORIZED) {
				/* Access token has expired. Refresh it */
				map = refreshAccessToken(userAuthData);
				if (map.containsKey(OAuthDetails.ACCESS_TOKEN)) {
					String newAccessToken = (String) map.get(OAuthDetails.ACCESS_TOKEN);
					userAuthData.setAccess_token(newAccessToken);
					userAuthData.setRefresh_token((String) map.get(OAuthDetails.REFRESH_TOKEN));
					response = executeGetRequest(resourceUrl, newAccessToken);
					map = handleResponse(response);
				}
			}
		}
		return map;
	}

	/**
	 * Rfreshes the tithings using the access token aftre expire
	 */
	private Map<String, Object> refreshAccessToken(OAuthUserData userAuthData) throws IOException {
		Map<String, Object> bodyMap = new HashMap<String, Object>();
		final OAuthDetails oauthDetails = OAuthConfig.getInstance().getOauthDetails();
		bodyMap.put(OAuthDetails.GRANT_TYPE, OAuthDetails.REFRESH_TOKEN);
		bodyMap.put(OAuthDetails.REFRESH_TOKEN, userAuthData.getRefresh_token());
		bodyMap.put(OAuthDetails.CLIENT_ID, oauthDetails.getClientId());
		bodyMap.put(OAuthDetails.CLIENT_SECRET, oauthDetails.getClientSecret());
		byte[] postDataBytes = createPostDataBytes(bodyMap);
		String response = executePostRequest(oauthDetails.getTokenEndpointUrl(), postDataBytes);
		return handleResponse(response);
	}
	
	/**
	 * Withings POST http request
	 */
	private String executePostRequest(String urlString, byte[] postDataBytes) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
		conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);
        return getHttpResponseBody(conn);
	}

	/**
	 * Withings GET http request
	 */
	private String executeGetRequest(String urlString, String accessToken) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
		conn.setRequestProperty("User-Agent", "Mozilla/5.0");
		conn.setRequestProperty("Accept", OAuthDetails.TEXT_JSON_CONTENT);
		conn.setRequestProperty(OAuthDetails.AUTHORIZATION, getAuthorizationHeaderForAccessToken(accessToken));
		return getHttpResponseBody(conn);
	}
	
	/**
	 * Get http response body
	 */
	private String getHttpResponseBody(HttpURLConnection conn) throws IOException {
        int responseCode = conn.getResponseCode();
        String responseString="", errMsg="";
        boolean hasError = false;
        if (responseCode == 200) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
            	response.append(line);
            }
            responseString = response.toString();
            reader.close();
        } else {
        	hasError = true;
        	errMsg = conn.getResponseMessage();
        }
        conn.disconnect();
        if(hasError) throw new IOException(errMsg);
		return responseString;
	}
	
	/**
	 * Create POST url encoded form body data 
	 */
	private static byte[] createPostDataBytes(Map<String, Object> params) throws UnsupportedEncodingException {
		StringBuilder postData = new StringBuilder();
        for (Map.Entry<String,Object> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            postData.append(urlEncodedString(param.getKey()));
            postData.append('=');
            postData.append(urlEncodedString(String.valueOf(param.getValue())));
        }
		return postData.toString().getBytes("UTF-8");
	}
	
	public static String urlEncodedString(String string) throws UnsupportedEncodingException {
		return URLEncoder.encode(string, "UTF-8");
	}
	
	/**
	 * Adds query parms to the given URL address 
	 */
	public static String addQueryParams(Map<String, String> queryParams, String addr) throws UnsupportedEncodingException {
		String url = addr;
		if(queryParams != null && !queryParams.isEmpty()){
			for (Map.Entry < String, String > item : queryParams.entrySet()) {
			   String key = urlEncodedString(item.getKey());
			   String value = urlEncodedString(item.getValue());
			   if (!url.contains("?")) url += "?" + key + "=" + value;
			   else url += "&" + key + "=" + value;
			}
		}
		return url;
	}

	/**
	 * Sets the Bearer token authorization header
	 */
	private static String getAuthorizationHeaderForAccessToken(String accessToken) {
		return OAuthDetails.BEARER + " " + accessToken;
	}
	
	/**
	 * Handles HTTP response body 
	 */
	private static Map<String,Object> handleResponse(String response) throws IOException {
		JsonHelper<Object> jsonHelper = JsonHelper.getHelper();
		return jsonHelper.jsonToObjectMapConverter(response);
	}
	
	/**
	 * Frequently used java.lang.String blank checker (Avoiding appache comomos as it causes OSGI container isssues)
	 */
	public static boolean isBlank(String str) {
		return (str == null || str.trim().isEmpty());
	}

	/**
	 * Frequently used java.lang.String non-blank checker (Avoiding appache comomos as it causes OSGI container isssues)
	 */
	public static boolean isNotBlank(String str) {
		return !isBlank(str);
	}

}
