# Scala.js AWS Lambda Slack Bot Example

**THIS REPO IS WORK IN PROGRESS**

## Acknowledgements

- [Yris-ops/slack-bot-aws-lambda](https://github.com/Yris-ops/slack-bot-aws-lambda)
- [bgahagan/scalajs-lambda.g8](https://github.com/bgahagan/scalajs-lambda.g8)
- [How to Create a Slack Bot using AWS Lambda in < 1 Hour](https://medium.com/glasswall-engineering/how-to-create-a-slack-bot-using-aws-lambda-in-1-hour-1dbc1b6f021c)
- https://github.com/typelevel/feral

## Run locally

### As lambda container

```
sbt fastOptJS::webpack
./scripts/run-local-lambda-container.sh
curl -XPOST "http://localhost:9000/2015-03-31/functions/function/invocations" -d '{"payload":"hello world!"}'
```

### Through local server
This will expose your lambda through a custom http endpoint served by http4s.

#### Scala.js

```
# npm install source-map-support

sbt local-runJS/run

## nodejs debugger available on port 9229. It's compatible with Intellij "Attach to Node.js/Chrome" debug option
```

#### JVM

```
# npm install source-map-support

sbt local-runJVM/reStart

## JVM debugger available on port 5005. It's compatible with Intellij "Remote JVM Debug" debug option
```

#### Ngrok

Slack requires https traffic and current server implementation exposes only http endpoint. 
So you can't simply expose the port on your router. Instead, we will use ngrok to create an http proxy

```
ngrok http 8080

# public url will be shown in the terminal. Alternatively you can get it with
curl -s localhost:4040/api/tunnels | jq -r '.tunnels[0].public_url'
```

### Alternatives

This bot can be deployed to localstack but with significant limitations:

* `AWS::ApiGatewayV2::Api` recource is not supported in localstack community edition, so this part would have to be done
  separately (outside of cloud formation)
