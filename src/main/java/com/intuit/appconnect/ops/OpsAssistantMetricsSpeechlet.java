package com.intuit.appconnect.ops;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by sjaiswal on 9/27/17.
 */
public class OpsAssistantMetricsSpeechlet implements Speechlet {

    private static final Logger log = LoggerFactory.getLogger(OpsAssistantMetricsSpeechlet.class);


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

        if ("OneshotOpsMetricsIntent".equals(intentName)) {
            return handleOneshotOpsMetricsIntent(intent, session);
        } else if ("DialogOpsMetricsIntent".equals(intentName)) {
            return handleDialogOpsMetricsIntent(intent, session);
        } else if ("ReportedMetricsIntent".equals(intentName)) {
            return handleReportedMetricsRequest(intent, session);
        } else if ("SupportedEnvironmentsIntent".equals(intentName)) {
            return handleSupportedEnvironmentRequest(intent, session);
        } else if ("SupportedModulesIntent".equals(intentName)) {
              return handleSupportedModulesRequest(intent, session);
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

    }

    private SpeechletResponse getMetricsReponse (Map<String,String> slotValueMap) {
        String speechOutput = "";
        Set<Map.Entry<String,String>> slotValueEntrySet = slotValueMap.entrySet();
        Iterator<Map.Entry<String,String>> iterator = slotValueEntrySet.iterator();
        speechOutput = new StringBuilder()
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
        String whatActionPrompt = "Do you want to deploy new stack or know metrics?";
        String speechOutput = "<speak>"
                + "Welcome to Ops Assistant. "
                + whatActionPrompt
                + "</speak>";
        String repromptText =
                "I can lead you through launch a stack for different modules "
                        + "and let you check CPU and memory utilization for server or database, "
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

    private SpeechletResponse handleReportedMetricsRequest(final Intent intent,
                                                                final Session session) {
        String repromptText = "What metrics would you like to know for an environment?";
        String speechOutput =
                "Currently, I report CPU usage and memory usage: "
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

    private SpeechletResponse handleOneshotOpsMetricsIntent(final Intent intent, final Session session) {

        String moduleSlotValue = null;
        String envSlotValue = null;
        String serverTypeValue = null;
        String metricsSlotValue = null;
         try {
            envSlotValue = getSlotValueFromIntent(intent,false,SLOT_ENVIRONMENT);
            metricsSlotValue = getSlotValueFromIntent(intent,false, SLOT_METRICS);
            serverTypeValue = getSlotValueFromIntent(intent,false, SLOT_SERVER_TYPE);
            if(!serverTypeValue.equalsIgnoreCase("dbserver")){
                moduleSlotValue = getSlotValueFromIntent(intent,false, SLOT_MODULE);
            }

        } catch (Exception e) {
            // invalid city. move to the dialog
            String speechOutput =
                    "Please try again, the valid metrics are CPU usage and memory usage and valid environments are QA, STAGE."
                            + "What metrics would you like to know?";

            // repromptText is the same as the speechOutput
            return newAskResponse(speechOutput, speechOutput);
        }

        // all slots filled, either from the user or by default values. Move to final request

        Map<String, String> slotValueMap = new HashMap<String, String>();
        slotValueMap.put(SLOT_ENVIRONMENT, envSlotValue);
        slotValueMap.put(SLOT_METRICS, metricsSlotValue);
        slotValueMap.put(SLOT_SERVER_TYPE, serverTypeValue);
        slotValueMap.put(SLOT_MODULE, moduleSlotValue);
        return getMetricsReponse(slotValueMap);
    }

    private SpeechletResponse handleDialogOpsMetricsIntent(final Intent intent, final Session session) {

        Slot moduleSlot = intent.getSlot(SLOT_MODULE);
        Slot metricsSlotValue = intent.getSlot(SLOT_METRICS);
        Slot envSlot = intent.getSlot(SLOT_ENVIRONMENT);
        Slot serverType = intent.getSlot(SLOT_SERVER_TYPE);

        if (envSlot != null && envSlot.getValue() != null) {
            return handleEnvMetricsDialogRequest(intent, session);
        } else if (serverType != null && serverType.getValue() != null) {
            return handleServerTypeDialogRequest(intent, session);
        } else if (moduleSlot != null && moduleSlot.getValue() != null) {
            return handleModuleMetricsDialogRequest(intent, session);
        } else if (metricsSlotValue != null && metricsSlotValue.getValue() != null) {
            return handleMetricsDialogRequest(intent, session);
        }
        return handleMetricsDialogRequest(intent, session);
    }

    private SpeechletResponse handleModuleMetricsDialogRequest(final Intent intent, final Session session) {
        String moduleSlotValue = null;
        String envSlotValue = null;
        String serverTypeValue = null;
        String metricsSlotValue = null;
        try {
            moduleSlotValue = getSlotValueFromIntent(intent, false, SLOT_MODULE);
        } catch (Exception e) {

            String speechOutput =
                    "Please try again, the valid modules are UI API JOBS."
                            + "For what module would you like the metrics for?";

            // repromptText is the same as the speechOutput
            return newAskResponse(speechOutput, speechOutput);
        }
        if (session.getAttributes().containsKey(SESSION_ENVIRONMENT)) {
            envSlotValue = (String) session.getAttribute(SESSION_ENVIRONMENT);
        } else {
            // set city in session and prompt for date
            session.setAttribute(SESSION_MODULE, moduleSlotValue);
            String speechOutput = "What server: application or database";

            return newAskResponse(speechOutput, speechOutput);
        }

        if (session.getAttributes().containsKey(SESSION_METRICS)) {
            metricsSlotValue = (String) session.getAttribute(SESSION_METRICS);
        } else {
            // set city in session and prompt for date
            session.setAttribute(SESSION_ENVIRONMENT, envSlotValue);
            session.setAttribute(SESSION_MODULE, moduleSlotValue);
            String speechOutput = "What metrics? You can ask for CPU usage or memory usage.";
            return newAskResponse(speechOutput, speechOutput);
        }

        if (session.getAttributes().containsKey(SESSION_SERVER_TYPE)) {
            metricsSlotValue = (String) session.getAttribute(SESSION_SERVER_TYPE);
        } else {
            // set city in session and prompt for date
            session.setAttribute(SESSION_METRICS, metricsSlotValue);
            session.setAttribute(SESSION_ENVIRONMENT, envSlotValue);
            session.setAttribute(SESSION_MODULE, moduleSlotValue);
            String speechOutput = "What server type?";
            String repromptText =
                    metricsSlotValue + " for what server type on "+envSlotValue+" environment?";

            return newAskResponse(speechOutput, repromptText);
        }

        Map<String, String> slotValueMap = new HashMap<String, String>();
        slotValueMap.put(SLOT_MODULE, moduleSlotValue);
        slotValueMap.put(SLOT_ENVIRONMENT, envSlotValue);
        slotValueMap.put(SLOT_SERVER_TYPE, serverTypeValue);
        slotValueMap.put(SLOT_METRICS, metricsSlotValue);

        return getMetricsReponse(slotValueMap);
    }

    private SpeechletResponse handleServerTypeDialogRequest(final Intent intent, final Session session) {
        String moduleSlotValue = null;
        String envSlotValue = null;
        String serverTypeValue = null;
        String metricsSlotValue = null;
        try {
            serverTypeValue= getSlotValueFromIntent(intent, false, SLOT_SERVER_TYPE);
        } catch (Exception e) {

            String speechOutput =
                    "Please try again, the valid server types are App server and DB server"
                            + "Which type of server would you like to know the metrics for?";

            // repromptText is the same as the speechOutput
            return newAskResponse(speechOutput, speechOutput);
        }
        if (session.getAttributes().containsKey(SESSION_ENVIRONMENT)) {
            envSlotValue = (String) session.getAttribute(SESSION_ENVIRONMENT);
        } else {
            // set city in session and prompt for date
            session.setAttribute(SESSION_SERVER_TYPE, serverTypeValue);
            String speechOutput = "Which environment would you like to know the metrics for?";
            String repromptText =
                    "For what environment would you like to know the metrics for on "+serverTypeValue +" ?";

            return newAskResponse(speechOutput, repromptText);
        }

        if (session.getAttributes().containsKey(SESSION_MODULE)) {
            moduleSlotValue = (String) session.getAttribute(SESSION_MODULE);
        } else {
            // set city in session and prompt for date
            session.setAttribute(SESSION_SERVER_TYPE, serverTypeValue);
            session.setAttribute(SESSION_ENVIRONMENT, envSlotValue);
            String speechOutput = "For which module?";
            String repromptText =
                    "Which module in "+serverTypeValue +" on "+envSlotValue+" environment?";

            return newAskResponse(speechOutput, repromptText);
        }

        if (session.getAttributes().containsKey(SESSION_METRICS)) {
            metricsSlotValue = (String) session.getAttribute(SESSION_METRICS);
        } else {
            // set city in session and prompt for date
            session.setAttribute(SESSION_SERVER_TYPE, serverTypeValue);
            session.setAttribute(SESSION_ENVIRONMENT, envSlotValue);
            session.setAttribute(SESSION_MODULE, moduleSlotValue);
            String speechOutput = "What metrics would you like to know? CPU usage or memory usage.";
            String repromptText =
                    "What metric for " +serverTypeValue +" on "+envSlotValue+" environment?";

            return newAskResponse(speechOutput, repromptText);
        }

        Map<String, String> slotValueMap = new HashMap<String, String>();
        slotValueMap.put(SLOT_MODULE, moduleSlotValue);
        slotValueMap.put(SLOT_ENVIRONMENT, envSlotValue);
        slotValueMap.put(SLOT_SERVER_TYPE, serverTypeValue);
        slotValueMap.put(SLOT_METRICS, metricsSlotValue);

        return getMetricsReponse(slotValueMap);
    }


    private SpeechletResponse handleEnvMetricsDialogRequest(final Intent intent, final Session session) {

            String moduleSlotValue = null;
            String envSlotValue = null;
            String serverTypeValue = null;
            String metricsSlotValue = null;
            try {
                envSlotValue = getSlotValueFromIntent(intent, false, SLOT_ENVIRONMENT);
            } catch (Exception e) {
                // invalid city. move to the dialog
                String speechOutput =
                        "Please try again, the valid environments are QA, STAGE, BETA."
                                + "Which environment would you like to know the metrics for?";

                // repromptText is the same as the speechOutput
                return newAskResponse(speechOutput, speechOutput);
            }
            if (session.getAttributes().containsKey(SESSION_SERVER_TYPE)) {
                serverTypeValue = (String) session.getAttribute(SESSION_SERVER_TYPE);
            } else {
                // set city in session and prompt for date
                session.setAttribute(SESSION_ENVIRONMENT, envSlotValue);
                String speechOutput = "What server: application or database";
                String repromptText =
                        "What server would you like to the metrics for on "+envSlotValue +" environment ?";

                return newAskResponse(speechOutput, repromptText);
            }

            if (session.getAttributes().containsKey(SESSION_MODULE)) {
                moduleSlotValue = (String) session.getAttribute(SESSION_MODULE);
            } else {
                // set city in session and prompt for date
                session.setAttribute(SESSION_SERVER_TYPE, serverTypeValue);
                session.setAttribute(SESSION_ENVIRONMENT, envSlotValue);
                String speechOutput = "For which module?";
                String repromptText =
                        "Which module in "+serverTypeValue +" on "+envSlotValue+" environment?";

                return newAskResponse(speechOutput, repromptText);
            }

            if (session.getAttributes().containsKey(SESSION_METRICS)) {
                metricsSlotValue = (String) session.getAttribute(SESSION_METRICS);
            } else {
                // set city in session and prompt for date
                session.setAttribute(SESSION_SERVER_TYPE, serverTypeValue);
                session.setAttribute(SESSION_ENVIRONMENT, envSlotValue);
                session.setAttribute(SESSION_MODULE, moduleSlotValue);
                String speechOutput = "What metrics would you like to know? CPU usage or memory usage.";
                String repromptText =
                        "What metric for " +serverTypeValue +" on "+envSlotValue+" environment?";

                return newAskResponse(speechOutput, repromptText);
            }

            Map<String, String> slotValueMap = new HashMap<String, String>();
            slotValueMap.put(SLOT_MODULE, moduleSlotValue);
            slotValueMap.put(SLOT_ENVIRONMENT, envSlotValue);
            slotValueMap.put(SLOT_SERVER_TYPE, serverTypeValue);
            slotValueMap.put(SLOT_METRICS, metricsSlotValue);

            return getMetricsReponse(slotValueMap);

    }


    private SpeechletResponse handleMetricsDialogRequest(final Intent intent, final Session session) {
        String moduleSlotValue = null;
        String envSlotValue = null;
        String serverTypeValue = null;
        String metricsSlotValue = null;
        try {
            metricsSlotValue = getSlotValueFromIntent(intent, false, SLOT_METRICS);
        } catch (Exception e) {
            if (session.getAttributes().containsKey(SESSION_METRICS)) {
                metricsSlotValue = (String) session.getAttribute(SESSION_METRICS);
            } else {
                String speechOutput =
                        "The metrics that can be currently reported are CPU usage and memory usage."
                                + "What metrics are you interested in?";

                // repromptText is the same as the speechOutput
                return newAskResponse(speechOutput, speechOutput);
            }
        }

        if (session.getAttributes().containsKey(SESSION_MODULE)) {
            moduleSlotValue = (String) session.getAttribute(SESSION_MODULE);
        } else {
            // set city in session and prompt for date
            session.setAttribute(SESSION_METRICS, metricsSlotValue);
            String speechOutput = "For which module?";
            String repromptText = "you can say UI, API, jobs, connectors or all.";
            return newAskResponse(speechOutput, repromptText);
        }

        if (session.getAttributes().containsKey(SESSION_SERVER_TYPE)) {
            serverTypeValue = (String) session.getAttribute(SESSION_SERVER_TYPE);
        } else {
            // set city in session and prompt for date
            session.setAttribute(SESSION_METRICS, metricsSlotValue);
            session.setAttribute(SESSION_MODULE, moduleSlotValue);
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
            session.setAttribute(SESSION_MODULE, moduleSlotValue);
            String speechOutput = "What environment?";
            String repromptText =
                    metricsSlotValue + " for " +serverTypeValue +" on what environment?";

            return newAskResponse(speechOutput, repromptText);
        }

        Map<String, String> slotValueMap = new HashMap<String, String>();
        slotValueMap.put(SLOT_MODULE, moduleSlotValue);
        slotValueMap.put(SLOT_ENVIRONMENT, envSlotValue);
        slotValueMap.put(SLOT_SERVER_TYPE, serverTypeValue);
        slotValueMap.put(SLOT_METRICS, metricsSlotValue);

        return getMetricsReponse(slotValueMap);
    }

}
