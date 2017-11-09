package courses.restapi.core.storage

import courses.restapi.BaseServiceTest
import courses.restapi.core.storage.Course.CourseId
import org.mongodb.scala.model.Filters

class CourseStorageTest extends BaseServiceTest {

  override def afterAll(): Unit = {
    awaitForResult(MongoStorage.db.getCollection("courses").deleteMany(Filters.equal("courseName", "my course")) toFuture())
    super.afterAll()
  }


  "CourseStorage" should {
    "save and load course by id" in new Context {
      awaitForResult(for {
        _ <- courseStorage.saveCourse(course)
        courseOption <- courseStorage.getCourse(course._id)
      } yield courseOption.get shouldEqual course)
    }
    "return None when course does not exist" in new Context {
      awaitForResult(for {
        c <- courseStorage.getCourse(new CourseId)
      } yield c shouldBe None)
    }

  }

  trait Context {
    val courseStorage = new MongoCourseStorage

    val course = Course("my course")
  }

}


