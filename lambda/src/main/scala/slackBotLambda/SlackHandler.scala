package slackBotLambda

import cats.data.EitherT
import cats.effect.{IO, Outcome}
import sttp.client4.*
import ujson.Value.Value

import java.net.URLDecoder
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

object SlackHandler {

  case class Input(body: String)
  case class Output(body: String)

  val backend = DefaultFutureBackend()

  def run(event: Input): IO[Output] = {
    scribe.info(s"Received call: ${event.body}")
    val response: EitherT[IO, Output, Output] = for {
      userId   <- getUserId(event.body)
      botToken <- getBotToken
      response <- fetchUserInfo(userId, botToken)
      _         = scribe.info(s"Slack response: ${response}")
    } yield {
      val realName = response("user")("profile")("real_name").str
      Output(s"Hello $realName!")
    }
    response.merge
  }

  private def getBotToken: EitherT[IO, Output, String] = {
    EitherT.fromOptionF(cats.effect.std.Env[IO].get("SLACK_BOT_TOKEN"), Output("Missing slack token"))
  }

  private def getUserId(requestBody: String): EitherT[IO, Output, String] = {
    val parsed = parseUrlParams(requestBody)
    EitherT.fromOption(parsed.get("user_id"), Output("Malformed request"))
  }

  private def fetchUserInfo(userId: String, token: String): EitherT[IO, Output, ujson.Value] = {
    val request = basicRequest
      .get(uri"https://slack.com/api/users.info?&user=${userId}")
      .auth
      .bearer(token)
      .response(asStringAlways)
    for {
      response <- EitherT.liftF(IO.fromFuture(IO(request.send(backend))))
      parsed   <- EitherT.fromEither(
                    Try(ujson.read(response.body)).toEither.left
                      .map(e => Output(e.getMessage)),
                  )
      _        <- EitherT.fromEither(
                    Try(parsed("ok").bool).toEither.left
                      .map(e => Output(e.getMessage))
                      .flatMap(isOk => Either.cond(isOk, (), Output(s"Failure in communicating with slack: ${response.body}"))),
                  )
    } yield ujson.read(response.body)
  }

  def parseUrlParams(body: String): Map[String, String] = {
    body
      .split("&")
      .collect { case s"$key=$value" => key -> URLDecoder.decode(value, "UTF-8") }
      .toMap
  }

}
