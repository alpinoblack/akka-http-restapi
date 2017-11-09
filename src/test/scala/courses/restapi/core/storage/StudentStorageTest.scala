package courses.restapi.core.storage

import courses.restapi.BaseServiceTest
import courses.restapi.core.storage.Course.CourseId
import courses.restapi.core.storage.Student.StudentId
import courses.restapi.util._
import org.mongodb.scala.MongoException
import org.mongodb.scala.model.Filters
import org.scalatest.Outcome

import scala.util.Success

class StudentStorageTest extends BaseServiceTest {
  override def withFixture(test: NoArgTest): Outcome = {
    try {
      super.withFixture(test)
    }
    finally {
      awaitForResult(
        MongoStorage.db.getCollection("students").deleteMany(Filters.in("firstName", "Saar", "Albert", "Isaac")) toFuture())
      super.afterAll()
    }
  }

  "StudentStorage" when {
    "save and load student by id" in new Context {
      awaitForResult(for {
        _ <- studentStorage.createStudent(student1)
        studentOption <- studentStorage.getStudent(student1._id)
      } yield studentOption.get._id shouldBe student1._id)
    }

    "return None when student does not exist" in new Context {
      awaitForResult(for {
        c <- studentStorage.getStudent(new StudentId)
      } yield c shouldBe None)
    }

    "list all students" should {
      "get all the students with average score" in new Context {

        val x = awaitForResult(for {
          _ <- studentStorage.createStudent(studentWithAvgScore75)
          _ <- studentStorage.createStudent(student2)
          students <- studentStorage.listStudents toFuture()
        } yield {
          students shouldEqual Seq(studentWithAvgScore75, student2).map(_.toListStudent)
        }
        )
      }
    }

    "list top students" should {
      "get all students with average score above 90" in new Context {
        val studentWithAvgScore95 = Student(new StudentId(), "Albert", "Einstein", "genius@gmail.com",
          Seq(CourseScore(new CourseId, Some(100)), CourseScore(new CourseId, Some(90))))
        val studentWithAvgScore100 = Student(new StudentId(), "Isaac", "Newton", "apple@gmail.com",
          Seq(CourseScore(new CourseId, Some(100))))
        awaitForResult(for {
          _ <- studentStorage.createStudent(studentWithAvgScore75)
          _ <- studentStorage.createStudent(studentWithAvgScore95)
          _ <- studentStorage.createStudent(studentWithAvgScore100)
          students <- studentStorage.listOutstandingStudents(95) toFuture()
        } yield students shouldEqual Seq(studentWithAvgScore100, studentWithAvgScore95).map(_.toListStudent))
      }
    }

    "get the best student" should {
      "return the student with the highest average score" in new Context {
        val studentWithAvgScore95 = Student(new StudentId(), "Albert", "Einstein", "genius@gmail.com",
          Seq(CourseScore(new CourseId, Some(100)), CourseScore(new CourseId, Some(90))))
        val studentWithAvgScore100 = Student(new StudentId(), "Isaac", "Newton", "apple@gmail.com",
          Seq(CourseScore(new CourseId, Some(100))))
        awaitForResult(for {
          _ <- studentStorage.createStudent(studentWithAvgScore75)
          _ <- studentStorage.createStudent(studentWithAvgScore95)
          _ <- studentStorage.createStudent(studentWithAvgScore100)
          student <- studentStorage.getBestStudent
        } yield student shouldEqual studentWithAvgScore100)
      }
    }

    "assign a course to a student" in new Context {
      awaitForResult(for {
        _ <- studentStorage.createStudent(student1)
        _ <- courseStorage.saveCourse(course1)
        result <- studentStorage.assignCourse(student1._id, course1._id)
        updatedStudent <- studentStorage.getStudent(student1._id)
      } yield {
        val c = updatedStudent.get.courses.head
        result shouldBe Success()
        c.courseId shouldBe course1._id
        c.score shouldBe None
      })
    }

    "add score to a student for course" should {
      "succeed when course is registered" in new Context {
        awaitForResult(for {
          _ <- studentStorage.createStudent(student2)
          _ <- studentStorage.addScore(student2._id, course2._id, 99)
          updatedStudent <- studentStorage.getStudent(student2._id)
        } yield {
          val courses = updatedStudent.get.courses
          courses.length shouldBe 1
          courses.head.courseId shouldBe course2._id
          courses.head.score shouldBe Some(99)
        })
      }

      "failed when course is not registered" in new Context {
        an[MongoException] should be thrownBy {
          awaitForResult(for {
            _ <- studentStorage.createStudent(student2)
            gradeResult <- studentStorage.addScore(student2._id, course1._id, 99)
          } yield gradeResult)
        }
      }

      "list all students for course" in new Context {
        awaitForResult(for {
          _ <- studentStorage.createStudent(student1)
          _ <- studentStorage.createStudent(student2)
          _ <- courseStorage.saveCourse(course1)
          _ <- studentStorage.assignCourse(student1._id, course1._id)
          _ <- studentStorage.assignCourse(student2._id, course1._id)
          s1 <- studentStorage.getStudent(student1._id)
          s2 <- studentStorage.getStudent(student2._id)
          students <- studentStorage.listStudentsForCourse(course1._id.toHexString) toFuture()
        } yield {
          students shouldEqual Seq(s1.get, s2.get)
        })
      }
    }
  }

  sealed trait Context {
    val studentStorage = new MongoStudentStorage
    val courseStorage = new MongoCourseStorage
    val course1 = Course("sample course")
    val course2 = Course("course 2")
    val student1 = Student("Saar", "Wexler", "saarwexler@gmail.com")
    val student2 = Student(new StudentId, "Saar", "Wexler", "saarwexler@hotmail.com", Seq(CourseScore(course2._id, None)))

    val studentWithAvgScore75 = student1
      .copy(courses = Seq(CourseScore(new CourseId(), Some(50)),
        CourseScore(new CourseId, Some(100)),
        CourseScore(new CourseId, None)))
  }

}


