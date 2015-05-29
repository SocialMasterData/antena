package com.socialmdm.util;

public final class Constants {
    
    private Constants() {}
    
    // WebSocket Streaming url
    public static String SIMPLE_MESSAGE_BORKER = "/topic";
    public static String DESTINATION_PREFIX = "/app";
    public static String ENDPOINT = "/hello";
    
    // PubSub Related
    public static String PROJECT_NAME_PREFIX = "projects/";
    public static String PROJECT_NAME = "socialmdm-antena/";
    public static String TOPIC_NAME_PREFIX = "topics/";
    public static String TOPIC_NAME_APPEND = "topic-";
    
    // Twitter Credentials
    public static String OAUTH_CONSUMER_KEY = "22ZmdXF53Ve4LKNUCkgHSm4jE";
    public static String OAUTH_CONSUMER_SECRET = "U5GnEFJitA2pfP3dzKo3niQNbbOt98BpZgGjtCHKIWFJkBNGHx";
    public static String OAUTH_ACCESS_TOKEN = "3152490259-XUJ0NnQiATzZp0JmaX93neajKeOzRjUf96tnNYq";
    public static String OAUTH_ACCESS_TOKEN_SECRET = "D88Fus4SkgyU1OFSa2X8YgiKckvQFbT7VDzy9iTrXZ8gY";
    
    // Prediction Related
    public static final String PROJECT_ID = "528732856399";
    public static final String APPLICATION_NAME = "socialmdm-antena";
    public static final String MODEL_ID = "spamdetection";
    public static final String STORAGE_DATA_LOCATION = "socialmdm-antena.appspot.com/language_id.txt";
    public static final String USER_CREDENTIAL_STORE_LOC = ".store/prediction_sample";
    public static final String USER_HOME = "user.home";

}