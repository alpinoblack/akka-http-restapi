package courses.restapi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import courses.restapi.core.service.{CourseService, StudentService}
import courses.restapi.core.storage.{MongoCourseStorage, MongoStudentStorage}
import courses.restapi.http.HttpRoute
import courses.restapi.utils.Config

import scala.concurrent.ExecutionContext

object Boot extends App {

  def startApplication() = {
    implicit val actorSystem = ActorSystem()
    implicit val executor: ExecutionContext = actorSystem.dispatcher
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    val config = Config.load()
    val studentStorage = new MongoStudentStorage()
    val courseService = new CourseService(new MongoCourseStorage() ,studentStorage)
    val httpRoute = new HttpRoute(courseService , new StudentService(studentStorage) )

    Http().bindAndHandle(httpRoute.route, config.http.host, config.http.port)
  }

  startApplication()

}
