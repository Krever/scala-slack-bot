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
import slackBotLambda.LambdaHandler

object LocalHttpServer extends IOApp.Simple {
  val run = runServer

  def runServer: IO[Nothing] = {
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"9876")
      .withHttpApp(Logger.httpApp(logHeaders = true, logBody = true)(errorLogging(helloWorldRoutes.orNotFound)))
      .build
      .void
      .useForever
  }

  def helloWorldRoutes: HttpRoutes[IO] = {
    val dsl = new Http4sDsl[IO] {}
    import dsl.*
    HttpRoutes.of[IO] {
      case GET -> Root / "hello"        => Ok("world")
      case req @ POST -> Root / "lambda" =>
        for {
          input  <- requestToLambdaInput(req)
          result <- LambdaHandler.run(input)
        } yield lambdaOutputToResponse(result)

    }
  }

  private def requestToLambdaInput(req: Request[IO]): IO[LambdaHandler.Input] = req.as[String].map(LambdaHandler.Input.apply)

  private def lambdaOutputToResponse(resp: LambdaHandler.Output): Response[IO] = {
    println(resp)
    Response(Status(resp.code))
      .withEntity(resp.body)
  }

  private def errorLogging(route: Http[IO, IO]): Http[IO, IO] = ErrorHandling.Recover.total(
    ErrorAction.log(
      route,
      messageFailureLogAction = (t, msg) => IO(scribe.error(msg, t)),
      serviceErrorLogAction = (t, msg) => IO(scribe.error(msg, t)),
    ),
  )

}
