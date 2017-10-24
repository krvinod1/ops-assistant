package com.intuit.appconnect.ops;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by sjaiswal on 9/28/17.
 */
public class OpsAssistantMetricsSpeechletNew implements Speechlet {
    private static final Logger log = LoggerFactory.getLogger(OpsAssistantMetricsSpeechletNew.class);


    private static final String SLOT_MODULE = "Module";
    private static final String SLOT_ENVIRONMENT = "Environment";
    private static final String SLOT_VERSION = "Version";
    private static final String SLOT_COUNT = "Count";

    private static final String SLOT_METRICS = "Metrics";
    private static final String SLOT_SERVER_TYPE = "ServerType";


    private static final String SESSION_MODULE = "module";
    private static final String SESSION_ENVIRONMENT = "environment";
    private static final String SESSION_VERSION = "version";
    private static final String SESSION_COUNT = "count";
    private static final String SESSION_METRICS = "metrics";
    private static final String SESSION_SERVER_TYPE = "servertype";



    static String ec2Namespace = "AWS/EC2";
    static String ec2NamespaceName = "InstanceId";
    static String ec2NamespaceValue ="i-03b9cb8f759d42cd5";
    static String rdsNamespace = "AWS/RDS";
    static String rdsNamespaceName = "DBInstanceIdentifier";
    static String rdsNamespaceValue ="itduzzit-qa";
    static String metricName = "CPUUtilization";


    private static final String HOST_URL = "https://stage.api.appconnect.intuit.com/api/v1/admin/stacks/";


