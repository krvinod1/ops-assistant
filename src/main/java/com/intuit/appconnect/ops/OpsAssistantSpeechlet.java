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
import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by sjaiswal on 9/27/17.
 */
public class OpsAssistantSpeechlet implements Speechlet {

    private static final Logger log = LoggerFactory.getLogger(OpsAssistantSpeechlet.class);


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
        if ("OneshotOpsIntent".equals(intentName)) {
            return handleOneshotOpsRequest(intent, session);
        } else if ("DialogOpsIntent".equals(intentName)) {
            // Determine if this turn is for city, for date, or an error.
            // We could be passed slots with values, no slots, slots with no value.
            Slot moduleSlot = intent.getSlot(SLOT_MODULE);
            Slot envSlot = intent.getSlot(SLOT_ENVIRONMENT);
            Slot versionSlot = intent.getSlot(SLOT_VERSION);
            Slot countSlot = intent.getSlot(SLOT_COUNT);

            if (moduleSlot != null && moduleSlot.getValue() != null) {
                return handleModuleDialogRequest(intent, session);
            } else if (envSlot != null && envSlot.getValue() != null) {
                return handleEnvDialogRequest(intent, session);
            } else if (versionSlot != null && versionSlot.getValue() != null) {
                return handleVersionDialogRequest(intent, session);
            } else if (countSlot != null && countSlot.getValue() != null) {
                return handleCountDialogRequest(intent, session);
            } else {
               // return handleNoSlotDialogRequest(intent, session);
            }

        } else if ("SupportedEnvironmentsIntent".equals(intentName)) {
            return handleSupportedEnvironmentRequest(intent, session);
        } else if ("SupportedModulesIntent".equals(intentName)) {
              return handleSupportedModulesRequest(intent, session);
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
            String repromptText = "What server type: App Server or DB server?";

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

        Map<String, String> slotValueMap = new HashMap<String, String>();
        slotValueMap.put(SLOT_ENVIRONMENT, envSlotValue);
        slotValueMap.put(SLOT_SERVER_TYPE, serverTypeValue);
        slotValueMap.put(SLOT_METRICS, metricsSlotValue);

        return getMetricsReponse(slotValueMap);
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

        Map<String, String> slotValueMap = new HashMap<String, String>();
        slotValueMap.put(SLOT_ENVIRONMENT, envSlotValue);
        slotValueMap.put(SLOT_SERVER_TYPE, serverTypeValue);
        slotValueMap.put(SLOT_METRICS, metricsSlotValue);

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

        Map<String, String> slotValueMap = new HashMap<String, String>();
        slotValueMap.put(SLOT_ENVIRONMENT, envSlotValue);
        slotValueMap.put(SLOT_SERVER_TYPE, serverTypeValue);
        slotValueMap.put(SLOT_METRICS, metricsSlotValue);

        return getMetricsReponse(slotValueMap);
    }








    private SpeechletResponse handleModuleDialogRequest(final Intent intent, final Session session) {
        String moduleSlotValue = null;
        String targetEnv = null;
        String version = null;

        try {
            moduleSlotValue = getSlotValueFromIntent(intent, false, SLOT_MODULE);
        } catch (Exception e) {
            // invalid city. move to the dialog
            String speechOutput =
                    "Please try again, the valid modules are API, UI, JOBS, CONNECTOR."
                            + "Which module would you like to deploy?";

            // repromptText is the same as the speechOutput
            return newAskResponse(speechOutput, speechOutput);
        }

        if (session.getAttributes().containsKey(SESSION_ENVIRONMENT)) {
             targetEnv = (String) session.getAttribute(SESSION_ENVIRONMENT);


        } else {
            // set city in session and prompt for date
            session.setAttribute(SESSION_MODULE, moduleSlotValue);
            String speechOutput = "For which environment?";
            String repromptText =
                    "For which environment would you like to deploy "+moduleSlotValue +" module ?";

            return newAskResponse(speechOutput, repromptText);
        }

        if (session.getAttributes().containsKey(SESSION_VERSION)) {
            version = (String) session.getAttribute(SESSION_VERSION);


        } else {
            // set city in session and prompt for date
            session.setAttribute(SESSION_MODULE, moduleSlotValue);
            session.setAttribute(SESSION_ENVIRONMENT, targetEnv);
            String speechOutput = "Which artifact version?";
            String repromptText =
                    "Which artifact version would you like to deploy "+moduleSlotValue +" module on "+targetEnv+" environment?";

            return newAskResponse(speechOutput, repromptText);
        }

        session.setAttribute(SESSION_MODULE,moduleSlotValue);

        Map<String, String> slotValueMap = getSlotValueFromSession(session);

        return getDeploymentReponse(slotValueMap);
    }


