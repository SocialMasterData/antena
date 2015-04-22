
'use strict';

var pubsub = pubsub || angular.module('pubsub', []);

/**
 * PubsubController.
 *
 * @NgInject
 */
pubsub.PubsubController = function($http, $log, $timeout) {
  this.promise = null;
  this.logger = $log;
  this.http = $http;
  this.timeout = $timeout;
  this.interval = 1;
  this.isAutoUpdating = true;
  this.failCount = 0;
};

pubsub.PubsubController.MAX_FAILURE_COUNT = 3;

pubsub.PubsubController.TIMEOUT_MULTIPLIER = 1000;

/**
 * Sends a message
 *
 * @param {string} message
 */
pubsub.PubsubController.prototype.sendMessage = function(message) {
  var self = this;
  self.http({
    method: 'POST',
    url: '/send_message',
    data: 'message=' + encodeURIComponent(message),
    headers: {'Content-Type': 'application/x-www-form-urlencoded'}
  }).success(function(data, status) {
    self.message = null;
  }).error(function(data, status) {
    self.logger.error('Failed to send the message. Status: ' + status + '.');
  });
};