package forex

import cats.Eval
import com.typesafe.scalalogging._
import forex.config._
import forex.main._
import org.zalando.grafter._

object Main extends App with LazyLogging {

  def startApp: Option[Application] = pureconfig.loadConfig[ApplicationConfig]("app") match {
    case Left(errors) ⇒
      logger.error(s"Errors loading the configuration:\n${errors.toList.mkString("- ", "\n- ", "")}")
      None
    case Right(applicationConfig) ⇒
      val application = configure[Application](applicationConfig).configure()

      Rewriter
        .startAll(application)
        .flatMap {
          case results if results.exists(!_.success) ⇒
            logger.error(toStartErrorString(results))
            Rewriter.stopAll(application).map(_ ⇒ ())
            Eval.now(None)
          case results ⇒
            logger.info(toStartSuccessString(results))
            Eval.now {
              Some(application)
            }
        }
        .value
  }

  val app = startApp

  sys.addShutdownHook {
    app.foreach(Rewriter.stopAll(_))
  }

}
