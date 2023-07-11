package local

import cats.effect.{IO, IOApp}
import cats.syntax.all.*
import com.comcast.ip4s.*
import fs2.io.net.Network
import net.exoego.facade.aws_lambda.APIGatewayProxyResult
import org.http4s.dsl.Http4sDsl
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.middleware.{ErrorAction, ErrorHandling, Logger}
import org.http4s.{Http, HttpRoutes, Request, Response, Status}
import org.slf4j.LoggerFactory
import slackBotLambda.{Handler, SlackEvent}

object LocalHttpServer extends IOApp.Simple {
  val run = runServer
  val log = LoggerFactory.getLogger(getClass.getName)

  def runServer: IO[Nothing] = {
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(Logger.httpApp(true, true)(errorLogging(helloWorldRoutes.orNotFound)))
      .build
      .void
      .useForever
  }

  def helloWorldRoutes: HttpRoutes[IO] = {
    val dsl = new Http4sDsl[IO] {}
    import dsl.*
    HttpRoutes.of[IO] {
      case GET -> Root / "hello"        => Ok("world")
      case req @ GET -> Root / "lambda" =>
        for {
          input  <- requestToLambdaInput(req)
          result <- Handler.main(input)
        } yield lambdaOutputToResponse(result)

    }
  }

  private def requestToLambdaInput(req: Request[IO]): IO[SlackEvent] = {
    for {
      strBody <- req.as[String]
    } yield new SlackEvent {
      override val body: String = strBody
    }
  }

  private def lambdaOutputToResponse(resp: APIGatewayProxyResult): Response[IO] = {
    println(resp)
    Response(Status(resp.statusCode.toInt))
      .withEntity(resp.body)
  }

  private def errorLogging(route: Http[IO, IO]): Http[IO, IO] = ErrorHandling.Recover.total(
    ErrorAction.log(
      route,
      messageFailureLogAction = (t, msg) => IO(log.error(msg, t)),
      serviceErrorLogAction = (t, msg) => IO(log.error(msg, t)),
    ),
  )

}