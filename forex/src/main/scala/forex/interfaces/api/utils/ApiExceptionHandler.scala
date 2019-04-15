package forex.interfaces.api.utils

import akka.http.scaladsl._
import com.typesafe.scalalogging.LazyLogging
import forex.processes._
import forex.processes.rates.messages.Error.DownstreamError
import forex.processes.rates.messages.Error.System

object ApiExceptionHandler extends LazyLogging {

  def apply(): server.ExceptionHandler =
    server.ExceptionHandler {
      case e: RatesError ⇒
        val (error, msg) = e match {
          case DownstreamError(underlying: Throwable) ⇒
            underlying → s"Downstream error in the rates process: ${underlying.getMessage}"
          case System(underlying: Throwable) ⇒
            (underlying, s"Something went wrong in the rates process: ${underlying.getMessage}")
          case err ⇒ (err, s"Something went wrong in the rates process: $err")
        }

        logger.error("Rates process error", error)

        ctx ⇒
          ctx.complete(msg)
      case t: Throwable ⇒
        ctx ⇒
          ctx.complete(s"Something else went wrong: $t")
    }

}
