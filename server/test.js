var mysql = require("mysql");
var app = require("express")();
var crypto = require("crypto")

var conn = mysql.createConnection({
  user: "root",
  password: "root",
  host: "localhost",
  port: 3306,
  database: "dm"
});


function generateHash(pass) {
  return crypto.pbkdf2Sync(pass, "9mcCpxNrgSlbuhZwJ6bWSHt/nvGOO6ZabI+xCAtavLy5y0pEOBDDj2C7T4MR8egjLf5u0KOAFuHbfxlrd8lQdg==", 1000, 512, "sha512");
}

var pass = generateHash("hello world");
var q = "INSERT INTO users (username, password) VALUES(?, ?)";
conn.query(q, ["test", pass], function(err, res) {
  if(err)
    console.log(err);
  else
    console.log("success");
});