    private SpeechletResponse handleEnvDialogRequest(final Intent intent, final Session session) {
        String moduleSlotValue = null;
        String envSlotValue = null;
        String versionSlotValue = null;
        String countSlotValue = null;

        try {
            envSlotValue = getSlotValueFromIntent(intent, false, SLOT_ENVIRONMENT);
        } catch (Exception e) {
            // invalid city. move to the dialog
            String speechOutput =
                    "Please try again, the valid environments are QA, STAGE, BETA."
                            + "Which environment would you like to deploy?";

            // repromptText is the same as the speechOutput
            return newAskResponse(speechOutput, speechOutput);
        }

        if (session.getAttributes().containsKey(SESSION_MODULE)) {
            moduleSlotValue = (String) session.getAttribute(SESSION_MODULE);
        } else {
            // set city in session and prompt for date
            session.setAttribute(SESSION_ENVIRONMENT, envSlotValue);
            String speechOutput = "Which module?";
            String repromptText =
                    "Which module would you like to deploy on "+envSlotValue +" environment ?";

            return newAskResponse(speechOutput, repromptText);
        }

        if (session.getAttributes().containsKey(SESSION_VERSION)) {
            versionSlotValue = (String) session.getAttribute(SESSION_VERSION);


        } else {
            // set city in session and prompt for date
            session.setAttribute(SESSION_MODULE, moduleSlotValue);
            session.setAttribute(SESSION_ENVIRONMENT, envSlotValue);
            String speechOutput = "Which artifact version?";
            String repromptText =
                    "Which artifact version would you like to deploy "+moduleSlotValue +" module on "+envSlotValue+" environment?";

            return newAskResponse(speechOutput, repromptText);
        }

        session.setAttribute(SESSION_ENVIRONMENT,envSlotValue);
        Map<String, String> slotValueMap = getSlotValueFromSession(session);

        return getDeploymentReponse(slotValueMap);

    }

    private SpeechletResponse handleVersionDialogRequest(final Intent intent, final Session session) {
        String moduleSlotValue = null;
        String envSlotValue = null;
        String versionSlotValue = null;
        String countSlotValue = null;

        try {
            versionSlotValue = getSlotValueFromIntent(intent, false, SLOT_VERSION);
        } catch (Exception e) {
            // invalid city. move to the dialog
            String speechOutput =
                    "Please try again, the valid versions are 3.0.89.0, 3.0.90.0"
                            + "Which version would you like to deploy?";

            // repromptText is the same as the speechOutput
            return newAskResponse(speechOutput, speechOutput);
        }

        if (session.getAttributes().containsKey(SESSION_MODULE)) {
            moduleSlotValue = (String) session.getAttribute(SESSION_MODULE);
        } else {
            // set city in session and prompt for date
            session.setAttribute(SESSION_VERSION, versionSlotValue);
            String speechOutput = "Which module?";
            String repromptText =
                    "Which module would you like to deploy with artifact version "+versionSlotValue + "?";

            return newAskResponse(speechOutput, repromptText);
        }

        if (session.getAttributes().containsKey(SESSION_ENVIRONMENT)) {
            envSlotValue = (String) session.getAttribute(SESSION_ENVIRONMENT);


        } else {
            // set city in session and prompt for date
            session.setAttribute(SESSION_MODULE, moduleSlotValue);
            session.setAttribute(SESSION_VERSION, versionSlotValue);
            String speechOutput = "For which environment?";
            String repromptText =
                    "For which environment would you like to deploy "+moduleSlotValue +" module ?";
            return newAskResponse(speechOutput, repromptText);
        }

        session.setAttribute(SESSION_VERSION,versionSlotValue);
        Map<String, String> slotValueMap = getSlotValueFromSession(session);


        return getDeploymentReponse(slotValueMap);

    }

