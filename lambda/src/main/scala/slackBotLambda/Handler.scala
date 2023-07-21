package slackBotLambda

import cats.effect.IO
import sttp.client4._

import java.net.URLDecoder

object Handler {

  // TODO get real ApiGatewayProxyEventV2. Use feral?
  case class Input(body: String)
  case class Output(code: Int, body: String)

  val backend = DefaultFutureBackend()

  def run(event: Input): IO[Output] = {
    val parsed   = parseUrlParams(event.body)
    scribe.info(s"Received call: ${parsed}")
    for {
      botToken <- cats.effect.std.Env[IO].get("SLACK_BOT_TOKEN")
      response <- fetchUserInfo(parsed("user_id"), botToken.get)
      _ = scribe.info(s"Slack response: ${response}")
    } yield {
      val json = ujson.read(response.body)
      val realName = json("user")("profile")("real_name").str
      Output(200, s"Hello $realName!")
    }
  }

  def fetchUserInfo(userId: String, token: String): IO[Response[String]] = {
    val request = basicRequest
      .get(uri"https://slack.com/api/users.info?&user=${userId}")
      .auth
      .bearer(token)
      .response(asStringAlways)
    IO.fromFuture(IO(request.send(backend)))
  }

  def parseUrlParams(body: String): Map[String, String] = {
    body
      .split("&") // Split on "&"
      .collect { case s"$key=$value" => key -> URLDecoder.decode(value, "UTF-8") }
      .toMap      // Get the whole as a Map
  }

}
