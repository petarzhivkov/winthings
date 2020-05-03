package org.pekoo.osgi.springboot.http;

import java.io.IOException;
import java.util.Map;

import org.pekoo.osgi.springboot.utils.OAuthDetails;
import org.pekoo.osgi.springboot.utils.OAuthUserData;

/**
 * Witnigs OAuth 2.0 HTTP Consumer Interface
 * @author Petar Zhivkov
 */
public interface IHttpConsumer {

	Map<String, Object> getAccessToken(OAuthDetails oauthDetails, String authorizationCode) throws IOException;

	Map<String, Object> getProtectedResource(OAuthUserData userAuthData, String resourceUrl) throws IOException;

}
