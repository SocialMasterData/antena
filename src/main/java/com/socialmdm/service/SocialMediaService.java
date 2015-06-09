package com.socialmdm.service;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

import com.google.api.client.util.Strings;
import com.socialmdm.util.Constants;
import com.socialmdm.util.LoggerUtil;

/**
 * SocialMediaService for SocialMedia related operations
 * 
 * @author Naveen
 *
 */
@Controller
public class SocialMediaService {
    

    private SimpMessagingTemplate messagingTemplate;
    private static TwitterStream twitterStream = null;

    // Static thread pool with maximum thread count of 10
    private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * Constructor
     * @param messagingTemplate
     */
    @Autowired
    public SocialMediaService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Get the token to create a socket connection
     * 
     * @param response
     */
    @RequestMapping("/getToken")
    public void getToken(HttpServletResponse response) {
        String uuid = null;
        try {
            uuid = UUID.randomUUID().toString();
            LoggerUtil.writeInfo(String.format("UUID: %s", uuid), this.getClass());
            response.getWriter().write(uuid);
            response.getWriter().close();
        } catch (IOException e) {
            LoggerUtil.writeError(String.format("Error occured for\n UUID: %s\n Error Message: %s", uuid, e.getMessage()), this.getClass());
        }
    }

    /**
     * Search the twitter with the given keyword
     * 
     * @param keyword
     * @param token
     * @param response
     * @throws IOException 
     */
    @RequestMapping("/search")
    public void search(@PathParam("keyword") final String keyword,@PathParam("token") final String token, HttpServletResponse response) throws IOException {
        try {
            if( Strings.isNullOrEmpty(keyword) ) {
                LoggerUtil.writeWarn("Keyword cannot be null", this.getClass());
                response.getWriter().write("Keyword cannot be null");
                response.getWriter().close();
                return;
            }
            if( Strings.isNullOrEmpty(token) ) {
                LoggerUtil.writeWarn("Token cannot be null", this.getClass());
                response.getWriter().write("Please reload your webpage");
                response.getWriter().close();
                return;
            }

            LoggerUtil.writeDebug(String.format("Keyword: %s\n Token (UUID): %s", keyword, token), this.getClass());

            //Create Topic in PubSub
            final String fullTopicName = PubSubService.getInstance().createTopic(keyword);
            final String fullTopicNameSpamFree = PubSubService.getInstance().createTopic(keyword + Constants.TOPIC_SPAMFREE_APPEND);

            // Shutdown the previous twitterStream if there is any, Twitter allows only one open connection from an account
            closeTwitterStream();

            // Twitter Configuration
            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true)
            .setOAuthConsumerKey(Constants.OAUTH_CONSUMER_KEY)
            .setOAuthConsumerSecret(Constants.OAUTH_CONSUMER_SECRET)
            .setOAuthAccessToken(Constants.OAUTH_ACCESS_TOKEN)
            .setOAuthAccessTokenSecret(Constants.OAUTH_ACCESS_TOKEN_SECRET);

            // TwitterStream Status Listener
            StatusListener listener = new StatusListener(){
                public void onStatus(Status status) {
                    if( !Strings.isNullOrEmpty(fullTopicName) && status != null ) {
                        pushToPubSubThread(fullTopicName, status);
                        if( PredictionService.getInstance().predict(status.getText()).equals(Constants.PREDICTION_NOT_SPAM) ) {
                            pushToPubSubThread(fullTopicNameSpamFree, status);
                            pushToSocket(status, token);
                        }
                    }else {
                        LoggerUtil.writeError("fullTopicName or status is NullOrEmpty", this.getClass());
                    }
                }
                public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
                public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
                public void onException(Exception ex) {
                    LoggerUtil.writeError(String.format("Twiiter Stream exception %s",ex), this.getClass());
                }
                public void onScrubGeo(long arg0, long arg1) {
                    // TODO Auto-generated method stub
                }
                public void onStallWarning(StallWarning arg0) {
                    // TODO Auto-generated method stub
                }
            };

            twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
            twitterStream.addListener(listener);

            FilterQuery fq = new FilterQuery();
            fq.track(new String[] {"#"+ keyword});
            fq.language(new String[]{"en"});
            twitterStream.filter(fq);

            LoggerUtil.writeInfo(String.format("TwitterStream successfully started for\n Keyword: %s \n Token (UUID): %s",keyword, token), this.getClass());
        } catch (IOException ex) {
            LoggerUtil.writeError(String.format("Error occured for\n Keyword: %s \n Token (UUID): %s \n Error Message: %s", keyword, token, ex), this.getClass());
        }
    }

    /**
     * Close TwitterStream if it's not null
     */
    public void closeTwitterStream() {
        if( twitterStream != null ) {
            twitterStream.cleanUp(); // shutdown internal stream consuming thread
            twitterStream.shutdown(); // Shuts down internal dispatcher thread shared by all TwitterStream instances.
        }
    }

    /**
     * Push the status to Socket based on the token
     * 
     * @param status
     * @param token
     */
    public void pushToSocket(Status status, String token) {
        String message =
                "<li>"
                        + "<div class='avatar pull-left'>"
                        + "<img style='height: 38px; width: 38px; border-radius: 999999px;' src='img/twitter.png' alt=''>"
                        + "</div>"
                        + "<div class='recent-content'>"
                        + "<div class='recent-meta'>&nbsp;</div>"
                        + "<div>" 
                        + "<font color='linear' style='font-weight: bold;'>" + "@" + status.getUser().getScreenName() 
                        + ": " + "</font>" + status.getText() 
                        + "</div>"
                        + "<div class='clearfix'></div>"
                        + "</div>"
                        + "</li>";
        this.messagingTemplate.convertAndSend( "/topic/"+token , message);
    }

    /**
     * Accessing the pushToPubSub method in a Thread
     * 
     * @param fullTopicName
     * @param status
     */
    public void pushToPubSubThread(final String fullTopicName, final Status status) {
        executorService.submit(new Runnable() {
            public void run() {
                try {
                    PubSubService.getInstance().pushToPubSub(fullTopicName, status);
                } catch (Exception ex) {
                    LoggerUtil.writeError(String.format("Error occured while pushing to PubSub: %s", ex), this.getClass());
                }
            }
        });
    }

}