    private Map<String, String> getSlotValueFromSession (Session session) {
        Map<String,String> slotValueMap = new HashMap<String, String>();
        String slotValue = session.getAttribute(SESSION_MODULE).toString();
        String envValue = session.getAttribute(SESSION_ENVIRONMENT).toString();
        String versionValue = session.getAttribute(SESSION_VERSION).toString();
        log.info("SlotValue = {}, envValue = {}, versionValue = {}",slotValue,envValue,versionValue);
        slotValueMap.put(SLOT_MODULE,slotValue);
        slotValueMap.put(SLOT_ENVIRONMENT,envValue);
        slotValueMap.put(SLOT_VERSION,versionValue);
        slotValueMap.put(SLOT_COUNT, "1");
        return slotValueMap;
    }

    private SpeechletResponse handleCountDialogRequest(final Intent intent, final Session session) {
        return null;

    }

    /**
     * This handles the one-shot interaction, where the user utters a phrase like: 'Alexa, open Tide
     * Pooler and get tide information for Seattle on Saturday'. If there is an error in a slot,
     * this will guide the user to the dialog approach.
     */
    private SpeechletResponse handleOneshotOpsRequest(final Intent intent, final Session session) {
        // Determine city, using default if none provided
        String moduleSlotValue = null;
        String envSlotValue = null;
        String versionSlotValue = null;
        String countSlotValue = null;
        try {
            moduleSlotValue = getSlotValueFromIntent(intent, false, SLOT_MODULE);
            envSlotValue = getSlotValueFromIntent(intent,false,SLOT_ENVIRONMENT);
            versionSlotValue = getSlotValueFromIntent(intent,false, SLOT_VERSION);
            countSlotValue = getSlotValueFromIntent(intent,true,SLOT_COUNT);

        } catch (Exception e) {
            // invalid city. move to the dialog
            String speechOutput =
                    "Please try again, the valid modules are API, UI, JOBS, CONNECTOR and valid environments are QA, STAGE, BETA."
                            + "Which module would you like to deploy?";

            // repromptText is the same as the speechOutput
            return newAskResponse(speechOutput, speechOutput);
        }

        // all slots filled, either from the user or by default values. Move to final request

        Map<String, String> slotValueMap = new HashMap<String, String>();
        slotValueMap.put(SLOT_MODULE, moduleSlotValue);
        slotValueMap.put(SLOT_ENVIRONMENT, envSlotValue);
        slotValueMap.put(SLOT_VERSION, versionSlotValue);
        slotValueMap.put(SLOT_COUNT, countSlotValue);

        return getDeploymentReponse(slotValueMap);
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
        speechOutput = new StringBuilder("Metrics ")
                .append(slotValueMap.get(SLOT_METRICS))
                .append(" for ")
                .append(slotValueMap.get(SLOT_SERVER_TYPE))
                .append(" on ")
                .append(slotValueMap.get(SLOT_ENVIRONMENT))
                .append(" will be available soon. ")
                .toString();

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Ops Assistant");
        card.setContent(speechOutput);

        // Create the plain text output
        PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
        outputSpeech.setText(speechOutput);

        return SpeechletResponse.newTellResponse(outputSpeech, card);
    }



