#!/usr/bin/env bash
set -eu

description=$(aws cloudformation describe-stacks --stack-name scala-slack-bot-lambda)

lambda_url=$(echo $description | jq '.Stacks[0].Outputs[0].OutputValue')
status=$(echo $description | jq '.Stacks[0].StackStatus')

echo "Status: " $status
echo "Lambda url: " $lambda_url