package slackBotLambda

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.{APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse}
import scala.jdk.CollectionConverters._

import java.util.Base64

class JVMHandler extends RequestHandler[APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse] {
  override def handleRequest(input: APIGatewayV2HTTPEvent, context: Context): APIGatewayV2HTTPResponse = {
    implicit val ior: IORuntime = IORuntime.global

    scribe.info(s"Received: $input")

    val strBody = if (input.getIsBase64Encoded) new String(Base64.getDecoder.decode(input.getBody)) else input.getBody

    SlackHandler
      .run(SlackHandler.Input(strBody))
      .map(out =>
        APIGatewayV2HTTPResponse
          .builder()
          .withBody(out.body)
          .withStatusCode(200)
          .withHeaders(Map("Content-Type" -> "text/plain").asJava)
          .build(),
      )
      .unsafeRunSync()
  }
}
