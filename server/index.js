/*
 * modularized main file for Decentralized-Messenger
 * Imports necessary modules, starts the admin panel,
 * Then creates multiple forks of the process to run the application
 */
module.exports = function(logger) {
  var fs = require("fs");
  var conf;
  try {
    conf = JSON.parse(fs.readFileSync(__dirname + "/config.json", "utf-8"));
  } catch(e) {
    console.log(e);
    console.error("Could not open configuration file!");
    process.exit();
  }
  var d = new Date();
  var express = require("express");
  var app = express();
  var bodyParser = require("body-parser");
  var cookieParser = require("cookie-parser");
  app.use(logger.route);
  app.use(bodyParser.urlencoded({ extended: true }));
  app.use(function(req, res, next) {
    res.set({
      "X-Frame-Options": "DENY",
      "Strict-Transport-Security": "max-age=31536000",
      "Schrodingers-Cat-State": "Unknown"
    });
    next();
  });
  app.use(cookieParser());
  app.use(express.static(__dirname + "/../web/www"));

  var ct = require(__dirname + "/crosstalk.js")(conf);
  logger.addListener(masterListener);
  var storage = require("./storage.js")(conf);
  var messaging = require("./messaging.js")(app, storage, ct, conf);

  function masterListener(key, data) {
    var el = {}
    el[key] = data;
    process.send(el);
  }
  return app;
}