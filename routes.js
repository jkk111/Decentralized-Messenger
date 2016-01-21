module.exports = function(app, config) {
  // add new routes here!
  var auth = require("./auth.js")(app);
  var web = require("./web.js")(app);
  var messaging = require("./messaging.js")(app, config);
}
