package com.socialmdm.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import com.google.api.services.pubsub.Pubsub;
import com.google.api.services.pubsub.model.PublishRequest;
import com.google.api.services.pubsub.model.PublishResponse;
import com.google.api.services.pubsub.model.PubsubMessage;
import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;
import com.socialmdm.util.PubsubUtils;

@Path("/social")
public class SocialMediaService {

    @GET
    @Path("/getToken")
    public String getToken() {
        try {
            ChannelService channelService = ChannelServiceFactory.getChannelService();
            return channelService.createChannel( UUID.randomUUID().toString() );
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unused")
    @POST
    @Path("/search")
    public void searchSocialMedia(@CookieParam("token") String token,@FormParam("keyword") final String keyword) {
        System.out.println("before try");
        try {
            ConfigurationBuilder cb = new ConfigurationBuilder()
            .setDebugEnabled(true)
            .setOAuthConsumerKey("22ZmdXF53Ve4LKNUCkgHSm4jE")
            .setOAuthConsumerSecret("U5GnEFJitA2pfP3dzKo3niQNbbOt98BpZgGjtCHKIWFJkBNGHx")
            .setOAuthAccessToken("3152490259-XUJ0NnQiATzZp0JmaX93neajKeOzRjUf96tnNYq")
            .setOAuthAccessTokenSecret("D88Fus4SkgyU1OFSa2X8YgiKckvQFbT7VDzy9iTrXZ8gY");

            TwitterFactory tf = new TwitterFactory(cb.build());
            Twitter twitter = tf.getInstance();
            

            String keywords[] = keyword.split(",");


            ChannelService channelService = ChannelServiceFactory.getChannelService();
            
            long sinceId = 0;
            for(final String obj:keywords) {
                System.out.println("for entered");
                Query query;
                if(sinceId == 0) {
                    query = new Query(obj+" -filter:retweets");
                }else {
                    query = new Query(obj+" -filter:retweets");
                    query.setSinceId(sinceId);
                }
                QueryResult result;
                result = twitter.search(query);
                List<Status> tweets = result.getTweets();
                boolean firstOccurence = true;
                for (Status tweet : tweets) {
                    if(firstOccurence) {
                        sinceId = tweet.getId();
                        firstOccurence = false;
                    }
                    Thread.sleep(1000);
                    StringBuilder sb = new StringBuilder();
                    sb.append(
                            "<li>"
                                    + "<div class='avatar pull-left'>"
                                    + "<img style='height: 38px; width: 38px; border-radius: 999999px;' src='img/twitter.png' alt=''>"
                                    + "</div>"
                                    + "<div class='recent-content'>"
                                    + "<div class='recent-meta'>&nbsp;</div>"
                                    + "<div>" + "<font color='linear' style='font-weight: bold;'>" + "@" + tweet.getUser().getScreenName() + ": " + "</font>" 
                                    + tweet.getText() + "</div>"
                                    + "<div class='clearfix'></div>"
                                    + "</div>"
                                    + "</li>");
                    //sb.append("<br>@" + tweet.getUser().getScreenName() + " - " + tweet.getText());
                    channelService.sendMessage(new ChannelMessage(token, sb.toString()));
                }

                Pubsub client = PubsubUtils.getClient();
                if (!"".equals(obj)) {
                    String fullTopicName = String.format("projects/%s/topics/%s",
                            PubsubUtils.getProjectId(),
                            PubsubUtils.getAppTopicName());
                    for(Status tweet: tweets) {
                        PubsubMessage pubsubMessage = new PubsubMessage();
                        pubsubMessage.encodeData( ("@" + tweet.getUser().getScreenName() + " - " + tweet.getText()).getBytes("UTF-8"));
                        PublishRequest publishRequest = new PublishRequest();
                        publishRequest.setMessages(ImmutableList.of(pubsubMessage));

                        PublishResponse publishResponse = client.projects().topics()
                                .publish(fullTopicName, publishRequest)
                                .execute();
                    }
                }
            }
        } catch (NullPointerException ex) {
            System.out.println("error: "+ex.getMessage());
        } catch (IOException ex) {
            System.out.println("io: "+ex.getMessage());
        } catch (InterruptedException ex) {
            System.out.println("ie: "+ex.getMessage());
        } catch (TwitterException ex) {
            System.out.println("te: "+ex.getMessage()+"\n"+ex.getErrorMessage());
        }
    }
}