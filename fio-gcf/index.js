const functions = require("@google-cloud/functions-framework");

functions.http("fio", (req, res) => {
  res.send("Hello, World");
});
