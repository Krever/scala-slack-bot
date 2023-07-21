function_name="scala-slack-lambda-example-MyFunction-BvISKtzOIudg"

#aws lambda update-function-configuration --function-name ${function_name} \
#  --handler lambda.handler1

pushd lambda/.js/target/scala-3.3.0/scalajs-bundler/main/
zip fastopt.zip lambda-fastopt-bundle.js

aws lambda update-function-code --function-name ${function_name} \
  --zip-file fileb://fastopt.zip