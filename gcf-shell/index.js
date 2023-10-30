const functions = require("@google-cloud/functions-framework");

functions.http("gcf-shell", (req, res) => {
  res.send("Hello, World");
});
