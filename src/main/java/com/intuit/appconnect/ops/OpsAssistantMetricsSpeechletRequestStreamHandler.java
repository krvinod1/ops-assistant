package com.intuit.appconnect.ops;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by sjaiswal on 9/27/17.
 */
public class OpsAssistantMetricsSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {
    private static final Set<String> supportedApplicationIds = new HashSet<String>();
    static {
        /*
         * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit" the relevant
         * Alexa Skill and put the relevant Application Ids in this Set.
         */
        supportedApplicationIds.add("amzn1.ask.skill.02ae683c-ecb8-4053-8e10-84cbf8db4844");
        supportedApplicationIds.add("amzn1.ask.skill.58775e03-ee03-4b46-9a65-3d42b010c864");
    }

    public OpsAssistantMetricsSpeechletRequestStreamHandler() {
        super(new OpsAssistantMetricsSpeechlet(), supportedApplicationIds);
    }

}