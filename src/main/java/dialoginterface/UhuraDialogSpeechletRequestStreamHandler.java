package dialoginterface;

import java.util.HashSet;
import java.util.Set;

import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

/**
 * This class could be the handler for an AWS Lambda function powering an Alexa Skills Kit
 * experience. To do this, simply set the handler field in the AWS Lambda console to
 * "historybuff.HistoryBuffSpeechletRequestStreamHandler" For this to work, you'll also need to
 * build this project using the {@code lambda-compile} Ant task and upload the resulting zip file to
 * power your function.
 */
public class UhuraDialogSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {

    private static final Set<String> supportedApplicationIds;

    static {
        /*
         * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit" the relevant
         * Alexa Skill and put the relevant Application Ids in this Set.
         */
        supportedApplicationIds = new HashSet<String>();
        supportedApplicationIds.add("amzn1.echo-sdk-ams.app.68eaa848-4117-4e35-9b60-81aeaebf0ebb");
        supportedApplicationIds.add("amzn1.ask.skill.e8e219d4-2aa8-4caa-a9ba-0a4c4a1af076");
        supportedApplicationIds.add("amzn1.ask.skill.8a7941f3-f633-4a52-bb7a-c2c501d75807");
    }

    public UhuraDialogSpeechletRequestStreamHandler() {
        super(new UhuraDialogSpeechlet(), supportedApplicationIds);
    }

    public UhuraDialogSpeechletRequestStreamHandler(Speechlet speechlet,
            Set<String> supportedApplicationIds) {
        super(speechlet, supportedApplicationIds);
    }

}
