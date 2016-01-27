/*
  Static web server for development of the web front end.
*/
var express = require("express");
var app = express();
app.use(express.static("www"));
app.listen(80);
