package slackBotLambda

import cats.effect.IO

object Handler {

  // TODO get real ApiGatewayProxyEventV2. Use feral?
  case class Input(body: String)
  case class Output(code: Int, body: String)


  def run(event: Input): IO[Output] = IO {
    Output(200, s"yup: ${event.body}")
  }


}

