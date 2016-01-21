var express = require("express");
var fs = require("fs");
var app = express();
var request = require("request");
var conf;
try {
  conf = JSON.parse(fs.readFileSync("config.json", "utf-8"));
} catch(e) {
  console.log(e);
  console.error("Could not open configuration file!");
  process.exit();
}
var routes  = require("./routes.js")(app, conf);
app.listen(80);
