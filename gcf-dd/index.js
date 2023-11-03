const functions = require("@google-cloud/functions-framework");
const { execSync } = require("child_process");

functions.http("gcf-dd", (req, res) => {
  console.info("Received request:", req);
  if (req.body && req.body.command) {
    res.send(execSync("dd " + req.body.command, { encoding: "utf-8" }));
    return;
  }
  res.send(
    "Please provide a dd command inside the command attribute of the request body."
  );
});
