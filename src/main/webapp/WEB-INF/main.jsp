<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE HTML>

<html xmlns:ng="http://angularjs.org" ng-app="pubsub">
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title>Hello Cloud Pub/Sub</title>
  </head>

  <body ng-controller="pubsub.PubsubController as PubsubController">
    <h1>Hello Cloud Pub/Sub!</h1>

    <b>Project:</b> <%= request.getAttribute("project") %><br/>
    <b>Publishing to Topic:</b> <%= request.getAttribute("topic") %><br/>
    <b>Listening on Subscription:</b>  <%= request.getAttribute("subscription") %><br/>
    <b>Endpoint URL:</b> <%= request.getAttribute("subscriptionEndpoint") %><br/>

    <form>
      <textarea ng-model="PubsubController.message" name="message" rows="5" cols="100"></textarea>
    </form>
    <button ng-click="PubsubController.sendMessage(PubsubController.message)">Send message</button>
    <script src="//ajax.googleapis.com/ajax/libs/angularjs/1.2.15/angular.min.js"></script>
    <script src="/js/pubsub.js"></script>
  </body>
</html>
