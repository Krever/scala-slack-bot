#!/usr/bin/env bash
set -xeuv

 SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

 aws cloudformation create-stack \
   --capabilities CAPABILITY_NAMED_IAM \
   --stack-name scala-slack-bot-lambda \
   --template-body file://"${SCRIPT_DIR}"/../resources/cf-stack.yaml \
   --parameters ParameterKey=BotToken,ParameterValue=${SLACK_BOT_TOKEN}