    private SpeechletResponse getDeploymentReponse (Map<String,String> slotValueMap) {
        String speechOutput = "";
        Set<Map.Entry<String,String>> slotValueEntrySet = slotValueMap.entrySet();
        Iterator<Map.Entry<String,String>> iterator = slotValueEntrySet.iterator();
        speechOutput = new StringBuilder("Deploying")
                        .append(slotValueMap.get(SLOT_MODULE))
                        .append(" module on ")
                        .append(slotValueMap.get(SLOT_ENVIRONMENT))
                        .append(" with version ")
                        .append(slotValueMap.get(SLOT_VERSION))
                        .append(" and count ")
                        .append(slotValueMap.get(SLOT_COUNT))
                        .toString();

        try {
            log.info("Deploying...");
           // handleDeployStackRequest(slotValueMap);
        } catch (Exception e) {
            log.error("Error while deploying stack..");
            speechOutput = "Error while deploying the stack, please try again.";
        }

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Ops Assistant");
        card.setContent(speechOutput);

        // Create the plain text output
        PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
        outputSpeech.setText(speechOutput);
        log.info("Executed the stack creation request..");
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
        String whatActionPrompt = "Do you want to deploy new stack or check status?";
        String speechOutput = "<speak>"
                + "Welcome to Ops Assistant. "
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
        String repromptText = "Which environment would you like to deploy to?";
        String speechOutput =
                "Currently, I support QA, STAGE, BETA environment for deployment: "
                       + repromptText;

        return newAskResponse(speechOutput, repromptText);
    }

    /**
     * Handles the case where we need to know which city the user needs tide information for.
     */
    private SpeechletResponse handleSupportedModulesRequest(final Intent intent,
                                                                final Session session) {
        // get city re-prompt
        String repromptText = "Which module would you like to deploy to?";
        String speechOutput =
                "Currently, I support UI, API, JOBS, CONNECTOR modules for deployment: "
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

    public String handleDeployStackRequest(Map<String, String> stackValueMap) throws Exception{

        DeployStackRequest deployStackRequest = new DeployStackRequest();
        deployStackRequest.setStackType("api");//stackValueMap.get(//SLOT_MODULE.toLowerCase()));
        deployStackRequest.setStackEnv("qa");//stackValueMap.get(//SLOT_ENVIRONMENT.toLowerCase()));
        deployStackRequest.setStackVersion("3.0.89.0-SNAPSHOT");//stackValueMap.get(SLOT_VERSION+"-SNAPSHOT"));
        deployStackRequest.setStackCapacity("Single");
        deployStackRequest.setZone("us-west-2a");

        if (SLOT_ENVIRONMENT.equalsIgnoreCase("qa")) {
            deployStackRequest.setInstanceTag("develop");
        } else {
            deployStackRequest.setInstanceTag("release");
        }
        HttpPost httpPostReq = HttpRequestHelper.createHttpPostRequest(HOST_URL);

        String authHeader = HttpRequestHelper.getPrivateAuthHeader(HttpRequestHelper.getAuthHeaderMap());
        String stackRequestJson = convertToJson(deployStackRequest);
        log.info("Stack deployment request = {}",stackRequestJson);

        httpPostReq.setHeader("Authorization",authHeader);
        try {
            httpPostReq.setHeader("intuit_originatingip", InetAddress.getLocalHost().getHostAddress());
            HttpHelperResponse response = HttpRequestHelper.doPostRequest(httpPostReq, stackRequestJson);
            if(response!=null)
            {
                if (response.getHttpStatus() != 200) {
                    return "Error while deploying the stack, please try again.";
                }
            }

        } catch (UnknownHostException e) {
            log.error("Error occurred. " + e.getMessage(), e);
            throw new SpeechletException("Error while deploying the stack, please try again.");
        } catch (Exception e) {
            log.error("Error occurred. " + e.getMessage(), e);
            throw new SpeechletException("Error while deploying the stack, please try again.");
        }
        log.info("Stack deployed successfully...");
        return "Stack deployed successfully.";
    }


    public static String convertToJson(Object source){
        Gson gson = new Gson();
        return gson.toJson(source);
    }




}
