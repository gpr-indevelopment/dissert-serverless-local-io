const functions = require("@google-cloud/functions-framework");
const { execSync } = require("child_process");

functions.http("gcf-shell", (req, res) => {
  console.info("Received request:", req);
  res.send(execSync("sudo apt-get update", { encoding: "utf-8" }));
});
