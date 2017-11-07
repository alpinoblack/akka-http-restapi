package courses.restapi.http.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.Marshaller._
import akka.http.scaladsl.model.StatusCodes.{BadRequest, OK}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.MethodDirectives.{get, post}
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import courses.restapi.core.service.StudentService
import courses.restapi.core.storage.{ListStudent, Student}
import spray.json.DefaultJsonProtocol

import scala.concurrent.ExecutionContext
import scala.util.Failure

case class StudentDto(id: Option[String], firstName: String, lastName: String, email: String, avgScore: Option[Double])

case class AssignCourseRequest(courseId: String)

case class AddScoreRequest(score: Int)

object StudentRoute {
  def apply(studentService: StudentService)(implicit executionContext: ExecutionContext): StudentRoute = new StudentRoute(studentService)

  implicit class StudentDtoExtension(student: StudentDto) {

    import student._

    def toModel = Student(firstName, lastName, email)
  }

  implicit class StudentExtension(student: Student) {

    import student._

    def toDto = StudentDto(Some(_id.toHexString), firstName, lastName, email, None)
  }

  implicit class ListStudentExtension(student: ListStudent) {

    import student._

    def toDto = StudentDto(Some(_id.toHexString), firstName, lastName, email, Some(avg))
  }

}

sealed trait StudentJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val studentFormat = jsonFormat5(StudentDto)
  implicit val assignCourseFormat = jsonFormat1(AssignCourseRequest)
  implicit val addScoreFormat = jsonFormat1(AddScoreRequest)
}

class StudentRoute(studentService: StudentService)(implicit executionContext: ExecutionContext) extends StudentJsonSupport {

  import StudentRoute._

  val rootRoute = pathEndOrSingleSlash {
    get {
      complete {
        for {
          students <- studentService.getStudents
        } yield students map (_.toDto)
      }
    } ~
      post {
        entity(as[StudentDto]) { studentDto =>
          complete {
            studentService.createStudent(studentDto.toModel) map (_ => """{"result": "Ok"}""")
          }
        }
      }
  }
  val route =
    pathPrefix("students") {
      pathPrefix(Segment) { studentId =>
        pathEndOrSingleSlash {
          put {
            complete("student $studentId updated")
          } ~
            get {
              rejectEmptyResponse {
                complete(studentService.getStudent(studentId) map (_ map (_.toDto)))
              }
            }
        } ~
          pathPrefix("courses") {
            pathEndOrSingleSlash {
              post {
                entity(as[AssignCourseRequest]) { request =>
                  complete(studentService.assign(studentId, request.courseId) map {
                    case Failure(_) => BadRequest -> """{"result": "Error"}"""
                    case _ => OK -> """{"result": "Ok"}"""
                  })
                }
              } ~
                get {
                  complete(s"courses for student $studentId")
                }
            } ~
              pathPrefix(Segment) { courseId =>
                post {
                  entity(as[AddScoreRequest]) { request =>
                    complete {
                      studentService.addScore(studentId, courseId, request.score)
                        .map(_ => OK -> """{"result": "Ok"}""")
                        .recover {
                          case ex: Exception => BadRequest ->
                            s"""|{
                                |"result": "Error",
                                | "message": "${ex.getMessage}"
                                | }""".stripMargin
                        }
                    }
                  }
                }
              }
          }
      } ~
      rootRoute
    }

}








