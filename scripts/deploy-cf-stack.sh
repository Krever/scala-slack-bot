 read -sp 'BotToken: ' bot_token

 aws cloudformation create-stack \
   --capabilities CAPABILITY_NAMED_IAM \
   --stack-name 'scala-slack-lambda-example' \
   --template-body file://resources/cf-stack.yaml \
   --parameters ParameterKey=BotToken,ParameterValue=${bot_token}


aws cloudformation describe-stacks --stack-name 'scala-slack-lambda-example' | jq '.Stacks[0].Outputs'