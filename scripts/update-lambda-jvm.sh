#!/usr/bin/env bash
set -xuev

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

aws lambda update-function-configuration --function-name ScalaSlackLambda-lambda \
  --runtime java17 \
  --timeout 300 \
  --handler slackBotLambda.JVMHandler::handleRequest

aws lambda update-function-code --function-name ScalaSlackLambda-lambda \
  --zip-file fileb://${SCRIPT_DIR}/../lambda/.jvm/target/universal/lambda-0.1.0-SNAPSHOT.zip