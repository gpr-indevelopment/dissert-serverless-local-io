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

export const lambdaHandler = async (event, context) => {
  const statusCode = 200;
  console.info("Received event:", event);
  if (event.body) {
    let eventBody = JSON.parse(event.body);
    if (eventBody.command) {
      return {
        statusCode,
        body: execSync("dd " + eventBody.command, { encoding: "utf-8" }),
      };
    }
  }
  return {
    statusCode,
    body: "Please provide a dd command inside the command attribute of the request body.",
  };
};
