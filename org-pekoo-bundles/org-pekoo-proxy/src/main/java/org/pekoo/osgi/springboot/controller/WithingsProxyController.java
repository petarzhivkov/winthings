package org.pekoo.osgi.springboot.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import org.pekoo.osgi.springboot.http.HttpConsumer;
import org.pekoo.osgi.springboot.service.impl.ProxyServiceImpl;
import org.pekoo.osgi.springboot.utils.JsonHelper;
import org.pekoo.osgi.springboot.utils.OAuthConfig;
import org.pekoo.osgi.springboot.utils.OAuthDetails;
import org.pekoo.osgi.springboot.utils.OAuthUserData;

/**
 * Witnigs Proxy RestController
 * @author Petar Zhivkov
 */
@RestController
@RequestMapping("/withings-spring")
public class WithingsProxyController {
	
	@Autowired
	private ProxyServiceImpl proxyService;
	
	/**
	 * Creates simple authorize account form
	 */
	@GetMapping(value="/account", produces = MediaType.TEXT_HTML_VALUE)
	public String accountForm() {
		OAuthConfig appconfig = proxyService.getAppOAuthConfig();
		String href = "<a href=\"" + getAppBaseUrl(appconfig) + "/register" + "\">Register</a>";
	    return createSimpleAccontNameForm("/account", "Authorize OpenHab Withings User", href);
	}
	
