package slackBotLambda

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import net.exoego.facade.aws_lambda.*

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

object JsHandler {

  @JSExportTopLevel(name = "handler")
  val handler: js.Function2[APIGatewayProxyEvent, Context, js.Promise[APIGatewayProxyResult]] = {
    (event: APIGatewayProxyEvent, _: Context) =>
      import js.JSConverters.*
      implicit val ior: IORuntime = IORuntime.global
      implicit val ec: ExecutionContextExecutor = ExecutionContext.global

      Handler
        .run(Handler.Input(event.body))
        .map(out =>
          APIGatewayProxyResult(
            statusCode = out.code,
            body = out.body,
            headers = js.defined(js.Dictionary("Content-Type" -> "text/plain")),
          ),
        )
        .unsafeToFuture()
        .toJSPromise
  }

}
