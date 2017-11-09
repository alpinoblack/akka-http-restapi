package courses.restapi.core.storage

import courses.restapi.core.storage.Course.CourseId
import courses.restapi.core.storage.Student.StudentId
import org.mongodb.scala.bson.ObjectId

object Student {
  type StudentId = ObjectId
  def apply(firstName: String, lastName: String, email: String): Student =  Student(new StudentId, firstName, lastName, email, Nil)

}
case class CourseScore(courseId: CourseId, score: Option[Int])
case class Student (_id: StudentId,firstName: String, lastName: String, email: String, courses: Seq[CourseScore])
case class ListStudent(_id: StudentId, firstName: String, lastName: String, email: String, avg: Double )

