# uhura-echo-interface
A custom Echo skill for interfacing with Uhura's backend. This instruction is written based on the example skills provided by Amazon.

## Setup
To run this skill you need to do two things. The first is to deploy the example code in lambda, and the second is to configure the Alexa skill to use Lambda.

### AWS Lambda Setup
1. Go to the AWS Console and click on the Lambda link. Note: ensure you are in us-east or you wont be able to use Alexa with Lambda.
2. Click on the Create a Lambda Function or Get Started Now button.
3. Skip the blueprint
4. Name the Lambda Function "Uhura".
5. Select the runtime as Java 8
6. Go to the the directory containing pom.xml, and run 'mvn assembly:assembly -DdescriptorId=jar-with-dependencies package'. This will generate a zip file named "uhura-echo-interface-0.1-jar-with-dependencies.jar" in the target directory.
7. Select Code entry type as "Upload a .ZIP file" and then upload the "uhura-echo-interface-0.1-jar-with-dependencies.jar" file from the build directory to Lambda
8. Set the Handler as dialoginterface.UhuraDialogSpeechletRequestStreamHandler (this refers to the Lambda RequestStreamHandler file in the zip).
9. Create a basic execution role and click create.
10. Leave the Advanced settings as the defaults.
11. Click "Next" and review the settings then click "Create Function"
12. Click the "Event Sources" tab and select "Add event source"
13. Set the Event Source type as Alexa Skills kit and Enable it now. Click Submit.
14. Copy the ARN from the top right to be used later in the Alexa Skill Setup.

### Alexa Skill Setup
1. Go to the [Alexa Console](https://developer.amazon.com/edw/home.html) and click Add a New Skill.
2. Set "Uhura" as the skill name and "uhura" as the invocation name, this is what is used to activate your skill. For example you would say: "Alexa/Echo, start uhura."
3. Select the Lambda ARN for the skill Endpoint and paste the ARN copied from above. Click Next.
4. Copy the custom slot types from the customSlotTypes folder. Each file in the folder represents a new custom slot type. The name of the file is the name of the custom slot type, and the values in the file are the values for the custom slot.
5. Copy the Intent Schema from the included IntentSchema.json.
6. Copy the Sample Utterances from the included SampleUtterances.txt. Click Next.
7. Go back to the skill Information tab and copy the appId. Paste the appId into the UhuraDialogSpeechletRequestStreamHandler.java file for the variable supportedApplicationIds,
   then update the lambda source zip file with this change and upload to lambda again, this step makes sure the lambda function only serves request from authorized source.
8. You are now able to start testing your sample skill! You should be able to go to the [Echo webpage](http://echo.amazon.com/#skills) and see your skill enabled.
9. In order to test it, try to say some of the Sample Utterances from the Examples section below.
10. Your skill is now saved and once you are finished testing you can continue to publish your skill.

### Graphical Interface
You may visit https://uhura.csail.mit.edu/echo-index.html to visualize the problem and solution Uhura is currently working on. Currently the backend only supports one user at a time. 

## Examples
User:  "Alexa/Echo, start uhura"

Alexa: "Good afternoon, this is Uhura, how may I help you?"

User:  "Take me home by seven pm."

Alexa: "Ok, anything else?"

User:  "Stop at a bike shop for five minutes."

Alexa: "Ok, anything else?"

User:  "No, that's it"

Alexa:  "Ok, I have found a plan for you ... Is that ok?"

User: "No."

Alexa:  "Ok, I have found another plan for you ... Is that ok?"

User: "That's fine."

Alexa:  "Have a nice trip!"

