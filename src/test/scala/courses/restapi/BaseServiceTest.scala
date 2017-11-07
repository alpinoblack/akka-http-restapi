package courses.restapi

import akka.http.scaladsl.testkit.ScalatestRouteTest
import scala.concurrent.duration._

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.{Await, Future}

trait BaseServiceTest extends WordSpec with Matchers with ScalatestRouteTest with MockitoSugar {

  def awaitForResult[T](futureResult: Future[T]): T =
    Await.result(futureResult, 10 seconds)

}
