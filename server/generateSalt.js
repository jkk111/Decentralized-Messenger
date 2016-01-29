var crypto = require("crypto");
var fs = require("fs");

fs.writeFileSync("hash.txt", crypto.randomBytes(64).toString("base64"), "utf-8");
