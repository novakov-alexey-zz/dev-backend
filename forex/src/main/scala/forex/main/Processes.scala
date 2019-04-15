package forex.main

import java.nio.ByteBuffer

import cats.Eval
import com.softwaremill.sttp.SttpBackend
import com.softwaremill.sttp.asynchttpclient.monix.AsyncHttpClientMonixBackend
import forex.config._
import forex.{services => s}
import forex.{processes => p}
import monix.eval.Task
import monix.reactive.Observable
import org.zalando.grafter.{Stop, StopResult}
import org.zalando.grafter.macros._

@readerOf[ApplicationConfig]
case class Processes(oneForgeCfg: OneForgeConfig) extends Stop {

  val backend: SttpBackend[Task, Observable[ByteBuffer]] = AsyncHttpClientMonixBackend()

  implicit final lazy val oneForge: s.OneForge[AppEffect] =
    s.OneForge.live[AppStack](oneForgeCfg, backend)

  final val Rates = p.Rates[AppEffect]

  override def stop: Eval[StopResult] =
    StopResult.eval("Processes") {
      backend.close()
    }
}
