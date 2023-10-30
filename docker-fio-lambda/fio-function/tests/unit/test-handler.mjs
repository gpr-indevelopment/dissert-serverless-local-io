"use strict";

import { lambdaHandler } from "../../app.mjs";
import { expect } from "chai";

describe("Tests index", function () {
  it("verifies fio is installed", async () => {
    const result = await lambdaHandler({
      op: "fio-version",
    });

    expect(result).to.be.an("object");
    expect(result.statusCode).to.equal(200);
    expect(result.body).to.be.an("string");

    let response = result.body;

    expect(response).to.contain("fio");
  });
});
