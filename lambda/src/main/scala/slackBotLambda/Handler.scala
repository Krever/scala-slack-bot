package slackBotLambda

import cats.effect.IO
import sttp.client3.SimpleHttpClient
import sttp.client3._

import java.net.URLDecoder

object Handler {

  // TODO get real ApiGatewayProxyEventV2. Use feral?
  case class Input(body: String)
  case class Output(code: Int, body: String)

  def run(event: Input): IO[Output] = IO {
    val parsed   = parseUrlParams(event.body)
    scribe.info(s"Received call: ${parsed}")
    val client   = SimpleHttpClient()
    val token    = scala.sys.env("SLACK_BOT_TOKEN")
    val request  = basicRequest
      .get(uri"https://slack.com/api/users.info?&user=${parsed("user_id")}")
      .auth
      .bearer(token)
      .response(asStringAlways)
    val response = client.send(request)
    val json     = ujson.read(response.body)
    val realName = json("user")("profile")("real_name").str
    Output(200, s"Hello $realName!")
  }

  def parseUrlParams(body: String): Map[String, String] = {
    body
      .split("&") // Split on "&"
      .collect { case s"$key=$value" => key -> URLDecoder.decode(value, "UTF-8") }
      .toMap      // Get the whole as a Map
  }

}
