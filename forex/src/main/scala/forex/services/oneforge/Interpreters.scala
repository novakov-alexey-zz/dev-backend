package forex.services.oneforge

import java.nio.ByteBuffer
import java.time.{ Instant, OffsetDateTime }
import java.util.TimeZone

import cats.implicits._
import com.softwaremill.sttp._
import com.softwaremill.sttp.asynchttpclient.monix.AsyncHttpClientMonixBackend
import com.softwaremill.sttp.circe._
import com.typesafe.scalalogging.LazyLogging
import forex.config.OneForgeConfig
import forex.domain._
import forex.services.oneforge.Error.{ BadResponse, System }
import forex.services.oneforge.models.Quote
import monix.eval.Task
import monix.reactive.Observable
import org.atnos.eff._
import org.atnos.eff.addon.monix.task._

object Interpreters {
  def dummy[R](implicit m1: _task[R]): Algebra[Eff[R, ?]] =
    new Dummy[R]

  def live[R](oneForgeCfg: OneForgeConfig,
              backend: SttpBackend[Task, Observable[ByteBuffer]])(implicit m1: _task[R]): Algebra[Eff[R, ?]] =
    new Live[R](oneForgeCfg, backend)
}

final class Dummy[R] private[oneforge] (implicit m1: _task[R]) extends Algebra[Eff[R, ?]] {
  override def get(pair: Rate.Pair): Eff[R, Error Either Rate] =
    for {
      result ← fromTask(Task.now(Rate(pair, Price(BigDecimal(100)), Timestamp.now)))
    } yield Right(result)
}

final class Live[R] private[oneforge] (cfg: OneForgeConfig, backend: SttpBackend[Task, Observable[ByteBuffer]])(
  implicit m1: _task[R]
) extends Algebra[Eff[R, ?]]
    with LazyLogging {

  logger.debug(s"OneForge API URL: ${cfg.quotesApiPrefixUri}")
  implicit val sttpBackend = backend

  override def get(pair: Rate.Pair): Eff[R, Error Either Rate] =
    for {
      response ← fromTask {
        val uriForPair = s"${cfg.quotesApiPrefixUri}${cfg.apiKey}&pairs=${pair.show}"
        sttp.get(uri"$uriForPair").response(asJson[List[Quote]]).send()
      }
      result ← fromTask {
        val rate = response.body match {
          case Left(err) ⇒ Left(System(new RuntimeException(err)))
          case Right(r) ⇒
            r match {
              case Left(parseErr) ⇒ Left(BadResponse(new RuntimeException(parseErr.error)))
              case Right(q) ⇒
                q match {
                  case x :: Nil ⇒
                    Right(Rate(pair, Price(x.price), Timestamp(toOffsetTime(x.timestamp))))
                  case Nil ⇒ Left(BadResponse(new RuntimeException(s"Quote is empty, but expected one")))
                  case _ ⇒
                    Left(BadResponse(new RuntimeException(s"There are ${q.length}, but expected 1 quote only")))
                }
            }
        }

        Task.now(rate)
      }
    } yield result

  private def toOffsetTime(l: Long) =
    OffsetDateTime.ofInstant(Instant.ofEpochSecond(l), TimeZone.getDefault.toZoneId)
}
