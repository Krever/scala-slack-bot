# Scala Slack Bot Example

The goal of this repository is showcase how to build and _deploy_ a simple slack bot in scala.
It focuses on composing existing pieces together while exploring deployment options and developer experience that come
with them.

Checkout the [article](https://medium.com/@w.pitula/slack-bot-in-scala-and-12-ways-to-run-it-8b5f2d9f3524) for a proper tutorial.

At the moment this repository supports the following run scenarios

<table>
    <thead>
        <tr>
            <th rowspan="2" colspan="2"></th>
            <th colspan="2">Local</th>
            <th rowspan="2">Remote</th>
        </tr>
        <tr>
            <th>Native</th>
            <th>Docker</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <th rowspan="2">Service</th>
            <th>Scala.js</th>
            <td>✅</td>
            <td colspan="2">❌ Could be added</td>
        </tr>
        <tr>
            <th>Scala JVM</th>
            <td>✅</td>
            <td>✅</td>
            <td>✅ on fly.io</td>
        </tr>
        <tr>
            <th rowspan="2">Lambda</th>
            <th>Scala.js</th>
            <td rowspan="2">❌</td>
            <td>✅</td>
            <td>✅ on AWS</td>
        </tr>
        <tr>
            <th>Scala JVM</th>
            <td>✅</td>
            <td>✅</td>
        </tr>
    </tbody>
</table>

## Create Slack App

```bash
export WEBHOOK_URL='' # Webhook handler for the slack app
export APP_CONF_TOKEN='' # Can be generated at https://api.slack.com/apps
scripts/create-slack-app.sh
```

## Update the app
Whenever you want to update the webhook URL
```bash
export WEBHOOK_URL='' # Webhook handler for the slack app
export APP_CONF_TOKEN='' # Can be generated at https://api.slack.com/apps
scripts/update-slack-app.sh
```

## Run locally

### Ngrok

Slack requires https traffic and current server implementation exposes only http endpoint.
This means you can't simply expose the port on your router. Instead, you can use ngrok to create a http proxy.

```
brew install --cask ngrok

ngrok http 9876 

# public url will be shown in the terminal. Alternatively you can get it with
curl -s localhost:4040/api/tunnels | jq -r '.tunnels[0].public_url'
```

### Prerequisites

```bash
export SLACK_BOT_TOKEN='' # Can be generated at https://api.slack.com/apps/${APP_ID}/oauth 
```

### As a service
This will expose an http endpoint served by http4s.

#### JVM

```bash
> sbt 
sbt:scala-slack-bot> serviceJVM/reStart
## JVM debugger available on port 5005.
```

#### Scala.js

```bash
npm install source-map-support

sbt serviceJS/run
## nodejs debugger available on port 9229.
```

#### Verify
Both runtimes work the same way and can be called with:
```bash
curl localhost:9876/hello

curl -XPOST localhost:9876/lambda -d 'user_id=111' # need proper user id to work correctly
```

### As a lambda

This will start an aws-provided container with lambda runtime

#### Scala.js

```
sbt fastOptJS::webpack
./scripts/run-local-lambda-container-js.sh
```

#### JVM

```
sbt lambdaJVM/universal:stage
./scripts/run-local-lambda-container-jvm.sh
```

#### Verify

```bash
curl -X POST --location "http://localhost:9876/2015-03-31/functions/function/invocations" \
    -d '{
  "body":"user_id=111",
  "isBase64Encoded": false
}'
```

## Run remotely

### As a lambda on AWS Lambda

Requires aws account and cli setup

#### Set up

This will create all necessary object through CloudFormation Stack
```bash
scripts/deploy-cf-stack.sh
```

#### Scala.js

```bash
sbt lambdaJS/universal:packageBin
scripts/update-lambda-js.sh
```

#### JVM

```bash
sbt lambdaJVM/universal:stage
./scripts/run-local-lambda-container-jvm.sh
```

#### Verify

```bash
> scripts/get-cf-stack-status.sh
Status:  "CREATE_COMPLETE"
Lambda url:  "my_url"

curl -XPOST my_url -d 'user_id=111'
```


### As a service on fly.io

```bash
fly auth login
fly auth docker

sbt serviceJVM/docker:publish

fly deploy -c resources/fly.tom 
fly secrets set -c resources/fly.toml SLACK_BOT_TOKEN=$SLACK_BOT_TOKEN

flyurl=$(fly status -c resources/fly.toml --json | jq -r .Hostname)
curl -XPOST fly_url/lambda -d 'user_id=111'
```

### Alternatives

#### Localstack
This bot could be deployed to localstack but with significant limitations:

* `AWS::ApiGatewayV2::Api` resource is not supported in localstack community edition, so this part would have to be done
  separately (outside of cloud formation)

#### Scala native & GraalVM
PRs welcomed!

## Acknowledgements

- [Yris-ops/slack-bot-aws-lambda](https://github.com/Yris-ops/slack-bot-aws-lambda)
- [bgahagan/scalajs-lambda.g8](https://github.com/bgahagan/scalajs-lambda.g8)
- [How to Create a Slack Bot using AWS Lambda in < 1 Hour](https://medium.com/glasswall-engineering/how-to-create-a-slack-bot-using-aws-lambda-in-1-hour-1dbc1b6f021c)
- https://github.com/typelevel/feral
- [Deploy Docker images on Fly.io free tier](https://medium.com/geekculture/deploy-docker-images-on-fly-io-free-tier-afbfb1d390b1)