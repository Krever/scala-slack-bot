
# Starts official aws runtime container with lambda code inside

docker run --rm \
    -v $(pwd)/target/scala-3.3.0/scalajs-bundler/main:/var/task:ro,delegated \
    -p 9000:8080 \
    public.ecr.aws/lambda/nodejs:18 \
    lambda-fastopt-bundle.handler

