package courses.restapi.core.service

import courses.restapi.core.CourseId
import courses.restapi.core.storage.{Course, CourseStorage, StudentStorage}
import courses.restapi.http.routes.FullCourseDto
import org.mongodb.scala.Completed
import courses.restapi.http.routes.StudentRoute._
import org.bson.types.ObjectId

import scala.concurrent.{ExecutionContext, Future}

class CourseService(courseStorage: CourseStorage, studentStorage: StudentStorage)(implicit executionContext: ExecutionContext) {
  def GetStudentsForCourse(courseId: String): Future[FullCourseDto] = {
    for {
      courseOpt <- getCourse(courseId)
      students <- studentStorage.listStudentsForCourse(courseId) toFuture()
    } yield FullCourseDto(courseId, courseOpt.get.courseName, students.map(_.toDto))
  }

  def getCourses: Future[Seq[Course]] = courseStorage.getCourses toFuture

  def getCourse(id: String): Future[Option[Course]] = {
    if (ObjectId.isValid(id))
      courseStorage.getCourse(new CourseId(id))
    else
      Future(None)
  }

  def createCourse(course: Course): Future[Completed] = courseStorage.saveCourse(course)
}
