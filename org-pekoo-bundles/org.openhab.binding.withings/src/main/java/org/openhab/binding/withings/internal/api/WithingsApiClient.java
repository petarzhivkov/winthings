package org.openhab.binding.withings.internal.api;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.openhab.binding.withings.internal.model.Attribute;
import org.openhab.binding.withings.internal.model.Category;
import org.openhab.binding.withings.internal.model.MeasureGroup;
import org.openhab.binding.withings.internal.model.MeasureResult;
import org.openhab.binding.withings.internal.model.MeasureType;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class WithingsApiClient {
	
	private static final String API_ENDPOINT_MEASURE = "measure";

    private static final String API_METHOD_GET_MEASURES = "getmeas";
    
    private Gson gson;
    
    private JsonParser jsonParser;
    
    private String accountId;

	public String getAccountId() {
		return accountId;
	}

	private String proxyUrl;
    
	public WithingsApiClient(String userId, String accountId, String proxyUrl) {
        this.accountId = accountId;
        this.gson = createGsonBuilder().create();
        this.jsonParser = new JsonParser();
        this.proxyUrl = proxyUrl;
    }
    
    public List<MeasureGroup> getMeasures(int startTime) throws WithingsConnectionException {
    	 String url = getServiceUrl(API_ENDPOINT_MEASURE, API_METHOD_GET_MEASURES);
         if (startTime > 0) {
             url = url + "?startdate=" + startTime;
         }
         try {
        	 System.out.println("WithingsApiClient->about to call proxy:" + url);
             JsonObject jsonObject = callProxy(url);

             int status = jsonObject.get("status").getAsInt();

             if (status == 0) {
                 JsonElement body = jsonObject.get("body");
                 return gson.fromJson(body.getAsJsonObject(), MeasureResult.class).measureGroups;
             } else {
                 throw new WithingsConnectionException("Withings API call failed: " + status);
             }

         } catch (Exception ex) {
             throw new WithingsConnectionException("Could not connect to URL: " + ex.getMessage(), ex);
         }
    }
    
    public List<MeasureGroup> getMeasures() throws WithingsConnectionException {
        return getMeasures(0);
    }
    
    private JsonObject callProxy(String signedUrl)  throws IOException, MalformedURLException, WithingsConnectionException, UnsupportedEncodingException {

        HttpURLConnection httpURLConnection;
        httpURLConnection = (HttpURLConnection) new URL(signedUrl).openConnection();
        httpURLConnection.connect();

        int responseCode = httpURLConnection.getResponseCode();

        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new WithingsConnectionException("Illegal response code: " + responseCode);
        }

        Reader reader = null;
        try {
            InputStream inputStream = httpURLConnection.getInputStream();
            reader = new InputStreamReader(inputStream, "UTF-8");
			JsonObject jsonObject = (JsonObject) jsonParser.parse(reader);
            return jsonObject;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {

                }
            }
        }

    }
    
    
    private GsonBuilder createGsonBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(MeasureType.class, new JsonDeserializers.MeasureTypeJsonDeserializer());
        gsonBuilder.registerTypeAdapter(Category.class, new JsonDeserializers.CategoryJsonDeserializer());
        gsonBuilder.registerTypeAdapter(Attribute.class, new JsonDeserializers.AttributeJsonDeserializer());
        return gsonBuilder;
    }

    private String getServiceUrl(String endpoint, String method) {
    	return proxyUrl + endpoint + "/" + accountId + "/" +  method;
    }

}
