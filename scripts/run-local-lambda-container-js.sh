#!/usr/bin/env bash
set -xuev
# Starts official aws runtime container with lambda code inside

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

source_dir=$SCRIPT_DIR/../lambda/.js/target/scala-3.3.0/scalajs-bundler/main
handler=lambda-fastopt-bundle.myJsHandler

docker run --rm \
  -v ${source_dir}:/var/task:ro,delegated \
  -p 9876:8080 \
  -e SLACK_BOT_TOKEN \
  public.ecr.aws/lambda/nodejs:18.2023.07.19.03 \
  ${handler}