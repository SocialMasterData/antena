package com.socialmdm.antena.servlet;

import java.io.IOException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.json.JsonParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.pubsub.model.PubsubMessage;
import com.socialmdm.antena.util.PubsubUtils;

/**
 * Processes incoming messages and sends them to the client web app.
 */
@SuppressWarnings("serial")
public class ReceiveMessageServlet extends HttpServlet {

	 @Override
	 @SuppressWarnings("unchecked")
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
		 System.out.println("calling the receive message servlet:==");
        // Validating unique subscription token before processing the message
		 //right now default token null, need to check and validate 
        String subscriptionToken = PubsubUtils.getAppSubscriptionName();
        /*if (!subscriptionToken.equals(req.getParameter("token"))) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().close();
            return;
        }*/
        System.out.println("THe subcription tokens are===="+subscriptionToken);
        ServletInputStream inputStream = req.getInputStream();

        // Parse the JSON message to the POJO model class
        JsonParser parser = JacksonFactory.getDefaultInstance()
                .createJsonParser(inputStream);
        parser.skipToKey("message");
        PubsubMessage message = parser.parseAndClose(PubsubMessage.class);
        
        // Base64-decode the data and work with it.
        String data = new String(message.decodeData(), "UTF-8");
        
        System.out.println("receive msg is end==="+data);
        // Acknowledge the message by returning a success code
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().close();
    }
}
