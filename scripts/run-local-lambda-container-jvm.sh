#!/usr/bin/env bash
set -xuev

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

source_dir=$SCRIPT_DIR/../lambda/.jvm/target/universal/stage
handler=slackBotLambda.JVMHandler::handleRequest

docker run --rm \
  -v "${source_dir}":/var/task:ro,delegated \
  -p 9876:8080 \
  -e SLACK_BOT_TOKEN \
  public.ecr.aws/lambda/java:17.2023.08.02.10 \
  ${handler}