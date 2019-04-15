package forex.main

import cats.Eval
import forex.config._
import forex.{services => s}
import forex.{processes => p}
import org.zalando.grafter.{Stop, StopResult}
import org.zalando.grafter.macros._

@readerOf[ApplicationConfig]
case class Processes(oneForgeCfg: OneForgeConfig) extends Stop {

  implicit final lazy val oneForge: s.OneForge[AppEffect] =
    s.OneForge.live[AppStack](oneForgeCfg)

  final val Rates = p.Rates[AppEffect]

  override def stop: Eval[StopResult] =
    StopResult.eval("Processes") {
      oneForge.close
    }
}
