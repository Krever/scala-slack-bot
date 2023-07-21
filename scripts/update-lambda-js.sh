function_name="scala-slack-lambda-example-MyFunction-BvISKtzOIudg"

#aws lambda update-function-configuration --function-name ${function_name} \
#  --handler lambda.handler1

aws lambda update-function-code --function-name ${function_name} \
  --zip-file fileb://lambda/.js/target/universal/lambda-0.1.0-SNAPSHOT.zip