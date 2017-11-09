package courses.restapi.core.service

import courses.restapi.BaseServiceTest
import courses.restapi.core.storage.Course.CourseId
import courses.restapi.core.storage.{Student, StudentStorage}
import org.mockito.Mockito._
import org.mongodb.scala.Completed
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.Future

class StudentServiceTest extends BaseServiceTest {

  "StudentService" when {
    "add new student" in new Context{
     val result = awaitForResult(studentStorage.createStudent(student) )
    }
    "update score for course" should {
      "update score successfully" in new Context {
        awaitForResult(service.addScore(student._id.toHexString, courseId.toHexString, 99 ))

      }
    }
  }

  sealed trait Context extends MockitoSugar {

    val student = Student("Saar", "Wexler","saarwexler@gmail.com")
    val courseId = new CourseId
    val studentStorage: StudentStorage = mock[StudentStorage]
    val service = new StudentService(studentStorage)
    when(studentStorage.createStudent(student))
      .thenReturn(Future(Completed())).thenThrow(new RuntimeException)
    when (studentStorage.addScore(student._id,  courseId, 99)).thenReturn(Future())
  }
}

