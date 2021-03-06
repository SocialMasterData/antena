package com.socialmdm.service;

import java.io.IOException;

import twitter4j.Status;

import com.google.api.services.pubsub.Pubsub;
import com.google.api.services.pubsub.model.PublishRequest;
import com.google.api.services.pubsub.model.PublishResponse;
import com.google.api.services.pubsub.model.PubsubMessage;
import com.google.api.services.pubsub.model.Topic;
import com.google.common.collect.ImmutableList;
import com.socialmdm.util.Constants;
import com.socialmdm.util.LoggerUtil;
import com.socialmdm.util.PubsubUtils;

/**
 * PubSubService class for PubSub related operations
 * 
 * @author Naveen
 *
 */
public class PubSubService {
    
    
    private static PubSubService pubSubService;
    private PubSubService(){}
    public static synchronized PubSubService getInstance(){
        if(pubSubService == null){
            pubSubService = new PubSubService();
        }
        return pubSubService;
    }

    /**
     * Push to PubSub 
     * 
     * @param fullTopicName
     * @param status
     * @throws Exception
     */
    public void pushToPubSub(String fullTopicName, Status status) throws IOException {
        try {
            Pubsub client = PubsubUtils.getClient();
            PubsubMessage pubsubMessage = new PubsubMessage();
            pubsubMessage.encodeData( ("@" + status.getUser().getScreenName() + " - " + status.getText()).getBytes("UTF-8"));
            PublishRequest publishRequest = new PublishRequest();
            publishRequest.setMessages(ImmutableList.of(pubsubMessage));

            PublishResponse publishResponse = client.projects().topics()
                    .publish(fullTopicName, publishRequest)
                    .execute();

            LoggerUtil.writeInfo(String.format("Successfully pushed to topic %s with response message Id %s", fullTopicName, publishResponse.toString()), this.getClass());
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    /**
     * Create a topic in Pubsub
     * If the topic already exists, it'll return as ALREADY_EXISTS
     * 
     * @param keyword
     * @return
     * @throws IOException
     */
    public String createTopic(String keyword) {
        String fullTopicName = Constants.PROJECT_NAME_PREFIX + Constants.PROJECT_NAME + 
                                Constants.TOPIC_NAME_PREFIX + Constants.TOPIC_NAME_APPEND + keyword;
        try {
            Pubsub client = PubsubUtils.getClient();
            Topic topicToPublish = client.projects().topics()
                    .create( fullTopicName, new Topic())
                    .execute();
            LoggerUtil.writeInfo(String.format("A new topic created to publish with the name %s", topicToPublish.getName()), this.getClass());
        } catch (IOException e) {
            LoggerUtil.writeInfo(String.format("Error while creating topic %s \nMessage: %s",fullTopicName, e.getMessage()), this.getClass());
        }
        return fullTopicName;
    }

}