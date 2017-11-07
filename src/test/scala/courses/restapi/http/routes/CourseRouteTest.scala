package courses.restapi.http.routes

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Route
import courses.restapi.BaseServiceTest
import courses.restapi.core.service.CourseService
import courses.restapi.core.storage.Course
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.mongodb.scala.Completed
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.Future

class CourseRouteTest extends BaseServiceTest with JsonSupport {


  "CourseRoute" when {
    "GET /courses" should {
      "return list of courses" in new Context {
        Get("/courses") ~> courseRoute ~> check {
          responseAs[Seq[CourseDto]] shouldEqual expectedResult
          status.intValue() shouldBe 200
        }
      }
    }
    "POST /courses" should {
      "create new course" in new Context {
        val data = HttpEntity(
          ContentTypes.`application/json`,
          """{"name": "English"}""")

        Post("/courses", data) ~> courseRoute ~> check {
          responseAs[String] shouldEqual "Ok!"
          status.intValue() shouldBe 200
          verify(courseService).createCourse(argThat[Course](_.courseName == "English"))
        }
      }
    }
    "GET /courses/:id" should {
      "return full course object" in new Context {
        Get(s"/courses/${course1._id.toHexString}") ~> courseRoute ~> check {
          responseAs[FullCourseDto] shouldEqual fullCourseDto
        }

      }
    }
  }

  trait Context extends MockitoSugar with JsonSupport {
    val courseService: CourseService = mock[CourseService]
    val courseRoute: Route = new CourseRoute(courseService).route

    val course1: Course = Course("course 1")
    val course2: Course = Course("course 2")
    val seq = Seq(course1, course2)
    val expectedResult = Seq(CourseDto(Some(course1._id.toString), course1.courseName), CourseDto(Some(course2._id.toString), course2.courseName))

    val student1 = StudentDto(Some("ABCD123"), "Saar", "Wexler", "saarwexler@gmail.com")

    val student2 = StudentDto(Some("1234ABCD"), "Yonit", "Levi", "Yonit@Channel2.co.il")

    val fullCourseDto = FullCourseDto(course1._id.toHexString, course1.courseName, Seq(student1, student2))


    when(courseService.getCourses).thenReturn(Future(seq))
    when(courseService.createCourse(any[Course])).thenReturn(Future(new Completed))
    when(courseService.GetStudentsForCourse(course1._id.toHexString)).thenReturn(Future(fullCourseDto))
  }

}
