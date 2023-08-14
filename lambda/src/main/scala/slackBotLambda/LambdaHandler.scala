package slackBotLambda

import cats.data.EitherT
import cats.effect.IO
import sttp.client4._

import java.net.URLDecoder
import scala.util.chaining.scalaUtilChainingOps

object LambdaHandler {

  // TODO get real ApiGatewayProxyEventV2. Use feral?
  case class Input(body: String)
  case class Output(code: Int, body: String)

  val backend = DefaultFutureBackend()

  def run(event: Input): IO[Output] = {
    val parsed                                = parseUrlParams(event.body)
    scribe.info(s"Received call: ${parsed}")
    val response: EitherT[IO, Output, Output] = for {
      botToken <- getBotToken
      response <- fetchUserInfo(parsed("user_id"), botToken)
      _         = scribe.info(s"Slack response: ${response}")
    } yield {
      val json     = ujson.read(response.body)
      val realName = json("user")("profile")("real_name").str
      Output(200, s"Hello $realName!")
    }
    response.merge
  }

  private def getBotToken: EitherT[IO, Output, String] = {
    EitherT.fromOptionF(cats.effect.std.Env[IO].get("SLACK_BOT_TOKEN"), Output(500, "Missing slack token"))
  }

  private def fetchUserInfo(userId: String, token: String): EitherT[IO, Output, Response[String]] = {
    val request = basicRequest
      .get(uri"https://slack.com/api/users.info?&user=${userId}")
      .auth
      .bearer(token)
      .response(asStringAlways)
    EitherT.liftF(IO.fromFuture(IO(request.send(backend))))
  }

  def parseUrlParams(body: String): Map[String, String] = {
    body
      .split("&")
      .collect { case s"$key=$value" => key -> URLDecoder.decode(value, "UTF-8") }
      .toMap
  }

}
