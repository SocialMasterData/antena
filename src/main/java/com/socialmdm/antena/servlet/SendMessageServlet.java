package com.socialmdm.antena.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.services.pubsub.Pubsub;
import com.google.api.services.pubsub.model.PublishRequest;
import com.google.api.services.pubsub.model.PublishResponse;
import com.google.api.services.pubsub.model.PubsubMessage;
import com.google.common.collect.ImmutableList;
import com.socialmdm.antena.util.PubsubUtils;

/**
 * Publishes messages to the application topic.
 */
@SuppressWarnings("serial")
public class SendMessageServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Pubsub client = PubsubUtils.getClient();
        String message = req.getParameter("message");
        System.out.println("coming here ==="+message);
        if (!"".equals(message)) {
            String fullTopicName = String.format("projects/%s/topics/%s",
                    PubsubUtils.getProjectId(),
                    PubsubUtils.getAppTopicName());
            PubsubMessage pubsubMessage = new PubsubMessage();
            pubsubMessage.encodeData(message.getBytes("UTF-8"));
            PublishRequest publishRequest = new PublishRequest();
            publishRequest.setMessages(ImmutableList.of(pubsubMessage));

            PublishResponse publishResponse = client.projects().topics()
                    .publish(fullTopicName, publishRequest)
                    .execute();            
            List<String> messageIds = publishResponse.getMessageIds();
            if (messageIds != null) {
                for (String messageId : messageIds) {
                    System.out.println("messageId: " + messageId);
                }
            }
        }
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        resp.getWriter().close();
    }
        
}
