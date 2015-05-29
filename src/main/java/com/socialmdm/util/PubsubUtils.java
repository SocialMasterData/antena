package com.socialmdm.util;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.pubsub.Pubsub;
import com.google.api.services.pubsub.PubsubScopes;
import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.apphosting.api.ApiProxy;
import com.google.common.base.Preconditions;

/**
 * Utility class to interact with the Pub/Sub API.
 */
public final class PubsubUtils {
    
    private static final Logger logger = Logger.getLogger(PubsubUtils.class);

    private static final String APPLICATION_NAME ="socialmdm-antena";

    /**
     * Builds a new Pubsub client with default HttpTransport and JsonFactory and returns it.
     *
     * @return Pubsub client.
     * @throws IOException when we can not get the default credentials.
     */
    public static Pubsub getClient() throws IOException {
        return getClient(Utils.getDefaultTransport(), Utils.getDefaultJsonFactory());
    }

    /**
     * Builds a new Pubsub client and returns it.
     *
     * @param httpTransport HttpTransport for Pubsub client.
     * @param jsonFactory JsonFactory for Pubsub client.
     * @return Pubsub client.
     * @throws IOException when we can not get the default credentials.
     */
    public static Pubsub getClient(HttpTransport httpTransport, JsonFactory jsonFactory)
            throws IOException {
        Preconditions.checkNotNull(httpTransport);
        Preconditions.checkNotNull(jsonFactory);
        GoogleCredential credential = GoogleCredential.getApplicationDefault();
        if (credential.createScopedRequired()) {
            credential = credential.createScoped(PubsubScopes.all());
        }
        // Please use custom HttpRequestInitializer for automatic
        // retry upon failures.
        HttpRequestInitializer initializer =
                new RetryHttpInitializerWrapper(credential);
        return new Pubsub.Builder(httpTransport, jsonFactory, initializer)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static String getAppTopicName() {
        return "topic-antena";
    }

    public static String getAppSubscriptionName() {
        return "subscription-" + getProjectId();
    }

    public static String getAppEndpointUrl() {
        logger.info( String.format("The Subscription url is %s", getAppSubscriptionName()) );
        String endPointUrl= "https://" + getProjectId() + ".appspot.com/receive_message" +
                "?token=" + getAppSubscriptionName();
        logger.info(String.format("The Endpoint url is %s", endPointUrl));
        return endPointUrl;
    }

    public static String getProjectId() {
        AppIdentityService identityService =
                AppIdentityServiceFactory.getAppIdentityService();

        // The project ID associated to an app engine application is the same
        // as the app ID.
        return identityService.parseFullAppId(ApiProxy.getCurrentEnvironment()
                .getAppId()).getId();
    }
}
