var prompt = require('prompt');
var fs = require("fs");
var crypto = require("crypto");
prompt.start();
prompt.message = "";
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
    }
  }
}

prompt.get(options, function (err, result) {
  if (err) { console.log("on err"); return;}
  result.salt = crypto.randomBytes(128).toString("base64");
  fs.writeFile("config.json", JSON.stringify(result, null, "\t"), "utf-8", function(err) {
    if(err)
      console.log(err);
    else
      require("./configure.js")(result);
  })
});
