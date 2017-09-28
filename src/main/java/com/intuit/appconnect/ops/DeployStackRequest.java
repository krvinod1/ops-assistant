package com.intuit.appconnect.ops;

/**
 * Created by sjaiswal on 9/28/17.
 */
public class DeployStackRequest {

    private String stackVersion;
    private String stackType;
    private String stackEnv;
    private String stackCapacity;
    private String zone;
    private String instanceTag;

    public DeployStackRequest(){}

    public DeployStackRequest(String stackVersion, String stackType, String stackEnv, String stackCapacity, String
            zone, String instanceTag) {
        this.stackVersion = stackVersion;
        this.stackType = stackType;
        this.stackEnv = stackEnv;
        this.stackCapacity = stackCapacity;
        this.zone = zone;
        this.instanceTag = instanceTag;
    }

    public String getStackVersion() {
        return stackVersion;
    }

    public void setStackVersion(String stackVersion) {
        this.stackVersion = stackVersion;
    }

    public String getStackType() {
        return stackType;
    }

    public void setStackType(String stackType) {
        this.stackType = stackType;
    }

    public String getStackEnv() {
        return stackEnv;
    }

    public void setStackEnv(String stackEnv) {
        this.stackEnv = stackEnv;
    }

    public String getStackCapacity() {
        return stackCapacity;
    }

    public void setStackCapacity(String stackCapacity) {
        this.stackCapacity = stackCapacity;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getInstanceTag() {
        return instanceTag;
    }

    public void setInstanceTag(String instanceTag) {
        this.instanceTag = instanceTag;
    }
}
