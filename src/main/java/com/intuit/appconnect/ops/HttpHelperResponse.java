package com.intuit.appconnect.ops;

/**
 * Created by sjaiswal on 9/28/17.
 */
public class HttpHelperResponse {

    private int httpStatus;
    private String response;

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
