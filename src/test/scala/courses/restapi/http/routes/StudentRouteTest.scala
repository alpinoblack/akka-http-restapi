package courses.restapi.http.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import com.mongodb.MongoException
import courses.restapi.BaseServiceTest
import courses.restapi.core.service.StudentService
import courses.restapi.core.storage.Student.StudentId
import courses.restapi.core.storage.{ListStudent, Student}
import courses.restapi.http.routes.StudentRoute._
import courses.restapi.util._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.mongodb.scala.Completed
import org.scalatest.mockito.MockitoSugar
import spray.json.DefaultJsonProtocol

import scala.concurrent.Future
import scala.util.{Failure, Success}

class StudentRouteTest extends BaseServiceTest with SprayJsonSupport with DefaultJsonProtocol {

  implicit val courseFormat = jsonFormat5(StudentDto)
  "StudentRoute" when {
    "GET /students" should {
      "return list of students" in new Context {
        Get("/students") ~> studentRoute ~> check {
          responseAs[Seq[StudentDto]] shouldEqual expectedResult
          status.intValue() shouldBe 200
        }
      }
    }

    "GET /students?avg=gt90" should {
      "list outstanding students" in new Context {
        Get("/students?min_avg=90") ~> studentRoute ~> check {
          responseAs[Seq[StudentDto]] shouldEqual outstandingStudents.map(_.toDto)
          status shouldBe OK
        }
      }
    }

    "POST /students" should {
      "create new Student" in new Context {
        val data = HttpEntity(
          ContentTypes.`application/json`,
          """|{
             |"firstName": "Saar",
             |"lastName": "Wexler",
             |"email": "saarwexler@gmail.com"
             |}""".stripMargin)

        Post("/students", data) ~> studentRoute ~> check {
          responseAs[String] shouldEqual """{"result": "Ok"}"""
          status.intValue() shouldBe 200
          verify(studentService).createStudent(argThat[Student](_.firstName == "Saar"))
        }
      }
    }

    "Get /students/:id" should {
      "get student by id" in new Context {
        Get(s"/students/${student1._id.toHexString}") ~> studentRoute ~> check {
          responseAs[StudentDto] shouldEqual student1.toDto
          status.intValue() shouldBe 200
        }
      }
      "return None when id does not exist" in new Context {
        Get("/students/blabla") ~> Route.seal(studentRoute) ~> check {
          status shouldBe StatusCodes.NotFound
        }
      }
    }
    "Post /students/:id/courses" should {
      "asssign a student to course" in new Context {
        val data = HttpEntity(
          ContentTypes.`application/json`,
          s"""{"courseId": "$courseId"}""")
        Post(s"/students/${student1._id.toHexString}/courses", data) ~> studentRoute ~> check {

          status shouldBe OK
          verify(studentService).assign(student1._id.toHexString, courseId)
        }
      }

      "get 400 if failure" in new Context {
        val data = HttpEntity(
          ContentTypes.`application/json`,
          s"""{"courseId": "blabla"}""")
        Post(s"/students/${student1._id.toHexString}/courses", data) ~> Route.seal(studentRoute) ~> check {
          status shouldBe StatusCodes.BadRequest
        }
      }
    }

    "Post /students/:id/courses/:courseId" should {
      "add score for course" in new Context {
        val data = HttpEntity(
          ContentTypes.`application/json`,
          """{"score": 96}""")
        Post(s"/students/${student1._id.toHexString}/courses/$courseId", data) ~> studentRoute ~> check {
          status shouldBe OK
        }
      }
      "reject 400 when fail" in new Context {
       val data =   HttpEntity(ContentTypes.`application/json`,
        """{"score": 96}""")
        Post(s"/students/badId/courses/$courseId", data) ~> studentRoute ~> check {
          status shouldBe StatusCodes.BadRequest
        }
      }
    }
    "Get  /students/top" should {
      "return the student with the highest avarage" in new Context {
        Get("/students/top") ~> studentRoute ~> check {
          responseAs[StudentDto] shouldEqual topStudent.toDto
          status shouldBe OK
        }
      }
    }
  }

  trait Context extends MockitoSugar {

    val studentService: StudentService = mock[StudentService]
    val studentRoute: Route = new StudentRoute(studentService).route

    val student1: Student = Student("Saar", "Wexler", "saarwexler@gmail.com")
    val student2: Student = Student("BB", "Netanyaho", "BB@gov.il")
    val topStudent: Student = Student("Albert", "Einstein", "top@student.com")
    val outstandingStudents = Seq(
      ListStudent(new StudentId, "Albert", "Einstein", "AE@AE.com", 99),
      ListStudent(new StudentId, "Saar", "Wexler", "saarwexler@gmail.com", 100))
    val seq = Seq(student1, student2).map(_.toListStudent)
    val expectedResult = Seq(student1, student2).map(_.toDto)
    val courseId = "A12334BCD"

    when(studentService.listStudents).thenReturn(Future(seq))
    when(studentService.getStudent(anyString)).thenReturn(Future(None))
    when(studentService.getStudent(student1._id.toHexString)).thenReturn(Future(Some(student1)))
    when(studentService.createStudent(any[Student])).thenReturn(Future(new Completed))
    when(studentService.assign(anyString, anyString)).thenReturn(Future(Failure(new Exception)))
    when(studentService.assign(student1._id.toHexString, courseId)).thenReturn(Future(Success()))
    when(studentService.addScore(anyString, anyString, anyInt)).thenReturn(Future(throw new MongoException("bla bla")))
    when(studentService.addScore(student1._id.toHexString, courseId, 96)).thenReturn(Future())
    when(studentService.listOutstandingStudents(90)).thenReturn(Future(outstandingStudents))
    when(studentService.topStudent).thenReturn(Future(topStudent))

  }

}
