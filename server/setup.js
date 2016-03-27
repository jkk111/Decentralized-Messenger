/*
 * Setup Script
 * Simplifies setting up and configuring the server by generating the necessary configuration file.
 */
var prompt = require('prompt');
var fs = require("fs");
var crypto = require("crypto");
prompt.start();
prompt.message = "";

try {
  fs.accessSync("logs", fs.R_OK | fs.W_OK);
} catch(e) {
  fs.mkdirSync("logs");
}

prompt.delimiter = "";
console.log("Warning if there is an existing datbase with the same name, \nit will be destroyed!");

var options = {
  properties: {
    databaseType: {
      message: "Enter a valid database type. See valid database types",
      default: "mysql"
    },
    databaseUser: {
      message: "Enter the database username",
      default: "root"
    },
    databasePassword: {
      message: "Enter the database users password",
      default: "root"
    },
    databaseHost: {
      message: "Enter the database host",
      default: "localhost"
    },
    databasePort: {
      message: "Enter the database port",
      default: 3306
    },
    databaseName: {
      message: "Enter database name",
      default: "dm"
    },
    serverAdmin: {
      message: "Enter a username for the administrator",
      default: "admin"
    },
    serverPassword: {
      message: "Enter a password for the administrator",
      default: "PLEASE USE A GOOD PASSWORD"
    },
    serverPort: {
      message: "Choose a port to run http redirect on",
      default: 80
    },
    securePort: {
      message: "Choose a port to run https on",
      default: 443
    }
  }
}

prompt.get(options, function (err, result) {
  if (err) { console.log("on err"); return;}
  result.salt = crypto.randomBytes(256).toString("base64");
  fs.writeFile("config.json", JSON.stringify(result, null, "\t"), "utf-8", function(err) {
    if(err)
      console.log(err);
    else
      require("./configure.js")(result);
  })
});
