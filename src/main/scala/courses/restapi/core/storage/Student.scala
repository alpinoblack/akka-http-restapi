package courses.restapi.core.storage

import courses.restapi.core.{CourseId, StudentId}
import courses.restapi.http.routes.StudentDto

object Student {
  def apply(firstName: String, lastName: String, email: String): Student =  Student(new StudentId, firstName, lastName, email, Nil)

}
case class CourseScore(courseId: CourseId, score: Option[Int])
case class Student (_id: StudentId,firstName: String, lastName: String, email: String, courses: Seq[CourseScore])
case class ListStudent(_id: StudentId, firstName: String, lastName: String, email: String, avg: Double )

