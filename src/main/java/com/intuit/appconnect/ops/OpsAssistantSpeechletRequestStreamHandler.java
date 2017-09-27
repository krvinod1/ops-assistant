package com.intuit.appconnect.ops;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by sjaiswal on 9/27/17.
 */
public class OpsAssistantSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {
    private static final Set<String> supportedApplicationIds = new HashSet<String>();
    static {
        /*
         * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit" the relevant
         * Alexa Skill and put the relevant Application Ids in this Set.
         */
        supportedApplicationIds.add("amzn1.ask.skill.ec3c1cb4-9409-46aa-bc68-837698caab7a");
    }

    public OpsAssistantSpeechletRequestStreamHandler() {
        super(new OpsAssistantSpeechlet(), supportedApplicationIds);
    }
}