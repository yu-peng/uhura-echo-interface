package dialoginterface;

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
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
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
            "http://uhura.csail.mit.edu:8080/uhura-dialog-manager/";

    /**
     * Constant defining session attribute key for the event index.
     */
    private static final String SESSION_INDEX = "index";
    
    private static final String DESTINATION_SLOT = "destination";
    private static final String DURATION_SLOT = "duration";
    private static final String TIME_SLOT = "time";
    private static final String FOOD_SLOT = "food";
    private static final String GENRE_SLOT = "genre";
    private static final String CUISINE_SLOT = "cuisine";


//    public static void main(String[] args){
//    	ArrayList<String> results = getResponseFromUhura("asd");
//    	System.out.println("Results size " + results.size());
//    	for (String result : results){
//    		System.out.println(result);
//    	}
//    }
    
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

        if ("GreetingIntent".equals(intentName)) {
        	
            return getWelcomeResponse();
            
        } else if ("InputGoalIntent".equals(intentName)) {
        	
            return handleInputGoalRequest(intent, session);
            
        } else if ("InputConstraintIntent".equals(intentName)) {
        	
            return handleInputConstraintRequest(intent, session);
            
        } else if ("ConfirmIntent".equals(intentName)) {
        	
            return handleConfirmRequest(intent, session);
            
        } else if ("DeclineIntent".equals(intentName)) {
        	
            return handleDeclineRequest(intent, session);
            
        } else if ("AMAZON.HelpIntent".equals(intentName)) {
        	
            return getHelpResponse();
            
        } else if ("AMAZON.StopIntent".equals(intentName)) {
        	
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText("Goodbye.");

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

    }
    
    private SpeechletResponse handleInputGoalRequest(Intent intent, Session session) {

        ArrayList<String> events = getResponseFromUhura(session.getSessionId(),"manager","Hello");
        StringBuilder speechOutputBuilder = new StringBuilder();;
        
        Map<String, Slot> slots = intent.getSlots();
        Slot destinationSlot = slots.get(DESTINATION_SLOT);

        // Check for favorite color and create output to user.
        if (destinationSlot != null && destinationSlot.getValue() != null) {
            // Store the user's favorite color in the Session and create response.
            String destination = destinationSlot.getValue();
            
            speechOutputBuilder.append("<p>");
            speechOutputBuilder.append("Destination is " + destination);
            speechOutputBuilder.append("</p> ");
        }
        
        Slot timeSlot = slots.get(TIME_SLOT);

        // Check for favorite color and create output to user.
        if (timeSlot != null && timeSlot.getValue() != null) {
            // Store the user's favorite color in the Session and create response.
            String time = timeSlot.getValue();
            
            speechOutputBuilder.append("<p>");
            speechOutputBuilder.append("Time is " + time);
            speechOutputBuilder.append("</p> ");
        }
        
        Slot durationSlot = slots.get(DURATION_SLOT);

        // Check for favorite color and create output to user.
        if (durationSlot != null && durationSlot.getValue() != null) {
            // Store the user's favorite color in the Session and create response.
            String duration = durationSlot.getValue();
            
            speechOutputBuilder.append("<p>");
            speechOutputBuilder.append("Duration is " + duration);
            speechOutputBuilder.append("</p> ");
        }
        
        Slot cuisineSlot = slots.get(CUISINE_SLOT);

        // Check for favorite color and create output to user.
        if (cuisineSlot != null && cuisineSlot.getValue() != null) {
            // Store the user's favorite color in the Session and create response.
            String cuisine = cuisineSlot.getValue();
            
            speechOutputBuilder.append("<p>");
            speechOutputBuilder.append("Cuisine is " + cuisine);
            speechOutputBuilder.append("</p> ");
        }
        
        if (events.isEmpty()) {
            String speechOutput =
                    "There is a problem connecting to Uhura at this time."
                            + " Please try again later.";

            // Create the plain text output
            SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
            outputSpeech.setSsml("<speak>" + speechOutput + "</speak>");

            return SpeechletResponse.newTellResponse(outputSpeech);
            
        } else {
        	            
            for (int i = 0; i < events.size(); i++) {
                speechOutputBuilder.append("<p>");
                speechOutputBuilder.append(events.get(i));
                speechOutputBuilder.append("</p> ");
            }
            
            String speechOutput = speechOutputBuilder.toString();

            String repromptText =
                    "With Uhura, you can plan a trip of multiple tasks and requirements in your city. "
                            + " For example, you could say take me to a pizza restaurant in 30 minutes."
                            + " Now, how can I help you today?";


            SpeechletResponse response = newAskResponse("<speak>" + speechOutput + "</speak>", true, repromptText, false);
            return response;
        }
    }
    
    private SpeechletResponse handleInputConstraintRequest(Intent intent, Session session) {
        

        String speechOutput = "Constraint input request received.";

        String repromptText =
                "With Uhura, you can plan a trip of multiple tasks and requirements in your city. "
                        + " For example, you could say take me to a pizza restaurant in 30 minutes."
                        + " Now, how can I help you today?";


        SpeechletResponse response = newAskResponse("<speak>" + speechOutput + "</speak>", true, repromptText, false);
        return response;
    }
    
    private SpeechletResponse handleConfirmRequest(Intent intent, Session session) {
        

        String speechOutput = "Confirm input request received.";

        String repromptText =
                "With Uhura, you can plan a trip of multiple tasks and requirements in your city. "
                        + " For example, you could say take me to a pizza restaurant in 30 minutes."
                        + " Now, how can I help you today?";


        SpeechletResponse response = newAskResponse("<speak>" + speechOutput + "</speak>", true, repromptText, false);
        return response;
    }
    
    private SpeechletResponse handleDeclineRequest(Intent intent, Session session) {
        

        String speechOutput = "Decline input request received.";

        String repromptText =
                "With Uhura, you can plan a trip of multiple tasks and requirements in your city. "
                        + " For example, you could say take me to a pizza restaurant in 30 minutes."
                        + " Now, how can I help you today?";


        SpeechletResponse response = newAskResponse("<speak>" + speechOutput + "</speak>", true, repromptText, false);
        return response;
    }

    /**
     * Function to handle the onLaunch skill behavior.
     * 
     * @return SpeechletResponse object with voice/card response to return to the user
     */
    private SpeechletResponse getWelcomeResponse() {
    	
    	String speechOutput = null;
    	
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTimeZone(TimeZone.getTimeZone("GMT-5"));
    	int hours = calendar.get(Calendar.HOUR_OF_DAY);
    	
    	if (hours >= 6 && hours < 12){
        	speechOutput = "Good morning, this is Uhura.";

    	} else if (hours >= 12 && hours < 18){
        	speechOutput = "Good afternoon, this is Uhura.";

    	} else if (hours >=18 && hours < 22){
        	speechOutput = "Good evening, this is Uhura.";

    	} else {
        	speechOutput = "Hello, this is Uhura.";
    	}
        
        // If the user either does not reply to the welcome message or says something that is not
        // understood, they will be prompted again with this text.
        
        String repromptText =
                "With Uhura, you can plan a trip of multiple tasks and requirements in your city. "
                        + " For example, you could say take me to a pizza restaurant in 30 minutes."
                        + " Now, how can I help you today?";

        return newAskResponse(speechOutput, false, repromptText, false);
    }
    
    private SpeechletResponse getHelpResponse() {
    	
        // Create the plain text output.
        String speechOutput =
                "With Uhura, you can plan a trip of multiple tasks and requirements in your city. "
                        + " For example, you could say take me to a pizza restaurant in 30 minutes."
                        + " Now, how can I help you today?";

        String repromptText = "How can I help you today?";

        return newAskResponse(speechOutput, false, repromptText, false);
    }

    public static ArrayList<String> getResponseFromUhura(String sessionID, String type, String input) {
        InputStreamReader inputStream = null;
        BufferedReader bufferedReader = null;
        String text = "";
        try {
            String line;
            URL url = new URL(URL_PREFIX + type + "?sessionID="+sessionID+"&input=" + input);
            System.out.println("URL: " + url.toString());
            
            inputStream = new InputStreamReader(url.openStream());
            bufferedReader = new BufferedReader(inputStream);
            
            
            StringBuilder builder = new StringBuilder();            
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
            text = builder.toString();
        } catch (IOException e) {
            // reset text variable to a blank string
        	e.printStackTrace();
            text = "";
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(bufferedReader);
        }
        
        return parseJson(text);
    }

    /**
     * Parse the JSON string returned from Uhura backend
     * 
     * @param text
     * @return
     */
    public static ArrayList<String> parseJson(String text){

    	System.out.println("Received: " + text);
    	
        ArrayList<String> events = new ArrayList<String>();
        
        if (text.isEmpty()) {
            return events;
        }
        
        try {
        	
        	JSONObject obj = new JSONObject(text);	
    		
    		// Parse the output of Uhura from text
    		if (!obj.has("text_output")){
    			return events;
    		}
    		
    		String uhura_output = obj.getString("text_output");
    		events.add(uhura_output);
        	
        } catch (JSONException e){
        	e.printStackTrace();
        }
        
		
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
