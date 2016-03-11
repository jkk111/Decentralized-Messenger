
/*
  Builds and configures a database for a mysql powered server
*/

var fs = require("fs");
var mysql = require("mysql");
var username, pass, dbaddr, dbname, dbport, conn;
if(!module.parent) {
  fs.readFile("config.json", "utf-8", function(err, data) {
    if(err) {
      console.log(err);
      process.exit();
    }
    var data = JSON.parse(data);
    username = data.databaseUser;
    pass = data.databasePassword;
    dbaddr = data.databaseHost;
    dbname = data.databaseName;
    dbport = data.databasePort || 3306;
    buildDatabase();
  });
}

module.exports = function(data) {
  username = data.databaseUser;
  pass = data.databasePassword;
  dbaddr = data.databaseHost;
  dbname = data.databaseName;
  dbport = data.databasePort || 3306;
  buildDatabase(function() {
    // can't quite recall what my goal was here, probably gonna do something when everything was initialized, 3am coding at its finest
  });
}

function buildDatabase() {
  console.log("Building database: %s, user: %s, pass: %s, database: %s", dbname, username, pass, dbaddr);
  conn = mysql.createConnection({
    host: dbaddr,
    user: username,
    password: pass,
    port: dbport,
    multipleStatements: true
  });

  conn.connect();

  conn.query("DROP DATABASE IF EXISTS " + dbname + "; CREATE DATABASE `" + dbname + "`", function(err, rows, fields) {
    if(err) {
      console.log(err);
      process.exit();
    }
    else {
      conn.end();
      conn = mysql.createConnection({
        host: dbaddr,
        user: username,
        password: pass,
        port: dbport,
        database: dbname,
        multipleStatements: true
      });
      buildTables();
    }
  });
}

function buildKeys(cb) {
  // TODO (john): implement key storage
  console.log("Coming soon to a messenger near you. Encryption!");
  cb();
}

// Am I in callback hell yet?
function buildTables() {
  var table = "CREATE TABLE users (id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY, \
                                   username VARCHAR(100) UNIQUE NOT NULL, \
                                   password BLOB NOT NULL, \
                                   lastActive TIMESTAMP DEFAULT NOW(), \
                                   isManaged BOOLEAN DEFAULT TRUE); \
               CREATE TABLE messages (id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY, \
                                      sender INT UNSIGNED NOT NULL, \
                                      recipient INT UNSIGNED NOT NULL, \
                                      message TEXT(60000) NOT NULL); \
               CREATE TABLE tokens (user INT UNSIGNED NOT NULL, \
                                    token VARCHAR(100) NOT NULL, \
                                    expiry DATETIME NOT NULL); \
               CREATE TABLE friends (id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY, \
                                     user1 INT UNSIGNED NOT NULL, \
                                     user2 INT UNSIGNED NOT NULL, \
                                     secret TEXT(60000) NOT NULL, \
                                     pending BOOLEAN DEFAULT TRUE); \
                   CREATE TABLE keypairs (id INT UNSIGNED UNIQUE, \
                                      private TEXT(60000) NOT NULL, \
                                      public TEXT(60000) NOT NULL);";
  conn.query(table, function(err, results) {
    if(err)
      console.log(err);
    else
      console.log("built successfully");
    conn.end();
  })
}
