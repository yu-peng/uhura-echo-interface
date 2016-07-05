package dialogmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;


public class UhuraDialogSpeechlet implements Speechlet {
    private static final Logger log = LoggerFactory.getLogger(UhuraDialogSpeechlet.class);

    /**
     * URL prefix to send planning request to Uhura.
     */
    private static final String URL_PREFIX =
            "https://uhura.csail.mit.edu:8443/uhura-web-interface/";

    /**
     * Constant defining session attribute key for the event index.
     */
    private static final String SESSION_INDEX = "index";

    /**
     * Constant defining session attribute key for the event text key for date of events.
     */
    private static final String SESSION_TEXT = "text";


    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        // any initialization logic goes here
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        Intent intent = request.getIntent();
        String intentName = intent.getName();

        if ("GetInputIntent".equals(intentName)) {
            return handleInputRequest(intent, session);
        } else if ("GetResponseIntent".equals(intentName)) {
            return handleResponseRequest(session);
        } else if ("AMAZON.HelpIntent".equals(intentName)) {
            // Create the plain text output.
            String speechOutput =
                    "With Uhura, you can plan a trip of multiple tasks and requirements in your city. "
                            + " For example, you could say take me to a pizza restaurant in 30 minutes."
                            + " Now, how can I help you today?";

            String repromptText = "How can I help you today?";

            return newAskResponse(speechOutput, false, repromptText, false);
        } else if ("AMAZON.StopIntent".equals(intentName)) {
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText("Have a nice trip! Goodbye.");

            return SpeechletResponse.newTellResponse(outputSpeech);
        } else if ("AMAZON.CancelIntent".equals(intentName)) {
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText("Goodbye");

            return SpeechletResponse.newTellResponse(outputSpeech);
        } else {
            throw new SpeechletException("Invalid Intent");
        }
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        // any session cleanup logic would go here
    }

    /**
     * Function to handle the onLaunch skill behavior.
     * 
     * @return SpeechletResponse object with voice/card response to return to the user
     */
    private SpeechletResponse getWelcomeResponse() {
        String speechOutput = "Hello, this is Uhura.";
        // If the user either does not reply to the welcome message or says something that is not
        // understood, they will be prompted again with this text.
        String repromptText =
                "With Uhura, you can plan a trip of multiple tasks and requirements in your city. "
                        + " For example, you could say take me to a pizza restaurant in 30 minutes."
                        + " Now, how can I help you today?";

        return newAskResponse(speechOutput, false, repromptText, false);
    }

    private SpeechletResponse handleInputRequest(Intent intent, Session session) {
     

        ArrayList<String> events = getResponseFromUhura(intent.toString());
        String speechOutput = null;
        
        if (events.isEmpty()) {
            speechOutput =
                    "There is a problem connecting to Uhura at this time."
                            + " Please try again later.";

            // Create the plain text output
            SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
            outputSpeech.setSsml("<speak>" + speechOutput + "</speak>");

            return SpeechletResponse.newTellResponse(outputSpeech);
            
        } else {
        	
            StringBuilder speechOutputBuilder = new StringBuilder();
            speechOutput = speechOutputBuilder.toString();

            String repromptText =
                    "With Uhura, you can plan a trip of multiple tasks and requirements in your city. "
                            + " For example, you could say take me to a pizza restaurant in 30 minutes."
                            + " Now, how can I help you today?";


            SpeechletResponse response = newAskResponse("<speak>" + speechOutput + "</speak>", true, repromptText, false);
            return response;
        }
    }

    private SpeechletResponse handleResponseRequest(Session session) {
        ArrayList<String> events = (ArrayList<String>) session.getAttribute(SESSION_TEXT);
        int index = (Integer) session.getAttribute(SESSION_INDEX);
        String speechOutput = "";

        if (events == null) {
            speechOutput =
                    "With Uhura, you can plan a trip of multiple tasks and requirements in your city. "
                            + " For example, you could say take me to a pizza restaurant in 30 minutes."
                            + " Now, how can I help you today?";
        } else {
            StringBuilder speechOutputBuilder = new StringBuilder();
            speechOutputBuilder.append("<p>");
            speechOutputBuilder.append(events.get(index));
            speechOutputBuilder.append("</p> ");

            session.setAttribute(SESSION_INDEX, index);
            speechOutput = speechOutputBuilder.toString();
        }
        
        String repromptText = "Is that ok?";

        SpeechletResponse response = newAskResponse("<speak>" + speechOutput + "</speak>", true, repromptText, false);
        return response;
    }

    private ArrayList<String> getResponseFromUhura(String input) {
        InputStreamReader inputStream = null;
        BufferedReader bufferedReader = null;
        String text = "";
        try {
            String line;
            URL url = new URL(URL_PREFIX + "dialogmanager?input=" + input);
            inputStream = new InputStreamReader(url.openStream(), Charset.forName("US-ASCII"));
            bufferedReader = new BufferedReader(inputStream);
            StringBuilder builder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
            text = builder.toString();
        } catch (IOException e) {
            // reset text variable to a blank string
            text = "";
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(bufferedReader);
        }
        return parseJson(text);
    }

    private ArrayList<String> parseJson(String text) {

        ArrayList<String> events = new ArrayList<String>();
        if (text.isEmpty()) {
            return events;
        }
        int startIndex = 0, endIndex = 0;
        while (endIndex != -1) {
            endIndex = text.indexOf("\\n", startIndex);
            String eventText =
                    (endIndex == -1 ? text.substring(startIndex) : text.substring(startIndex,
                            endIndex));
            
            // replace dashes returned in text from Wikipedia's API
            Pattern pattern = Pattern.compile("\\\\u2013\\s*");
            Matcher matcher = pattern.matcher(eventText);
            eventText = matcher.replaceAll("");
            
            // add comma after year so Alexa pauses before continuing with the sentence
            pattern = Pattern.compile("(^\\d+)");
            matcher = pattern.matcher(eventText);
            if (matcher.find()) {
                eventText = matcher.replaceFirst(matcher.group(1) + ",");
            }
            eventText = "In " + eventText;
            startIndex = endIndex + 2;
            events.add(eventText);
        }
        Collections.reverse(events);
        return events;
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
            ((SsmlOutputSpeech) repromptOutputSpeech).setSsml(repromptText);
        } else {
            repromptOutputSpeech = new PlainTextOutputSpeech();
            ((PlainTextOutputSpeech) repromptOutputSpeech).setText(repromptText);
        }
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptOutputSpeech);
        return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
    }

}
