package forex.services.oneforge

import java.time.{Instant, OffsetDateTime}
import java.util.TimeZone

import cats.implicits._
import forex.domain._
import monix.eval.Task
import org.atnos.eff._
import org.atnos.eff.all._
import org.atnos.eff.addon.monix.task._
import com.softwaremill.sttp._
import com.softwaremill.sttp.circe._
import com.softwaremill.sttp.asynchttpclient.monix.AsyncHttpClientMonixBackend
import forex.services.oneforge.Error.{BadResponse, System}
import forex.services.oneforge.models.Quote
import io.circe

object Interpreters {
  def dummy[R](implicit m1: _task[R]): Algebra[Eff[R, ?]] =
    new Dummy[R]

  def live[R](implicit m1: _task[R]): Algebra[Eff[R, ?]] =
    new Live[R]
}

final class Dummy[R] private[oneforge] (implicit m1: _task[R]) extends Algebra[Eff[R, ?]] {
  override def get(pair: Rate.Pair): Eff[R, Error Either Rate] =
    for {
      result ← fromTask(Task.now(Rate(pair, Price(BigDecimal(100)), Timestamp.now)))
    } yield Right(result)
}

final class Live[R] private[oneforge] (implicit m1: _task[R]) extends Algebra[Eff[R, ?]] {
  //TODO: stop backend on app shutdown
  implicit val backend = AsyncHttpClientMonixBackend()
  //TODO: move to config
  val oneForgeQuotesUri = "https://forex.1forge.com/1.0.3/quotes?api_key=iSvTDXCMIPjuIJZTSOVP9RUs0Dkvw83K"

  override def get(pair: Rate.Pair): Eff[R, Error Either Rate] =
    for {
      response ← fromTask {
        val uriForPair = s"$oneForgeQuotesUri&pairs=${pair.show}"
        sttp.get(uri"$uriForPair").response(asJson[List[Quote]]).send()
      }
      result ← fromTask {
        println("body:")
        println(response.body)
        val rate = response.body match {
          case Left(err) ⇒ Left(System(new RuntimeException(err)))
          case Right(r) ⇒
            r match {
              case Left(parseErr) ⇒ Left(BadResponse(new RuntimeException(parseErr.message)))
              case Right(q) ⇒
                q match {
                  case x :: Nil ⇒
                    val timestamp =
                      OffsetDateTime.ofInstant(Instant.ofEpochSecond(x.timestamp), TimeZone.getDefault.toZoneId)
                    Right(Rate(pair, Price(x.price), Timestamp(timestamp)))
                  case Nil ⇒ Left(BadResponse(new RuntimeException(s"Quote is empty, but expected one")))
                  case _ ⇒
                    Left(BadResponse(new RuntimeException(s"There are ${q.length}, but expected 1 quote only")))
                }
            }
        }

        Task.now(rate)
      }
    } yield result
}
