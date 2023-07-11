package slackBotLambda

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import net.exoego.facade.aws_lambda.*

import scala.concurrent.ExecutionContext
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

object Handler {

  def main(event: SlackEvent): IO[APIGatewayProxyResult] = IO {
    APIGatewayProxyResult(
      statusCode = 200,
      body = "hello " + event.body,
//      headers = js.defined(js.Dictionary("Content-Type" -> "text/plain"))
//      headers = js.defined(js.Dictionary.empty)
    )
  }

  @JSExportTopLevel(name="handler")
  val handler: js.Function2[APIGatewayProxyEvent, Context, js.Promise[APIGatewayProxyResult]] = { 
    (event: APIGatewayProxyEvent, _: Context) =>
      import js.JSConverters.*
      implicit val ior = IORuntime.global
      implicit val ec = ExecutionContext.global
      main(event.asInstanceOf[SlackEvent]).unsafeToFuture().toJSPromise
  }

}

trait SlackEvent extends js.Object {
  val body: String
}

