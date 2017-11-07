package courses.restapi.http

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import courses.restapi.core.service.{CourseService, StudentService}
import courses.restapi.http.routes.{CourseRoute, StudentRoute}

import scala.concurrent.ExecutionContext

class HttpRoute( courseService: CourseService, studentService: StudentService)
               (implicit executionContext: ExecutionContext) {

  private val courseRouter = new CourseRoute(courseService)
  private val studentRouter = new StudentRoute(studentService)

  val route: Route =
     {
      pathPrefix("v1") {
        courseRouter.route ~
        studentRouter.route
      } ~
        pathPrefix("healthcheck") {
          get {
            complete("OK")
          }
        }
    }

}
