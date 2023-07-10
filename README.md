# Scala.js AWS Lambda Slack Bot Example

**THIS REPO IS WORK IN PROGRESS**

## Acknowledgements
- [Yris-ops/slack-bot-aws-lambda](https://github.com/Yris-ops/slack-bot-aws-lambda)
- [bgahagan/scalajs-lambda.g8](https://github.com/bgahagan/scalajs-lambda.g8) (required dependencies update)


## Run locally

### As lambda container
```
sbt fastOptJS::webpack
./scripts/run-local-lambda-container.sh
curl -XPOST "http://localhost:9000/2015-03-31/functions/function/invocations" -d '{"payload":"hello world!"}'
```