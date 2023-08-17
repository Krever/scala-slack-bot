package local

import cats.effect.{IO, IOApp}
import cats.syntax.all.*
import com.comcast.ip4s.*
import fs2.io.net.Network
import org.http4s.dsl.Http4sDsl
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.middleware.{ErrorAction, ErrorHandling, Logger}
import org.http4s.{Http, HttpRoutes, Request, Response, Status}
import slackBotLambda.SlackHandler

object MyHttpServer extends IOApp.Simple {
  val run = runServer

  def runServer: IO[Nothing] = {
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"9876")
      .withHttpApp(Logger.httpApp(logHeaders = true, logBody = true)(errorLogging(routes.orNotFound)))
      .build
      .void
      .useForever
  }

  private def routes: HttpRoutes[IO] = {
    val dsl = new Http4sDsl[IO] {}
    import dsl.*
    HttpRoutes.of[IO] {
      case GET -> Root / "hello"        => Ok("world")
      case req @ POST -> Root / "lambda" =>
        for {
          input  <- requestToLambdaInput(req)
          result <- SlackHandler.run(input)
        } yield lambdaOutputToResponse(result)
    }
  }

  private def requestToLambdaInput(req: Request[IO]): IO[SlackHandler.Input] =
    req.as[String].map(SlackHandler.Input.apply)

  private def lambdaOutputToResponse(resp: SlackHandler.Output): Response[IO] = {
    Response().withEntity(resp.body)
  }

  private def errorLogging(route: Http[IO, IO]): Http[IO, IO] = ErrorHandling.Recover.total(
    ErrorAction.log(
      route,
      messageFailureLogAction = (t, msg) => IO(scribe.error(msg, t)),
      serviceErrorLogAction = (t, msg) => IO(scribe.error(msg, t)),
    ),
  )

}
