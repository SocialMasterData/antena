package com.socialmdm.service;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.socialmdm.util.Constants;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * SocialMediaService for SocialMedia related operations
 * 
 * @author naveen
 *
 */
@Controller
public class SocialMediaService {

    Logger logger = Logger.getLogger(SocialMediaService.class);

    private SimpMessagingTemplate messagingTemplate;
    private static PubSubService pubSubService = setPubSubService();
    private static TwitterStream twitterStream = null;

    /**
     * 
     * @return
     */
    static PubSubService setPubSubService() {
        return new PubSubService();
    }

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
            logger.info(String.format("UUID: %s", uuid));
            response.getWriter().write(uuid);
            response.getWriter().close();
        } catch (IOException e) {
            logger.error(String.format("Error occured for\n UUID: %s\n Error Message: %s", uuid, e.getMessage()));
        }
    }

    /**
     * Search the twitter with the given keyword
     * 
     * @param keyword
     * @param token
     * @param response
     */
    @RequestMapping("/search")
    public void search(@PathParam("keyword") final String keyword,@PathParam("token") final String token, HttpServletResponse response) {
        try {
            boolean requiredStatus = false;
            if( keyword == null || keyword.equals("") ) {
                requiredStatus = true;
                logger.warn("Keyword cannot be null");
                response.getWriter().write("Keyword cannot be null");
                response.getWriter().close();
            }
            if( token == null || token.equals("") ) {
                requiredStatus = true;
                logger.warn("Token cannot be null");
                response.getWriter().write("Please reload your webpage");
                response.getWriter().close();
            }

            if(! requiredStatus) {
                logger.debug(String.format("Keyword: %s\n Token (UUID): %s", keyword, token));

                //Create Topic in PubSub
                final String fullTopicName = pubSubService.createTopic(keyword);
                
                // Shutdown the previous twitterStream if there is any, Twitter allows only one open connection from an account
                if( twitterStream != null ) {
                    twitterStream.cleanUp(); // shutdown internal stream consuming thread
                    twitterStream.shutdown(); // Shuts down internal dispatcher thread shared by all TwitterStream instances.
                }

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
                        pushToPubSubThread(fullTopicName, status);
                        pushToSocket(status, token);
                    }
                    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
                    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
                    public void onException(Exception ex) {
                        logger.error(String.format("Twiiter Stream exception %s",ex.getMessage()));
                    }
                    @Override
                    public void onScrubGeo(long arg0, long arg1) {
                        // TODO Auto-generated method stub
                    }
                    @Override
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

                logger.info(String.format("TwitterStream successfully started for\n Keyword: %s \n Token (UUID): %s",keyword, token));
            }
        } catch (Exception e) {
            logger.error(String.format("Error occured for\n Keyword: %s \n Token (UUID): %s \n Error Message: %s", keyword, token, e.getMessage()));
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
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    pubSubService.pushToPubSub(fullTopicName, status);
                } catch (Exception e) {
                    logger.error(String.format("Error occured while pushing to PubSub: %s", e.getMessage()));
                }
            }
        });
        thread.start();
    }

}