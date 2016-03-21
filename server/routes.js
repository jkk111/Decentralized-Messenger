module.exports = function(app, storage, config, ct) {
  // add new routes here!
  var auth = require("./auth.js")(app, config);
  var web = require("./web.js")(app);
  var messaging = require("./messaging.js")(app, storage, ct, config);
}
