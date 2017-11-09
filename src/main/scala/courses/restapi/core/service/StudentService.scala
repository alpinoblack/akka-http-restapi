package courses.restapi.core.service

import courses.restapi.core.storage.Course.CourseId
import courses.restapi.core.storage.Student.StudentId
import courses.restapi.core.storage.{ListStudent, Student, StudentStorage}
import org.mongodb.scala.Completed

import scala.concurrent.{ExecutionContext, Future}

class StudentService(studentStorage: StudentStorage)(implicit executionContext: ExecutionContext) {

  def addScore(studentId: String, courseId: String, score: Int) = studentStorage.addScore(new StudentId(studentId), new CourseId(courseId), score)

  def assign(studentId: String, courseId: String) = studentStorage.assignCourse(new StudentId(studentId), new CourseId(courseId))

  def listStudents: Future[Seq[ListStudent]] = studentStorage.listStudents toFuture

  def listOutstandingStudents(minAvgScore: Int) = studentStorage.listOutstandingStudents(minAvgScore) toFuture()

  def topStudent = studentStorage.getBestStudent

  def getStudent(id: String): Future[Option[Student]] = studentStorage.getStudent(new StudentId(id))

  def createStudent(student: Student): Future[Completed] = studentStorage.createStudent(student)
}
