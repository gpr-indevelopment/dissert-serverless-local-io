/**
 *
 * Event doc: https://docs.aws.amazon.com/apigateway/latest/developerguide/set-up-lambda-proxy-integrations.html#api-gateway-simple-proxy-for-lambda-input-format
 * @param {Object} event - API Gateway Lambda Proxy Input Format
 *
 * Context doc: https://docs.aws.amazon.com/lambda/latest/dg/nodejs-prog-model-context.html
 * @param {Object} context
 *
 * Return doc: https://docs.aws.amazon.com/apigateway/latest/developerguide/set-up-lambda-proxy-integrations.html
 * @returns {Object} object - API Gateway Lambda Proxy Output Format
 *
 */
import { execSync } from "child_process";
import * as fs from "fs";

function removeExistingFioFile() {
  try {
    fs.unlinkSync("/tmp/fio.dat");
    console.info("Removed fio file");
  } catch (err) {
    if (err && err.code == "ENOENT") {
      console.info("Fio file doesn't exist, won't remove it.");
    } else {
      console.error("Error occurred while trying to remove fio file", err);
    }
  }
}

function extractFioArgs(event) {
  /*if (event.body) {
    return event.body;
  }
  if (event && !event.httpMethod) {
    return event;
  }*/
  return "--filename=/tmp/fio.dat --rw=write --direct=1 --bs=1M --ioengine=libaio --runtime=60 --numjobs=1 --time_based --group_reporting --name=seq_write --iodepth=1 --size=512M";
}

export const lambdaHandler = async (event, context) => {
  console.info("Received event:", event);
  removeExistingFioFile();
  let statusCode = 200;
  let result;
  if (event && event.op === "fio-version") {
    result = execSync("fio --version").toString();
  } else {
    let fioArgs = extractFioArgs(event);
    result = execSync("fio " + fioArgs).toString();
  }
  return {
    statusCode,
    body: result,
  };
};
