const functions = require("@google-cloud/functions-framework");
const { execSync } = require("child_process");

function tryCommand(command) {
  try {
    console.info("Trying command:", command);
    let result = execSync(command, { encoding: "utf-8" });
    console.info("Command result:", result);
    return result;
  } catch (e) {
    console.error(e);
  }
}

functions.http("gcf-list-all-commands", (req, res) => {
  console.info("Received request:", req);
  res.send(tryCommand("ls $(echo $PATH | tr ':' ' ')"));
});
