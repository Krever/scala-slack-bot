
# Starts official aws runtime container with lambda code inside

# sbt lambdaJS/fastOptJS::webpack

#source_dir=$(pwd)/lambda/.js/target/scala-3.3.0/scalajs-bundler/main
source_dir=$(pwd)/lambda/.js/target/universal/stage
#handler=lambda-fastopt-bundle.handler1
#handler=lambda-opt-bundle.handler1
handler=lambda.handler1

docker run --rm \
    -v ${source_dir}:/var/task:ro,delegated \
    -p 9000:8080 \
    -e SLACK_BOT_TOKEN=xx \
    public.ecr.aws/lambda/nodejs:18.2023.07.19.03 \
    ${handler}

# curl -XPOST "http://localhost:9000/2015-03-31/functions/function/invocations" -d '{"body":"hello world!"}'