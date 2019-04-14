package forex.interfaces.api.utils

import akka.http.scaladsl._
import com.typesafe.scalalogging.LazyLogging
import forex.processes._

object ApiExceptionHandler extends LazyLogging {

  def apply(): server.ExceptionHandler =
    server.ExceptionHandler {
      case e: RatesError ⇒
        ctx ⇒
          logger.error(s"rates error: ${e.getMessage}")
          ctx.complete("Something went wrong in the rates process")
      case _: Throwable ⇒
        ctx ⇒
          ctx.complete("Something else went wrong")
    }

}
