package courses.restapi.core.storage

import courses.restapi.core.CourseId

final object Course{
  def apply(courseName: String): Course = Course(new CourseId(), courseName)
}

final case class Course(_id: CourseId, courseName: String)
