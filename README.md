# Scala Slack Bot Example

**THIS REPO IS WORK IN PROGRESS**

The goal of this repository is showcase how to build and _deploy_ a simple slack bot in scala.
It focuses on composing existing pieces together while exploring deployment options and developer experience that come
with them.

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
            <td colspan="3">❌ Could be added</td>
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
            <td rowspan="2">❌ Not possible</td>
            <td>✅</td>
            <td>✅ on AWS</td>
        </tr>
        <tr>
            <th>Scala JVM</th>
            <td colspan="2">❌ Could be added</td>
        </tr>
    </tbody>
</table>

## Run locally

#### Ngrok

Slack requires https traffic and current server implementation exposes only http endpoint.
And so you can't simply expose the port on your router. Instead, you can use ngrok to create a http proxy.

```
brew install --cask ngrok

ngrok http 9876 

# public url will be shown in the terminal. Alternatively you can get it with
curl -s localhost:4040/api/tunnels | jq -r '.tunnels[0].public_url'
```

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

### Run on fly.io

Deployment to fly.io requires account there

```bash
# auth
fly auth login
fly auth docker

# build
sbt run-localJVM/docker:publish

#deploy
fly deploy
```

### Alternatives

This bot can be deployed to localstack but with significant limitations:

* `AWS::ApiGatewayV2::Api` recource is not supported in localstack community edition, so this part would have to be done
  separately (outside of cloud formation)

## Acknowledgements

- [Yris-ops/slack-bot-aws-lambda](https://github.com/Yris-ops/slack-bot-aws-lambda)
- [bgahagan/scalajs-lambda.g8](https://github.com/bgahagan/scalajs-lambda.g8)
- [How to Create a Slack Bot using AWS Lambda in < 1 Hour](https://medium.com/glasswall-engineering/how-to-create-a-slack-bot-using-aws-lambda-in-1-hour-1dbc1b6f021c)
- https://github.com/typelevel/feral
- [Deploy Docker images on Fly.io free tier](https://medium.com/geekculture/deploy-docker-images-on-fly-io-free-tier-afbfb1d390b1)