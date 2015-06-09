package com.socialmdm.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.prediction.Prediction;
import com.google.api.services.prediction.PredictionScopes;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.services.prediction.model.Input;
import com.google.api.services.prediction.model.Input.InputInput;
import com.google.api.services.prediction.model.Output;
import com.socialmdm.util.Constants;
import com.socialmdm.util.LoggerUtil;

public class PredictionService {
    
    // Singleton object
    private static PredictionService predictionService;
    private PredictionService(){}
    public static synchronized PredictionService getInstance() {
        if( predictionService == null ) {
            predictionService = new PredictionService();
        }
        return predictionService;
    }

    /** Directory to store user credentials. */
    private static final java.io.File DATA_STORE_DIR =
            new java.io.File(System.getProperty(Constants.USER_HOME), Constants.USER_CREDENTIAL_STORE_LOC);

    private static FileDataStoreFactory dataStoreFactory = null;

    private static HttpTransport httpTransport = null;

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /** Authorizes the installed application to access user's protected data. */
    private Credential authorize() throws IOException {
        try {
            // load client secrets
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                    new InputStreamReader(PredictionService.class.getResourceAsStream("/client_secrets.json")));
            if (clientSecrets.getDetails().getClientId().startsWith("Enter")
                    || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
                LoggerUtil.writeInfo(String.format("Enter Client ID and Secret from %s into %s", 
                        "https://code.google.com/apis/console/?api=prediction", 
                        "/src/main/resources/client_secrets.json"), this.getClass());
            }
            // set up authorization code flow
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, JSON_FACTORY, clientSecrets,
                    Collections.singleton(PredictionScopes.PREDICTION)).setDataStoreFactory(
                            dataStoreFactory).build();
            // authorize
            return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        } catch (IOException ex) {
            throw new IOException(ex);
        }
    } 

    public String predict(String text) {
        String predictedLabel = null;
        try {
            if( httpTransport == null ) {
                httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            }
            if( dataStoreFactory == null ) {
                dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
            }
            
            // authorization
            Credential credential = authorize();
            Prediction prediction = new Prediction.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(Constants.APPLICATION_NAME).build();

            Input input = new Input();
            InputInput inputInput = new InputInput();
            inputInput.setCsvInstance(Collections.<Object>singletonList(text));
            input.setInput(inputInput);
            Output output = prediction.trainedmodels().predict( Constants.PROJECT_ID, Constants.MODEL_ID, input).execute();
            predictedLabel = output.getOutputLabel();
            LoggerUtil.writeInfo(String.format("Text: %s", text), this.getClass());
            LoggerUtil.writeInfo(String.format("Predicted: %s", predictedLabel), this.getClass());
            return predictedLabel;
        } catch (IOException ioEx) {
            LoggerUtil.writeError(String.format("Error occured while Prediction for Text: %s \nError message: %s", text, ioEx), this.getClass());
        } catch (GeneralSecurityException ge) {
            LoggerUtil.writeError(String.format("Error occured while Prediction for Text: %s \nError message: %s", text, ge), this.getClass());
        }
        return predictedLabel;
    }

}