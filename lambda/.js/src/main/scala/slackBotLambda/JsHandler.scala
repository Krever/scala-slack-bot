package slackBotLambda

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import net.exoego.facade.aws_lambda.*

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.JSExportTopLevel
import java.util.Base64

object JsHandler {

  @JSExportTopLevel(name = "handler1")
  val handler: js.Function2[APIGatewayProxyEvent, Context, js.Promise[APIGatewayProxyResult]] = {
    (event: APIGatewayProxyEvent, _: Context) =>
      import js.JSConverters.*
      implicit val ior: IORuntime = IORuntime.global
      implicit val ec: ExecutionContextExecutor = ExecutionContext.global

      scribe.info(s"Received: ${JSON.stringify(event, null, 2)}")

      val strBody = if(event.isBase64Encoded) new String(Base64.getDecoder.decode(event.body)) else event.body

      LambdaHandler
        .run(LambdaHandler.Input(strBody))
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
