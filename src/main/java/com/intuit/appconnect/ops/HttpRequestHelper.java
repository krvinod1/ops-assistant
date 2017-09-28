package com.intuit.appconnect.ops;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by sjaiswal on 9/28/17.
 */
public class HttpRequestHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestHelper.class);

    private static final String URL_ENCODING_FORMAT = "UTF-8";
    private static final String CONTENT_TYPE = "application/json";
    private static final String HEADER_SEPARATOR = ",";
    private static final String URL_PATH_SEPARATOR = "/";
    private static final String INTUIT_HEADER_APP_ID = "intuit_appid";
    private static final String INTUIT_HEADER_APP_SECRET = "intuit_app_secret";
    private static final String INTUIT_HEADER_TOKEN_TYPE= "intuit_token_type";
    private static final String INTUIT_TOKEN = "x_intuit_ticket";
    private static final String INTUIT_AUTH_ID = "x_intuit_authid";
    private static final String INTUIT_VERSION = "intuit_version";
    private static final String INTUIT_AUTH_TYPE = "INTUIT_IAM ";


    public static HttpPost createHttpPostRequest(String url){
        HttpPost httpPostRequest = new HttpPost(url);
        httpPostRequest.addHeader("Content-Type",CONTENT_TYPE);
        httpPostRequest.setHeader("Accept", "application/json");
        return httpPostRequest;
    }

    public static HttpGet createHttpGetRequest(String url){
        HttpGet httpGetRequest = new HttpGet(url);
        httpGetRequest.addHeader("Content-Type", CONTENT_TYPE);
        httpGetRequest.setHeader("Accept", CONTENT_TYPE);
        return httpGetRequest;
    }

    public static String createNameValuePair(String name, String value){
        List<NameValuePair> queryParams = new ArrayList<NameValuePair>();
        queryParams.add(new BasicNameValuePair(name,value));
        return URLEncodedUtils.format(queryParams, URL_ENCODING_FORMAT);  //$NON-NLS-L$
    }

    protected static HttpClient getHttpClient(){
        return HttpClients.createDefault();
    }

    public static HttpHelperResponse doPostRequest(HttpPost httpPost, String payload){
        LOGGER.info("Inside doPostRequest()");
        String response = null;
        HttpHelperResponse helperResponse = null;
        if(httpPost!=null){

            HttpClient httpClient = getHttpClient();
            HttpResponse httpResponse = null;

            try {
                if(!StringUtils.isEmpty(payload)){
                    StringEntity requestBody = new StringEntity(payload);
                    httpPost.setEntity(requestBody);
                }
                httpResponse = httpClient.execute(httpPost);
                if(httpResponse!=null){
                    // Get the status code
                    int statusCode = httpResponse.getStatusLine().getStatusCode();
                    if(statusCode!=200){
                        throw new RuntimeException("Invalid response from api. Http status: "+statusCode);
                    }

                    HttpEntity httpEntity = httpResponse.getEntity();
                    response = EntityUtils.toString(httpEntity, URL_ENCODING_FORMAT);
                    helperResponse = new HttpHelperResponse();
                    helperResponse.setHttpStatus(statusCode);
                    helperResponse.setResponse(response);
                }


            } catch (UnsupportedEncodingException e) {
                LOGGER.error("Error occurred while encoding the request. "+e.getMessage());
                throw new RuntimeException("Error while encoding the http request",e);

            } catch (ClientProtocolException e) {
                LOGGER.error("Error occurred while making request. "+e.getMessage());
                throw new RuntimeException("Error occurred while making request",e);

            } catch (IOException e) {
                LOGGER.error("Error occurred while making request. "+e.getMessage());
                throw new RuntimeException("Error occurred while making request",e);

            }
        }

        return helperResponse;
    }

    public static String doGetRequest(HttpGet httpGet){

        String response = null;
        if(httpGet!=null){

            HttpClient httpClient = getHttpClient();
            HttpResponse httpResponse = null;

            try {
//                if(!StringUtils.isEmpty(payload)){
//                    StringEntity requestBody = new StringEntity(payload);
//                    httpGet.setEntity(requestBody);
//                }
                httpResponse = httpClient.execute(httpGet);
                if(httpResponse!=null){
                    // Get the status code
                    int statusCode = httpResponse.getStatusLine().getStatusCode();
                    if(statusCode!=200){
                        throw new RuntimeException("Invalid response from api. Http status: "+statusCode);
                    }

                    HttpEntity httpEntity = httpResponse.getEntity();
                    response = EntityUtils.toString(httpEntity, URL_ENCODING_FORMAT);
                }


            } catch (UnsupportedEncodingException e) {
                LOGGER.error("Error occurred while encoding the request. "+e.getMessage());
                throw new RuntimeException("Error while encoding the http request",e);

            } catch (ClientProtocolException e) {
                LOGGER.error("Error occurred while making request. "+e.getMessage());
                throw new RuntimeException("Error occurred while making request",e);

            } catch (IOException e) {
                LOGGER.error("Error occurred while making request. "+e.getMessage());
                throw new RuntimeException("Error occurred while making request",e);

            }
        }

        return response;
    }

    //INTUIT_IAM x_intuit_authid=123145816016672,x_intuit_ticket=fhdfjdsjfjlj
    public static String getPrivateAuthHeader(Map<String,String> headerValueMap){

        String authHeader = null;

        if(headerValueMap!=null && headerValueMap.size() >0){
            StringBuffer headerBuffer = new StringBuffer(INTUIT_AUTH_TYPE);
            Set<Map.Entry<String,String>> entrySet = headerValueMap.entrySet();
            for(Map.Entry <String,String> entry : entrySet){
                headerBuffer.append(createNameValuePair(entry.getKey(),entry.getValue()));
                headerBuffer.append(HEADER_SEPARATOR);
            }
            authHeader = headerBuffer.toString();
        }
        LOGGER.info("Request header: {}",authHeader.toString());
        return authHeader;

    }

    public static Map<String,String> getAuthHeaderMap(){

        Map<String,String> headerMap = new HashMap();
        headerMap.put(INTUIT_AUTH_ID,"123145816016672");
        headerMap.put(INTUIT_TOKEN, "V1-246-b375kaxhs2u87rzqjlgrlh");
        return headerMap;
    }



}
