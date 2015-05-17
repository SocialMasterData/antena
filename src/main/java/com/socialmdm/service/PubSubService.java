package com.socialmdm.service;

import java.util.List;

import twitter4j.Status;

import com.google.api.services.pubsub.Pubsub;
import com.google.api.services.pubsub.model.PublishRequest;
import com.google.api.services.pubsub.model.PublishResponse;
import com.google.api.services.pubsub.model.PubsubMessage;
import com.google.api.services.pubsub.model.Topic;
import com.google.common.collect.ImmutableList;
import com.socialmdm.util.PubsubUtils;

public class PubSubService {
    
    public void pushToPubSub(String topic, List<Status> tweets) throws Exception {
        System.out.println("pubsub entered");
        Pubsub client = PubsubUtils.getClient();
        if (!"".equals(topic)) {
            System.out.println("if entered");
            Topic newTopic = client.projects().topics()
                    .create("projects/myproject/topics/"+topic, new Topic())
                    .execute();
            System.out.println("Created: " + newTopic.getName());
            String fullTopicName = String.format("projects/%s/topics/%s",
                    PubsubUtils.getProjectId(),
                    topic);
            System.out.println("fulltopicname===== "+fullTopicName);
            for(Status tweet: tweets) {
                System.out.println("pubsub for entered");
                PubsubMessage pubsubMessage = new PubsubMessage();
                pubsubMessage.encodeData( ("@" + tweet.getUser().getScreenName() + " - " + tweet.getText()).getBytes("UTF-8"));
                PublishRequest publishRequest = new PublishRequest();
                publishRequest.setMessages(ImmutableList.of(pubsubMessage));

                PublishResponse publishResponse = client.projects().topics()
                        .publish(fullTopicName, publishRequest)
                        .execute();
                System.out.println("publish response: "+publishResponse.toString());
            }
        }
    }

}