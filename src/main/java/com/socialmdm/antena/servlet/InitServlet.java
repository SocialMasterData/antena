package com.socialmdm.antena.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.pubsub.Pubsub;
import com.google.api.services.pubsub.model.PushConfig;
import com.google.api.services.pubsub.model.Subscription;
import com.google.api.services.pubsub.model.Topic;
import com.socialmdm.antena.util.PubsubUtils;

/**
 * Entry point that initializes the application Pub/Sub resources.
 */
@SuppressWarnings("serial")
public class InitServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Pubsub client = PubsubUtils.getClient();
        System.out.println("client information:"+client.getBaseUrl().toString());
        String projectId = PubsubUtils.getProjectId();
        req.setAttribute("project", projectId);
        setupTopic(client);
        setupSubscription(client);

        req.setAttribute("topic", PubsubUtils.getAppTopicName());
        req.setAttribute("subscription", PubsubUtils.getAppSubscriptionName());
        req.setAttribute("subscriptionEndpoint",
                PubsubUtils.getAppEndpointUrl());
        RequestDispatcher rd = req.getRequestDispatcher("/WEB-INF/main.jsp");
        try {
            rd.forward(req, resp);
        } catch (ServletException e) {
            throw new IOException(e);
        }
    }
    
    private final  void setupTopic(Pubsub client)
            throws IOException {
        String fullName = String.format("projects/%s/topics/%s",
                PubsubUtils.getProjectId(),
                PubsubUtils.getAppTopicName());
        System.out.println("the topic full name:"+fullName);
        try {
        	Topic existingTopic = client.projects().topics().get(fullName).execute();
        	System.out.println("the exiting topic:"+existingTopic.getName());
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 404) {
                // Create the topic if it doesn't exist
            	Topic newTopic=client.projects().topics()
                        .create(fullName, new Topic())
                        .execute();
                System.out.println("the new topic:"+newTopic.getName());
            } else {
                throw e;
            }
        }
    }

    private final void setupSubscription(Pubsub client)
            throws IOException {
        String fullName = String.format("projects/%s/subscriptions/%s",
                PubsubUtils.getProjectId(),
                PubsubUtils.getAppSubscriptionName());
        try {
        	Subscription subscription = client.projects().subscriptions().get(fullName).execute();
        	System.out.println("the exiting subscription:"+subscription.getName());
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 404) {
            	System.out.println("sub set fulll name==="+fullName);
                // Create the subscription if it doesn't exist
                String fullTopicName = String.format("projects/%s/topics/%s",
                        PubsubUtils.getProjectId(),
                        PubsubUtils.getAppTopicName());
                System.out.println("inside the excepiton===full topic name==="+fullTopicName);
                PushConfig pushConfig = new PushConfig()
                        .setPushEndpoint(PubsubUtils.getAppEndpointUrl());
                System.out.println("the endpoint url is===="+PubsubUtils.getAppEndpointUrl());
                Subscription subscription = new Subscription()
                        .setTopic(fullTopicName)
                        .setPushConfig(pushConfig);
                client.projects().subscriptions()
                        .create(fullName, subscription)
                        .execute();
            }
        }        
    }

}