    /**
     * This method gets called during session initialization.
     * @param request
     * @param session
     * @throws SpeechletException
     */
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any initialization logic goes here
    }

    /**
     * This method gets called during launch of the skill app.
     * @param request
     * @param session
     * @return
     * @throws SpeechletException
     */
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session) throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        return getWelcomeResponse();
    }

    /**
     * This method gets called on every intent request.
     * @param request
     * @param session
     * @return
     * @throws SpeechletException
     */
    public SpeechletResponse onIntent(final IntentRequest request, final Session session) throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        /*
            Business logic for handling intent requests go here.
            CASE 1: Application Deployment
            CASE 2: Server Status
            CASE 3: Database Status
         */
        Intent intent = request.getIntent();
        String intentName = intent.getName();
        log.info("Intent name: {}", intentName);
        if ("OneshotMetricsIntent".equals(intentName)) {
            return handleOneshotMetricsRequest(intent, session);
        } else if ("DialogMetricsIntent".equals(intentName)) {

            Slot metricSlot = intent.getSlot(SLOT_METRICS);
            Slot envSlot = intent.getSlot(SLOT_ENVIRONMENT);
            Slot serverTypeSlot = intent.getSlot(SLOT_SERVER_TYPE);

            if(metricSlot != null && metricSlot.getValue()!=null) {
                return handleMetricsDialogRequest(intent,session);
            } else if (envSlot != null && envSlot.getValue() != null) {
                return handleEnvMetricsDialogRequest(intent, session);
            } else if (serverTypeSlot != null && serverTypeSlot.getValue() != null) {
                return handleServerTypeMetricsDialogRequest(intent, session);
            } else {

            }


        } else if ("SupportedEnvironmentsIntent".equals(intentName)) {
            return handleSupportedEnvironmentRequest(intent, session);
        } else if ("SupportedMetricsIntent".equals(intentName)) {
            return handleSupportedMetricsRequest(intent, session);
        } else if ("AMAZON.HelpIntent".equals(intentName)) {
            return handleHelpRequest();
        } else if ("AMAZON.StopIntent".equals(intentName)) {
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText("Goodbye");

            return SpeechletResponse.newTellResponse(outputSpeech);
        } else if ("AMAZON.CancelIntent".equals(intentName)) {
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText("Goodbye");

            return SpeechletResponse.newTellResponse(outputSpeech);
        } else {
            throw new SpeechletException("Invalid Intent");
        }


        return null;
    }

    private SpeechletResponse handleMetricsDialogRequest(final Intent intent, final Session session) {

        String envSlotValue = null;
        String serverTypeValue = null;
        String metricsSlotValue = null;

        try {
            metricsSlotValue = getSlotValueFromIntent(intent, false, SLOT_METRICS);
        } catch (Exception e) {
            // invalid city. move to the dialog
            String speechOutput =
                    "Please try again, the metrics that can be currently reported are CPU usage and memory usage."
                            + "What metrics are you interested in?";

            // repromptText is the same as the speechOutput
            return newAskResponse(speechOutput, speechOutput);
        }

        if (session.getAttributes().containsKey(SESSION_SERVER_TYPE)) {
            serverTypeValue = (String) session.getAttribute(SESSION_SERVER_TYPE);
        } else {
            // set city in session and prompt for date
            session.setAttribute(SESSION_METRICS, metricsSlotValue);
            String speechOutput = "For what server type ?";
            String repromptText = "What server type: You can coose from API, UI, JOBS, CONNECTORS, DATABASE?";

            return newAskResponse(speechOutput, repromptText);
        }

        if (session.getAttributes().containsKey(SESSION_ENVIRONMENT)) {
            envSlotValue = (String) session.getAttribute(SESSION_ENVIRONMENT);
        } else {
            // set city in session and prompt for date
            session.setAttribute(SESSION_METRICS, metricsSlotValue);
            session.setAttribute(SESSION_SERVER_TYPE, envSlotValue);

            String speechOutput = "What environment?";
            String repromptText =
                    metricsSlotValue + " for " +serverTypeValue +" on what environment?";

            return newAskResponse(speechOutput, repromptText);
        }

        session.setAttribute(SESSION_METRICS, metricsSlotValue);

        Map<String, String> slotValueMap = getSlotValueFromSession(session);

        return getMetricsReponse(slotValueMap);
    }

    private Map<String,String> getSlotValueFromSession(Session session) {
        Map<String,String> valueMap = new HashMap<String, String>();
        valueMap.put(SLOT_METRICS,session.getAttribute(SESSION_METRICS).toString());
        valueMap.put(SLOT_ENVIRONMENT,session.getAttribute(SESSION_ENVIRONMENT).toString());
        valueMap.put(SLOT_SERVER_TYPE,session.getAttribute(SESSION_SERVER_TYPE).toString());
        return valueMap;
    }

    private SpeechletResponse handleEnvMetricsDialogRequest(final Intent intent, final Session session) {

        String envSlotValue = null;
        String serverTypeValue = null;
        String metricsSlotValue = null;

        try {
            envSlotValue = getSlotValueFromIntent(intent, false, SLOT_ENVIRONMENT);
        } catch (Exception e) {
            // invalid city. move to the dialog
            String speechOutput =
                    "Please try again, the metrics that can be currently reported for QA, STAGE and BETA environment."
                            + "What environment do you want the metrics for?";

            // repromptText is the same as the speechOutput
            return newAskResponse(speechOutput, speechOutput);
        }

        if (session.getAttributes().containsKey(SESSION_SERVER_TYPE)) {
            serverTypeValue = (String) session.getAttribute(SESSION_SERVER_TYPE);
        } else {
            // set city in session and prompt for date
            session.setAttribute(SESSION_ENVIRONMENT, envSlotValue);
            String speechOutput = "For what server type ?";
            String repromptText = "What server type: App Server or DB server?";

            return newAskResponse(speechOutput, repromptText);
        }

        if (session.getAttributes().containsKey(SESSION_METRICS)) {
            metricsSlotValue = (String) session.getAttribute(SESSION_METRICS);
        } else {
            // set city in session and prompt for date
            session.setAttribute(SESSION_ENVIRONMENT, envSlotValue);
            session.setAttribute(SESSION_SERVER_TYPE, serverTypeValue);

            String speechOutput = "The metrics that can be currently reported are CPU usage and memory usage."
                    + "What metrics are you interested in?";
            String repromptText =
                    metricsSlotValue + " for " +serverTypeValue +" on what environment?";

            return newAskResponse(speechOutput, repromptText);
        }


        session.setAttribute(SESSION_ENVIRONMENT, envSlotValue);

        Map<String, String> slotValueMap = getSlotValueFromSession(session);

        return getMetricsReponse(slotValueMap);
    }


    private SpeechletResponse handleServerTypeMetricsDialogRequest(final Intent intent, final Session session) {

        String envSlotValue = null;
        String serverTypeValue = null;
        String metricsSlotValue = null;

        try {
            serverTypeValue = getSlotValueFromIntent(intent, false, SLOT_SERVER_TYPE);
        } catch (Exception e) {
            // invalid city. move to the dialog
            String speechOutput =
                    "Please try again, currently supported server types are UI, API, JOBS, CONNECTOR, DATABASE."
                            + "What server type do you want the metrics for?";

            // repromptText is the same as the speechOutput
            return newAskResponse(speechOutput, speechOutput);
        }

        if (session.getAttributes().containsKey(SESSION_ENVIRONMENT)) {
            envSlotValue = (String) session.getAttribute(SESSION_ENVIRONMENT);
        } else {
            // set city in session and prompt for date
            session.setAttribute(SESSION_SERVER_TYPE, serverTypeValue);
            String speechOutput = "For what environment ?";
            String repromptText = "You would like to check the metrics for which environment?";

            return newAskResponse(speechOutput, repromptText);
        }

        if (session.getAttributes().containsKey(SESSION_METRICS)) {
            metricsSlotValue = (String) session.getAttribute(SESSION_METRICS);
        } else {
            // set city in session and prompt for date
            session.setAttribute(SESSION_ENVIRONMENT, envSlotValue);
            session.setAttribute(SESSION_SERVER_TYPE, serverTypeValue);

            String speechOutput = "The metrics that can be currently reported are CPU usage and memory usage."
                    + "What metrics are you interested in?";
            String repromptText =
                    metricsSlotValue + " for " +serverTypeValue +" on what environment?";

            return newAskResponse(speechOutput, repromptText);
        }


        session.setAttribute(SESSION_SERVER_TYPE, serverTypeValue);

        Map<String, String> slotValueMap = getSlotValueFromSession(session);

        return getMetricsReponse(slotValueMap);
    }

    private SpeechletResponse handleOneshotMetricsRequest(final Intent intent, final Session session) {
        // Determine city, using default if none provided
        String serverTypeSlotValue = null;
        String envSlotValue = null;
        String metricsSlotValue = null;
        try {
            serverTypeSlotValue = getSlotValueFromIntent(intent, false, SLOT_SERVER_TYPE);
            envSlotValue = getSlotValueFromIntent(intent,false,SLOT_ENVIRONMENT);
            metricsSlotValue = getSlotValueFromIntent(intent,false, SLOT_METRICS);

        } catch (Exception e) {
            // invalid city. move to the dialog
            String speechOutput =
                    "Please try again, the valid metrics are CPU usage and MEMORY usage for API, UI, JOBS, CONNECTORS and valid environments are QA, STAGE."
                            + "Which metrics would you like to check?";

            // repromptText is the same as the speechOutput
            return newAskResponse(speechOutput, speechOutput);
        }

        // all slots filled, either from the user or by default values. Move to final request

        Map<String, String> slotValueMap = new HashMap<String, String>();
        slotValueMap.put(SLOT_METRICS, metricsSlotValue);
        slotValueMap.put(SLOT_ENVIRONMENT, envSlotValue);
        slotValueMap.put(SLOT_SERVER_TYPE, serverTypeSlotValue);


        return getMetricsReponse(slotValueMap);
    }


    private SpeechletResponse getMetricsReponse (Map<String,String> slotValueMap) {
        String speechOutput = "";
        Set<Map.Entry<String,String>> slotValueEntrySet = slotValueMap.entrySet();
        Iterator<Map.Entry<String,String>> iterator = slotValueEntrySet.iterator();

        log.info(" inside getMetricsReponse(), Server Type : {}",slotValueMap.get(SLOT_SERVER_TYPE));
        String metricResponse = "";
        if (! slotValueMap.get(SLOT_SERVER_TYPE).equalsIgnoreCase("database")) {
            log.info(" inside no database");
            metricResponse = MetricsHelper.getStats(ec2Namespace, ec2NamespaceName, ec2NamespaceValue);
        } else {
            metricResponse = MetricsHelper.getStats(rdsNamespace, rdsNamespaceName, rdsNamespaceValue);
        }

        speechOutput = new StringBuilder("Metrics ")
                .append(slotValueMap.get(SLOT_METRICS))
                .append(" for ")
                .append(slotValueMap.get(SLOT_SERVER_TYPE))
                .append(" on ")
                .append(slotValueMap.get(SLOT_ENVIRONMENT))
                .append(" is: ")
                .append(metricResponse)
                .toString();
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Ops Metrics");
        card.setContent(speechOutput);

        // Create the plain text output
        PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
        outputSpeech.setText(speechOutput);

        return SpeechletResponse.newTellResponse(outputSpeech, card);
    }





    private String getSlotValueFromIntent (final Intent intent, final boolean assignDefault, String slotName) throws Exception {
        Slot slot = intent.getSlot(slotName);
        String slotValue = null;
        if(slot == null ||  slot.getValue() == null) {
            if (!assignDefault) {
                throw new Exception("");
            } else {
                if (slotName == SLOT_COUNT) {
                    slotValue = "1";
                }
            }
        } else {
            slotValue = slot.getValue();

        }
        return slotValue;
    }


    /**
     * This method gets called when session is ended.
     * @param request
     * @param session
     * @throws SpeechletException
     */

    public void onSessionEnded(final SessionEndedRequest request, final Session session) throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any cleanup logic goes here
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getWelcomeResponse() {
        String whatActionPrompt = "Do you want to deploy new stack or check metrics?";
        String speechOutput = "<speak>"
                + "Welcome to Ops Metrics. "
                + whatActionPrompt
                + "</speak>";
        String repromptText =
                "I can lead you through launch a stack for different modules "
                        + "and let you check server or database status, "
                        + "or you can simply open Ops Assistant and ask a question like, "
                        + "deploy api module on qa with version 3.0.90.0 "
                        + "For a list of supported modules, ask what modules are supported. "
                        + whatActionPrompt;

        return newAskResponse(speechOutput, true, repromptText, false);
    }

    /**
     * Wrapper for creating the Ask response from the input strings.
     *
     * @param stringOutput
     *            the output to be spoken
     * @param isOutputSsml
     *            whether the output text is of type SSML
     * @param repromptText
     *            the reprompt for if the user doesn't reply or is misunderstood.
     * @param isRepromptSsml
     *            whether the reprompt text is of type SSML
     * @return SpeechletResponse the speechlet response
     */
    private SpeechletResponse newAskResponse(String stringOutput, boolean isOutputSsml,
                                             String repromptText, boolean isRepromptSsml) {
        OutputSpeech outputSpeech, repromptOutputSpeech;
        if (isOutputSsml) {
            outputSpeech = new SsmlOutputSpeech();
            ((SsmlOutputSpeech) outputSpeech).setSsml(stringOutput);
        } else {
            outputSpeech = new PlainTextOutputSpeech();
            ((PlainTextOutputSpeech) outputSpeech).setText(stringOutput);
        }

        if (isRepromptSsml) {
            repromptOutputSpeech = new SsmlOutputSpeech();
            ((SsmlOutputSpeech) repromptOutputSpeech).setSsml(stringOutput);
        } else {
            repromptOutputSpeech = new PlainTextOutputSpeech();
            ((PlainTextOutputSpeech) repromptOutputSpeech).setText(repromptText);
        }

        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptOutputSpeech);
        return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
    }

    private SpeechletResponse handleHelpRequest() {
        String repromptText = "Do you want to deploy new stack or check status?";

        String speechOutput =
                "I can lead you through launch a stack for different modules "
                        + "and let you check server or database status, "
                        + "or you can simply open Ops Assistant and ask a question like, "
                        + "deploy api module on qa with version 3.0.90.0 "
                        + "For a list of supported modules, ask what modules are supported. "
                        + repromptText;
        return newAskResponse(speechOutput, repromptText);
    }


    /**
     * Wrapper for creating the Ask response from the input strings with
     * plain text output and reprompt speeches.
     *
     * @param stringOutput
     *            the output to be spoken
     * @param repromptText
     *            the reprompt for if the user doesn't reply or is misunderstood.
     * @return SpeechletResponse the speechlet response
     */
    private SpeechletResponse newAskResponse(String stringOutput, String repromptText) {
        return newAskResponse(stringOutput, false, repromptText, false);
    }

    /**
     * Handles the case where we need to know which city the user needs tide information for.
     */
    private SpeechletResponse handleSupportedEnvironmentRequest(final Intent intent,
                                                                final Session session) {
        // get city re-prompt
        String repromptText = "Which environment would you like to get the metrics for?";
        String speechOutput =
                "Currently, I support QA, STAGE, BETA environment for deployment: "
                        + repromptText;

        return newAskResponse(speechOutput, repromptText);
    }




    private SpeechletResponse handleSupportedMetricsRequest(final Intent intent,
                                                            final Session session) {
        // get city re-prompt
        String repromptText = "Which metrics would you like to check?";
        String speechOutput =
                "Currently, I support CPU usage and MEMORY usage: "
                        + repromptText;

        return newAskResponse(speechOutput, repromptText);
    }




    public static String convertToJson(Object source){
        Gson gson = new Gson();
        return gson.toJson(source);
    }


}
