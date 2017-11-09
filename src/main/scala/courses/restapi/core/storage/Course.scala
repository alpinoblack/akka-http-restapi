package courses.restapi.core.storage

import courses.restapi.core.storage.Course.CourseId
import org.mongodb.scala.bson.ObjectId

object Course {
  type CourseId = ObjectId
  def apply(courseName: String): Course = Course(new CourseId(), courseName)
}

final case class Course(_id: CourseId, courseName: String)
