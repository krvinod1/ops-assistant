package com.intuit.appconnect.ops;

/**
 * Created by sjaiswal on 9/27/17.
 */

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * This class could be the handler for an AWS Lambda function powering an Alexa Skills Kit
 * experience. To do this, simply set the handler field in the AWS Lambda console to
 * "helloworld.HelloWorldSpeechletRequestStreamHandler" For this to work, you'll also need to build
 * this project using the {@code lambda-compile} Ant task and upload the resulting zip file to power
 * your function.
 */
public final class HelloWorldSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {
    private static final Set<String> supportedApplicationIds = new HashSet<String>();
    static {
        /*
         * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit" the relevant
         * Alexa Skill and put the relevant Application Ids in this Set.
         */
        supportedApplicationIds.add("amzn1.ask.skill.ec3c1cb4-9409-46aa-bc68-837698caab7a");
    }

    public HelloWorldSpeechletRequestStreamHandler() {
        super(new HelloWorldSpeechlet(), supportedApplicationIds);
    }
}
