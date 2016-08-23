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
import java.util.HashMap;
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
    
    private static final String LOCATION_SLOT = "location";
    private static final String DURATION_SLOT = "duration";
    private static final String TIME_SLOT = "time";
    private static final String FOOD_SLOT = "food";
    private static final String GENRE_SLOT = "genre";
    private static final String CUISINE_SLOT = "cuisine";
    private static final String PREPOSITION_SLOT = "prep";
    private static final String VERB_SLOT = "verb";
    private static final String INTENTION_SLOT = "intention";


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

        return getWelcomeResponse(session);
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        Intent intent = request.getIntent();
        String intentName = intent.getName();

        if ("GreetingIntent".equals(intentName)) {
        	
            return getWelcomeResponse(session);
            
        } else if ("InputGoalIntent".equals(intentName)) {
        	
            return handleInputGoalRequest(intent, session);
            
        } else if ("InputDestinationIntent".equals(intentName)) {
        	
            return handleInputDestinationRequest(intent, session);
            
        } else if ("InputOriginIntent".equals(intentName)) {
        	
            return handleInputOriginRequest(intent, session);
            
        } else if ("InputCorrectConstraintIntent".equals(intentName)) {
        	
            return handleInputCorrectConstraintRequest(intent, session);
            
        } else if ("InputCorrectGoalIntent".equals(intentName)) {
        	
            return handleInputCorrectGoalRequest(intent, session);
            
        } else if ("RemoveLastTaskIntent".equals(intentName)) {
        	
            return handleRemoveLastTaskRequest(intent, session);
            
        } else if ("ConfirmIntent".equals(intentName)) {
        	
            return handleConfirmRequest(intent, session);
            
        } else if ("DeclineIntent".equals(intentName)) {
        	
            return handleDeclineRequest(intent, session);
            
        } else if ("DeclineWithConstraintIntent".equals(intentName)) {
        	
            return handleDeclineRequest(intent, session);
            
        } else if ("DeclineDepartureRelaxationIntent".equals(intentName)) {
        	
            return handleDeclineDepartureRelaxationRequest(intent, session);
            
        } else if ("DeclineArrivalRelaxationIntent".equals(intentName)) {
        	
            return handleDeclineArrivalRelaxationRequest(intent, session);
            
        } else if ("DeclineArrivalRelaxationIntent".equals(intentName)) {
        	
            return handleDeclineArrivalRelaxationRequest(intent, session);
            
        } else if ("DeclineChoiceIntent".equals(intentName)) {
        	
            return handleDeclineChoiceRequest(intent, session);
            
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

    	HashMap<String,String> params = new HashMap<String,String>();
        StringBuilder speechOutputBuilder = new StringBuilder();
        
        Map<String, Slot> slots = intent.getSlots();
        Slot destinationSlot = slots.get(LOCATION_SLOT);

        // Check for favorite color and create output to user.
        if (destinationSlot != null && destinationSlot.getValue() != null) {
            // Store the user's favorite color in the Session and create response.
            String destination = destinationSlot.getValue();
            
//            speechOutputBuilder.append("<p>");
//            speechOutputBuilder.append("Destination is " + destination);
//            speechOutputBuilder.append("</p> ");
            params.put("destination", destination);
        }
        
        Slot timeSlot = slots.get(TIME_SLOT);

        // Check for favorite color and create output to user.
        if (timeSlot != null && timeSlot.getValue() != null) {
            // Store the user's favorite color in the Session and create response.
            String time = timeSlot.getValue();
            
//            speechOutputBuilder.append("<p>");
//            speechOutputBuilder.append("Time is " + time);
//            speechOutputBuilder.append("</p> ");
            params.put("time", time);
        }
        
        Slot durationSlot = slots.get(DURATION_SLOT);

        // Check for favorite color and create output to user.
        if (durationSlot != null && durationSlot.getValue() != null) {
            // Store the user's favorite color in the Session and create response.
            String duration = durationSlot.getValue();
            
//            speechOutputBuilder.append("<p>");
//            speechOutputBuilder.append("Duration is " + duration);
//            speechOutputBuilder.append("</p> ");
            params.put("duration", duration);
        }
        
        Slot cuisineSlot = slots.get(CUISINE_SLOT);

        // Check for favorite color and create output to user.
        if (cuisineSlot != null && cuisineSlot.getValue() != null) {
            // Store the user's favorite color in the Session and create response.
            String cuisine = cuisineSlot.getValue();
            
//            speechOutputBuilder.append("<p>");
//            speechOutputBuilder.append("Cuisine is " + cuisine);
//            speechOutputBuilder.append("</p> ");
            params.put("cuisine", cuisine);
        }
        
        Slot foodSlot = slots.get(FOOD_SLOT);

        // Check for favorite color and create output to user.
        if (foodSlot != null && foodSlot.getValue() != null) {
            // Store the user's favorite color in the Session and create response.
            String food = foodSlot.getValue();
            
//            speechOutputBuilder.append("<p>");
//            speechOutputBuilder.append("Cuisine is " + cuisine);
//            speechOutputBuilder.append("</p> ");
            params.put("food", food);
        }
        
        ArrayList<String> events = getResponseFromUhura(session.getSessionId(),"Manager/AddGoal",params);
        
        return presentResponse(events);
    }
    
    private SpeechletResponse handleInputOriginRequest(Intent intent, Session session) {

    	HashMap<String,String> params = new HashMap<String,String>();
        StringBuilder speechOutputBuilder = new StringBuilder();
        
        Map<String, Slot> slots = intent.getSlots();
        Slot locationSlot = slots.get(LOCATION_SLOT);

        // Check for favorite color and create output to user.
        if (locationSlot != null && locationSlot.getValue() != null) {
            // Store the user's favorite color in the Session and create response.
            String origin = locationSlot.getValue();
            
//            speechOutputBuilder.append("<p>");
//            speechOutputBuilder.append("Origin is " + origin);
//            speechOutputBuilder.append("</p> ");
            params.put("origin", origin);
        }
        
        Slot timeSlot = slots.get(TIME_SLOT);

        // Check for favorite color and create output to user.
        if (timeSlot != null && timeSlot.getValue() != null) {
            // Store the user's favorite color in the Session and create response.
            String time = timeSlot.getValue();
            
//            speechOutputBuilder.append("<p>");
//            speechOutputBuilder.append("Departure time is " + time);
//            speechOutputBuilder.append("</p> ");
            params.put("time", time);
        }
                
        ArrayList<String> events = getResponseFromUhura(session.getSessionId(),"Manager/SetOrigin",params);
        
        return presentResponse(events);
    }
    
    private SpeechletResponse handleInputDestinationRequest(Intent intent, Session session) {

    	HashMap<String,String> params = new HashMap<String,String>();
        StringBuilder speechOutputBuilder = new StringBuilder();
        
        Map<String, Slot> slots = intent.getSlots();
        Slot locationSlot = slots.get(LOCATION_SLOT);

        // Check for favorite color and create output to user.
        if (locationSlot != null && locationSlot.getValue() != null) {
            // Store the user's favorite color in the Session and create response.
            String destination = locationSlot.getValue();
            
//            speechOutputBuilder.append("<p>");
//            speechOutputBuilder.append("Destination is " + destination);
//            speechOutputBuilder.append("</p> ");
            params.put("destination", destination);
        }
        
        Slot timeSlot = slots.get(TIME_SLOT);

        // Check for favorite color and create output to user.
        if (timeSlot != null && timeSlot.getValue() != null) {
            // Store the user's favorite color in the Session and create response.
            String time = timeSlot.getValue();
            
//            speechOutputBuilder.append("<p>");
//            speechOutputBuilder.append("Arrival time is " + time);
//            speechOutputBuilder.append("</p> ");
            params.put("time", time);
        }
        
        Slot durationSlot = slots.get(DURATION_SLOT);

        // Check for favorite color and create output to user.
        if (durationSlot != null && durationSlot.getValue() != null) {
            // Store the user's favorite color in the Session and create response.
            String duration = durationSlot.getValue();
            
//            speechOutputBuilder.append("<p>");
//            speechOutputBuilder.append("Duration is " + duration);
//            speechOutputBuilder.append("</p> ");
            params.put("duration", duration);
        }
                
        ArrayList<String> events = getResponseFromUhura(session.getSessionId(),"Manager/SetDestination",params);
        
        return presentResponse(events);
    }
    
    private SpeechletResponse handleInputCorrectConstraintRequest(Intent intent, Session session) {
        
    	HashMap<String,String> params = new HashMap<String,String>();
        StringBuilder speechOutputBuilder = new StringBuilder();
        
        Map<String, Slot> slots = intent.getSlots();
    	Slot timeSlot = slots.get(TIME_SLOT);

        // Check for favorite color and create output to user.
        if (timeSlot != null && timeSlot.getValue() != null) {
            // Store the user's favorite color in the Session and create response.
            String time = timeSlot.getValue();
            params.put("time", time);

        }
        
        Slot durationSlot = slots.get(DURATION_SLOT);

        // Check for favorite color and create output to user.
        if (durationSlot != null && durationSlot.getValue() != null) {
            // Store the user's favorite color in the Session and create response.
            String duration = durationSlot.getValue();
            params.put("duration", duration);

        }

        ArrayList<String> events = getResponseFromUhura(session.getSessionId(),"Manager/CorrectConstraint",params);

        return presentResponse(events);
    }
    
    private SpeechletResponse handleInputCorrectGoalRequest(Intent intent, Session session) {
        
    	HashMap<String,String> params = new HashMap<String,String>();
        StringBuilder speechOutputBuilder = new StringBuilder();
        
        Map<String, Slot> slots = intent.getSlots();
        
        Slot destinationSlot = slots.get(LOCATION_SLOT);
        // Check for favorite color and create output to user.
        if (destinationSlot != null && destinationSlot.getValue() != null) {
            // Store the user's favorite color in the Session and create response.
            String destination = destinationSlot.getValue();
            
//            speechOutputBuilder.append("<p>");
//            speechOutputBuilder.append("Destination is " + destination);
//            speechOutputBuilder.append("</p> ");
            params.put("location", destination);
        }
        
        Slot cuisineSlot = slots.get(CUISINE_SLOT);
        // Check for favorite color and create output to user.
        if (cuisineSlot != null && cuisineSlot.getValue() != null) {
            // Store the user's favorite color in the Session and create response.
            String cuisine = cuisineSlot.getValue();
            
//            speechOutputBuilder.append("<p>");
//            speechOutputBuilder.append("Cuisine is " + cuisine);
//            speechOutputBuilder.append("</p> ");
            params.put("cuisine", cuisine);
        }

        ArrayList<String> events = getResponseFromUhura(session.getSessionId(),"Manager/CorrectGoal",params);

        return presentResponse(events);
    }
    
    private SpeechletResponse handleRemoveLastTaskRequest(Intent intent, Session session) {
        
    	HashMap<String,String> params = new HashMap<String,String>();
        StringBuilder speechOutputBuilder = new StringBuilder();
        
        ArrayList<String> events = getResponseFromUhura(session.getSessionId(),"Manager/RemoveLastTask",params);

        return presentResponse(events);
    }
    
    private SpeechletResponse handleConfirmRequest(Intent intent, Session session) {
    	
    	HashMap<String,String> params = new HashMap<String,String>();
        ArrayList<String> events = getResponseFromUhura(session.getSessionId(),"Manager/SendConfirm",params);

        return presentResponse(events);
    }
    
    private SpeechletResponse handleDeclineRequest(Intent intent, Session session) {
        
    	HashMap<String,String> params = new HashMap<String,String>();
    	Map<String, Slot> slots = intent.getSlots();
    	double cc = 0;
    	
    	for (String key : slots.keySet()){
    		if (slots.get(key).getValue() != null){
    			if (key.equals("cc_a")){
        			cc += 0.01*Integer.parseInt(slots.get(key).getValue());
            	} else if (key.equals("cc_b")){
            		cc += 0.01*Double.parseDouble("0."+slots.get(key).getValue());
            	} else {
            		params.put(key, slots.get(key).getValue());
            	} 
    		}   		
    	}
    	
    	if (slots.containsKey("cc_a") || slots.containsKey("cc_b")){
    		params.put("cc", ""+cc);
    	}

        ArrayList<String> events = getResponseFromUhura(session.getSessionId(),"Manager/SendDecline",params);

        return presentResponse(events);
    }
    
    private SpeechletResponse handleDeclineDepartureRelaxationRequest(Intent intent, Session session) {
        
    	HashMap<String,String> params = new HashMap<String,String>();
    	Map<String, Slot> slots = intent.getSlots();
    	
    	for (String key : slots.keySet()){
    		if (slots.get(key).getValue() != null){
            	params.put(key, slots.get(key).getValue());
    		}   		
    	}

        ArrayList<String> events = getResponseFromUhura(session.getSessionId(),"Manager/SendDeclineDepartureRelaxation",params);
        return presentResponse(events);
    }
    
    private SpeechletResponse handleDeclineArrivalRelaxationRequest(Intent intent, Session session) {
        
    	HashMap<String,String> params = new HashMap<String,String>();
    	Map<String, Slot> slots = intent.getSlots();
    	
    	for (String key : slots.keySet()){
    		if (slots.get(key).getValue() != null){
            	params.put(key, slots.get(key).getValue());
    		}   		
    	}

        ArrayList<String> events = getResponseFromUhura(session.getSessionId(),"Manager/SendDeclineArrivalRelaxation",params);
        return presentResponse(events);
    }
    
    private SpeechletResponse handleDeclineChoiceRequest(Intent intent, Session session) {
        
    	HashMap<String,String> params = new HashMap<String,String>();
    	Map<String, Slot> slots = intent.getSlots();
    	
    	for (String key : slots.keySet()){
    		if (slots.get(key).getValue() != null){
            	params.put(key, slots.get(key).getValue());
    		}   		
    	}

        ArrayList<String> events = getResponseFromUhura(session.getSessionId(),"Manager/SendDeclineChoice",params);
        return presentResponse(events);
    }
    
    private SpeechletResponse presentResponse(ArrayList<String> events){
    	StringBuilder speechOutputBuilder = new StringBuilder();

        if (events.isEmpty()) {
            String speechOutput =
                    "There is a problem connecting to Uhura at this time."
                            + " Please try again later.";

            // Create the plain text output
            SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
            outputSpeech.setSsml("<speak>" + speechOutput + "</speak>");

            return SpeechletResponse.newTellResponse(outputSpeech);
            
        } else if (events.contains("terminal")) {
        	for (int i = 0; i < events.size(); i++) {
        		if (!events.get(i).equals("terminal")){
        			speechOutputBuilder.append("<p>");
                    speechOutputBuilder.append(events.get(i));
                    speechOutputBuilder.append("</p> ");
        		}                
            }
            String speechOutput = speechOutputBuilder.toString();

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

    /**
     * Function to handle the onLaunch skill behavior.
     * 
     * @return SpeechletResponse object with voice/card response to return to the user
     */
    private SpeechletResponse getWelcomeResponse(Session session) {
    	
    	String speechOutput = null;   
    	HashMap<String,String> params = new HashMap<String,String>();
        ArrayList<String> events = getResponseFromUhura(session.getSessionId(),"Manager/ResetSession",params);

        if (events.isEmpty()) {
        	speechOutput =
                    "There is a problem connecting to Uhura at this time."
                            + " Please try again later.";

            // Create the plain text output
            SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
            outputSpeech.setSsml("<speak>" + speechOutput + "</speak>");

            return SpeechletResponse.newTellResponse(outputSpeech);
        } else {
        	Calendar calendar = Calendar.getInstance();
        	calendar.setTimeZone(TimeZone.getTimeZone("GMT-5"));
        	int hours = calendar.get(Calendar.HOUR_OF_DAY);
        	
        	if (hours >= 6 && hours < 12){
            	speechOutput = "Good morning, this is Uhura. How may I help you?";

        	} else if (hours >= 12 && hours < 18){
            	speechOutput = "Good afternoon, this is Uhura. How may I help you?";

        	} else if (hours >=18 && hours < 22){
            	speechOutput = "Good evening, this is Uhura. How may I help you?";

        	} else {
            	speechOutput = "Hello, this is Uhura. How may I help you?";
        	}
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

    public static ArrayList<String> getResponseFromUhura(String sessionID, String type, HashMap<String,String> params) {
        InputStreamReader inputStream = null;
        BufferedReader bufferedReader = null;
        String text = "";
        try {
            String line;
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(URL_PREFIX + type + "?sessionID="+sessionID);
            
            for (String key : params.keySet()){
            	if (params.get(key) != null){
            		urlBuilder.append("&"+key + "=" + params.get(key).replace(" ", "%20"));
            	}
            }
            
            URL url = new URL(urlBuilder.toString());
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
    		
    		if (obj.has("status")){
    			String status_output = obj.getString("status");
        		events.add(status_output);
    		}
        	
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
