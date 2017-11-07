package courses.restapi.http.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.Marshaller._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.MethodDirectives.{get, post}
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import courses.restapi.core.service.CourseService
import courses.restapi.core.storage.Course
import spray.json.DefaultJsonProtocol

import scala.concurrent.ExecutionContext

final case class CourseDto(_id: Option[String], name: String)

final case class FullCourseDto(_id: String, name: String, students: Seq[StudentDto])

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val courseFormat = jsonFormat2(CourseDto)
  implicit val studentFormat = jsonFormat4(StudentDto)
  implicit val fullCourseFormat = jsonFormat3(FullCourseDto)

}

class CourseRoute(courseService: CourseService)(implicit executionContext: ExecutionContext) extends JsonSupport {


  val route =
    pathPrefix("courses") {
      pathPrefix(Segment) { id =>
        pathEndOrSingleSlash {
          get {
            complete(courseService.GetStudentsForCourse(id))
          } ~
            put {
              complete("course $id updated")
            }
        }
      } ~
        pathEndOrSingleSlash {
          get {
            complete {
              courseService.getCourses.map(_ map (o => CourseDto(Some(o._id.toHexString), o.courseName)))
            }
          } ~
            post {
              entity(as[CourseDto]) { c =>
                complete {
                  val course = Course(c.name)
                  courseService.createCourse(course).map(_ => "Ok!")
                }
              }
            }
        }
    }
}