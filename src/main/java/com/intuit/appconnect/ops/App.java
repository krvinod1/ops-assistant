package com.intuit.appconnect.ops;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception {
        System.out.println( "Hello World!" );

//        OpsAssistantSpeechlet opsAssistantSpeechlet = new OpsAssistantSpeechlet();
//        opsAssistantSpeechlet.handleDeployStackRequest(null);

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        String dateString = dateFormat.format(date);
        System.out.print(dateString);



// Use Madrid's time zone to format the date in



    }
}
