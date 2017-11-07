package courses.restapi.http

import akka.http.scaladsl.server.Route
import courses.restapi.BaseServiceTest
import courses.restapi.core.service.{CourseService, StudentService}
import org.scalatest.mockito.MockitoSugar

class HttpRouteTest extends BaseServiceTest {

  "HttpRoute" when {

    "GET /healthcheck" should {

      "return 200 OK" in new Context {
        Get("/healthcheck") ~> httpRoute ~> check {
          responseAs[String] shouldBe "OK"
          status.intValue() shouldBe 200
        }
      }

    }

  }

  trait Context extends MockitoSugar{

    val courseService: CourseService = mock[CourseService]

    val studentService: StudentService = mock[StudentService]

    val httpRoute: Route = new HttpRoute( courseService,studentService ).route
  }

}