	/**
	 * Proccess login form data
	 */
	@PostMapping(value="/account", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
	public RedirectView accountSubmit(@RequestParam(required=true) MultiValueMap<String,String> paramMap) {
		RedirectView redirectView = new RedirectView();
		OAuthConfig appconfig = proxyService.getAppOAuthConfig();
		String accountName = paramMap.getFirst("accountName");
		if(accountName != null && !accountName.isEmpty()){
			String thisServerUrl = getAppBaseUrl(appconfig) +"/authorize?openhabId=" + accountName;;
			redirectView.setUrl(thisServerUrl);
		} else redirectView.setUrl(proxyService.getRrrorUrl("Invalid account name"));
	    return redirectView;
	}
	
	/**
	 * Creates simple register account form
	 */
	@GetMapping(value="/register", produces = MediaType.TEXT_HTML_VALUE)
	public String registerForm() {
	    return createSimpleAccontNameForm("/register", "Register OpenHab Withings User", null);
	}
	
	/**
	 * Proccess register form data
	 */
	@PostMapping(value="/register", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
	public RedirectView registerSubmit(@RequestParam(required=true) MultiValueMap<String,String> paramMap) {
		RedirectView redirectView = new RedirectView();
		OAuthConfig appconfig = proxyService.getAppOAuthConfig();
		String accountName = paramMap.getFirst("accountName");
		if(accountName != null && !accountName.isEmpty()){
			/* Register the new user OpenHab user to Withing bindings Bundle */
			String responce = proxyService.addAuthenticationAccountResult(accountName, getAppBaseUrl(appconfig)+"/");
			if(responce.equals("ERROR")) {
				redirectView.setUrl(proxyService.getRrrorUrl("Failed To Register OpenHab Withings User: " + accountName));
				return redirectView;
			}
			String thisServerUrl = getAppBaseUrl(appconfig)+"/account";
			redirectView.setUrl(thisServerUrl);
		} else redirectView.setUrl(proxyService.getRrrorUrl("Invalid account name"));
	    return redirectView;
	}

    /**
	 * Gneric error page
	 */
    @GetMapping(value="/error", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String error(@RequestParam(name = "errorId", required=false)  String errorId) {
    	String errResponse = "WITHINGS PROXY ERROR";
    	if(HttpConsumer.isNotBlank(errorId)) errResponse+=":"+errorId;
        return errResponse;
    }
    
    /**
     * On success Oauth 2 athorization page
     */
    @GetMapping(value="/success", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String success(@RequestParam(name = "openhabId", required=true)  String openhabId) {
        return "Successfuly Registered OpenHab Withings User: " + openhabId;
    }
    
    /**
	 * Withings OAuth 2.0 Authorization entry point
	 * @param openhabId - OpenHabb user ID
	 */
    @GetMapping("/authorize")
    public RedirectView authoriztionRedirect(@RequestParam(name = "openhabId", required=true)  String openhabId) {
    	RedirectView redirectView = new RedirectView();
    	/* Start Authorization in Withing bindings Bundle */
    	String responce = proxyService.startAuthenticationResult(openhabId);
    	if(responce.equals("SUCCESS")) redirectView.setUrl(proxyService.getAuthorizationUrl(openhabId));
    	else redirectView.setUrl(proxyService.getRrrorUrl("ERROR CALLING WITHIGS BINDING BUNDLE"));
    	/* Redirect the browser to withings authorization end point (Obviously this user needs account in Withings too) */
		return redirectView;
    }
    
    /**
	 * Withings OAuth 2.0 callback URL
	 */
    @GetMapping(value="/callback", produces = MediaType.APPLICATION_JSON_VALUE)
    public RedirectView authoriztionCallback(@RequestParam(name = "code", required=true) String code, @RequestParam(name = "state", required=true) String state) throws IOException {
    	RedirectView redirectView = new RedirectView();
    	OAuthConfig appconfig = proxyService.getAppOAuthConfig();
		if(appconfig.isExistsInCache(state)){
			String [] splitedState = state.split("-");
			String accountId="";
			if(splitedState != null) accountId=splitedState[0];
			else {
				redirectView.setUrl(proxyService.getRrrorUrl("AUTHORIZATION STATE INVALID"));
				return redirectView;	
			}
			Map<String,Object> accessTokenReponce = proxyService.getHttpConsumer().getAccessToken(appconfig.getOauthDetails(), code);
			if(accessTokenReponce.containsKey("error")) {
				redirectView.setUrl(proxyService.getRrrorUrl("FAILED TO GET ACCESS TOKEN"));
				return redirectView;	
			}
			String userId = (String) accessTokenReponce.get("userid");
			OAuthUserData userAuthData = new OAuthUserData();
		    userAuthData.setUserId(userId);
		    userAuthData.setAccess_token((String) accessTokenReponce.get(OAuthDetails.ACCESS_TOKEN));
		    userAuthData.setRefresh_token((String) accessTokenReponce.get(OAuthDetails.REFRESH_TOKEN));
		    userAuthData.setExpires_in(String.valueOf((Integer) accessTokenReponce.get("expires_in")));
		    userAuthData.setScope((String) accessTokenReponce.get(OAuthDetails.SCOPE));
		    userAuthData.setToken_type((String) accessTokenReponce.get("token_type"));
		    appconfig.registerUserAuthenticatedSession(accountId, userAuthData);
		    /* Try Auhtorization finish in Withing bindings Bundle */
		    String responce = proxyService.finishAuthenticationResult(accountId, userId);
		    if(responce.equals("SUCCESS")) {
			    String successUrl= getAppBaseUrl(proxyService.getAppOAuthConfig())+"/success"+"?openhabId=" + HttpConsumer.urlEncodedString(accountId);
			    redirectView.setUrl(successUrl); 
		    } else redirectView.setUrl(proxyService.getRrrorUrl("ERROR CALLING WITHIGS BINDING BUNDLE"));
		    return redirectView;
		} else {
			redirectView.setUrl(proxyService.getRrrorUrl("AUTHORIZATION CODE/STATE INVALID OR REUSED"));
			return redirectView;
		}
    }
    
    /**
	 * Withings OAuth 2.0 get protected reosurse 'getmeas'. Normaly called by Withing bindings Bundle
	 * @param accountid - Withings Account ID
	 */
	@GetMapping(path = "/measure/{accountid}/getmeas", produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody
    public String getmeas(@PathVariable(required=true) String accountid,
    		@RequestParam(name = "meastype", required=false) String meastype,
    		@RequestParam(name = "category", required=false) String category,
    		@RequestParam(name = "startdate", required=false) String startdate,
    		@RequestParam(name = "enddate", required=false) String enddate,
    		@RequestParam(name = "offset", required=false) String offset,
    		@RequestParam(name = "lastupdate", required=false) String lastupdate) throws IOException {
		OAuthConfig appconfig = proxyService.getAppOAuthConfig();
		if(appconfig.getUserAuthData(accountid)!=null){
			final String reourceServerUrl = appconfig.getOauthDetails().getResourceServerUrl();
			Map<String, String> queryParams= new  HashMap<>();
			if(HttpConsumer.isNotBlank(meastype)) queryParams.put("meastype", meastype);
			if(HttpConsumer.isNotBlank(category)) queryParams.put("category", category);
			if(HttpConsumer.isNotBlank(startdate)) queryParams.put("startdate", startdate);
			if(HttpConsumer.isNotBlank(enddate)) queryParams.put("enddate", startdate);
			if(HttpConsumer.isNotBlank(offset)) queryParams.put("offset", offset);
			if(HttpConsumer.isNotBlank(lastupdate)) queryParams.put("lastupdate", lastupdate);
			String queryString = HttpConsumer.addQueryParams(queryParams, reourceServerUrl + "/measure?action=getmeas");
			Map<String, Object> successReponce = proxyService.getHttpConsumer().getProtectedResource(appconfig.getUserAuthData(accountid), queryString);
			return JsonHelper.getHelper().objectMapToJsonConverter(successReponce);
		} else return errorResponceHandling("UNAUTHORIZED", OAuthDetails.HTTP_UNAUTHORIZED);
    }
    
	/**
	 * Genrates JSON error response string
	 */
    private String errorResponceHandling(String errorMsg, int err_code) throws IOException {
		System.out.println(errorMsg);
		Map<String,Object> errorTokenReponce = new HashMap<>();
		errorTokenReponce.put("error", errorMsg);
		errorTokenReponce.put("code", err_code);
		return JsonHelper.getHelper().objectMapToJsonConverter(errorTokenReponce);
	}
    
	/**
	 * Generic simple form creator
	 */
	private String createSimpleAccontNameForm(String action, String formName, String hrefLink){
		String accPage="<!DOCTYPE HTML>";
		accPage+="<html>";
		accPage+="<head></head>";
		accPage+="<body>";
		accPage+="<h1>"+formName+"</h1>";
		accPage+="<form name='f' action=\"/withings-spring"+action+"\" method='POST'>";
		accPage+="<table>";
		accPage+="<tr>";
		accPage+="<td>User:</td>";
		accPage+="<td><input type='text' name='accountName' value=''></td>";
		accPage+="</tr>";
		accPage+="<tr>";
		accPage+="<td><input name=\"submit\" type=\"submit\" value=\"submit\" /></td>";
		accPage+="</tr>";
		accPage+="</table>";
		if(HttpConsumer.isNotBlank(hrefLink)) accPage+=hrefLink;
		accPage+="</body>";
		return accPage;
	}

	/**
	 * Get this server base url
	 */
	private String getAppBaseUrl(OAuthConfig appconfig){
		OAuthDetails outhDetails = appconfig.getOauthDetails();
		String scheme = outhDetails.getThisServerHttpScheme();
		String host = outhDetails.getThisServerHost();
		String port = outhDetails.getThisServerPort();
		return scheme+"://" + host + ":" + port+"/withings-spring";
	}
 
}
