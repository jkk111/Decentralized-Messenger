module.exports = function(app, storage, config) {
  // add new routes here!
  var auth = require("./auth.js")(app, config);
  var web = require("./web.js")(app);
  var messaging = require("./messaging.js")(app, storage, config);
}
