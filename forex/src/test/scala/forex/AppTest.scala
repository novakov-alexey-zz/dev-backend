package forex

import java.nio.ByteBuffer

import cats.implicits._
import forex.domain.Currency._
import cats.syntax.either.catsSyntaxEither
import com.softwaremill.sttp.SttpBackend
import com.softwaremill.sttp.asynchttpclient.monix.AsyncHttpClientMonixBackend
import com.softwaremill.sttp.circe.asJson
import com.softwaremill.sttp.quick._
import com.typesafe.scalalogging.LazyLogging
import forex.domain.{ Currency, Price, Timestamp }
import forex.interfaces.api.rates.Protocol.GetApiResponse
import forex.main.Application
import io.circe.Decoder
import io.circe.generic.extras.wrapped._
import io.circe.generic.semiauto.deriveDecoder
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import org.scalatest.{ BeforeAndAfterAll, EitherValues, Matchers, WordSpec }
import org.zalando.grafter.Rewriter

import scala.concurrent.Await
import scala.concurrent.duration._

class AppTest extends WordSpec with Matchers with BeforeAndAfterAll with LazyLogging with EitherValues {

  var app: Option[Application] = None
  implicit val backend: SttpBackend[Task, Observable[ByteBuffer]] = AsyncHttpClientMonixBackend()

  implicit val currencyEncoder: Decoder[Currency] = Decoder.decodeString.emap { str ⇒
    Either.catchNonFatal(Currency.fromString(str)).leftMap(t ⇒ s"Failed to parse Currency instance: $t")
  }
  implicit val priceEncoder: Decoder[Price] = deriveUnwrappedDecoder[Price]
  implicit val timestampEncoder: Decoder[Timestamp] = deriveUnwrappedDecoder[Timestamp]
  implicit val responseEncoder: Decoder[GetApiResponse] = deriveDecoder[GetApiResponse]

  override def beforeAll(): Unit =
    app = Main.startApp

  "RatesRoutes" should {
    "return Forex quotes for a currency pair" in {
      //given
      app should be('defined)
      val instance = app.get
      val host = instance.api.config.interface
      val port = instance.api.config.port
      val from: Currency = Currency.USD
      val to: Currency = Currency.EUR

      //when
      val f = sttp
        .get(uri"http://$host:$port/?from=${from.show}&to=${to.show}")
        .response(asJson[GetApiResponse])
        .send()
        .runToFuture
      val response = Await.result(f, 30.seconds)
      println("response:\n" + response.body)

      //then
      response.code should be(200)
      response.body should be('right)
      response.body.right.get should be('right)

      val quote = response.body.right.get.right.get

      quote.from should be(from)
      quote.to should be(to)
    }
  }

  override def afterAll(): Unit = {
    backend.close()
    app.foreach(a ⇒ Rewriter.stopAll(a).value)
  }
}
