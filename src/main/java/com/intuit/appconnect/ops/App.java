package com.intuit.appconnect.ops;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception {
        System.out.println( "Hello World!" );

        OpsAssistantSpeechlet opsAssistantSpeechlet = new OpsAssistantSpeechlet();
        opsAssistantSpeechlet.handleDeployStackRequest(null);

    }
}
