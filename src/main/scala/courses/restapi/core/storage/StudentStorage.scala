package courses.restapi.core.storage

import com.mongodb.MongoException
import courses.restapi.core.{CourseId, StudentId}
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala._
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.model.Aggregates._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts.descending
import org.mongodb.scala.model.{Filters, Updates}
import org.mongodb.scala.result.UpdateResult

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}


sealed trait StudentStorage {
  def listStudentsForCourse(courseId: String): Observable[Student]

  def listStudents: Observable[ListStudent]

  def getStudent(id: StudentId): Future[Option[Student]]

  def createStudent(student: Student): Future[Completed]

  def updateStudent(student: Student): Future[UpdateResult]

  def assignCourse(studentId: StudentId, courseId: CourseId): Future[Try[Unit]]

  def addScore(studentId: StudentId, courseId: CourseId, score: Int): Future[Unit]

  def listTopStudents : Observable[ListStudent]
}

class MongoStudentStorage extends StudentStorage with MongoStorage {
 override def listTopStudents = studentCollection.withDocumentClass[ListStudent]
    .aggregate(Seq(project(Document(
      """{_id: "$_id",
        |firstName: "$firstName",
        |lastName: "$lastName",
        | email: "$email", avg: {$avg: "$courses.score"}}""".stripMargin)),
      filter(gt("avg", 90.00)),
      sort(descending("avg"))))



  private val codecRegistry = fromRegistries(fromProviders(classOf[CourseScore], classOf[Student], classOf[ListStudent]), DEFAULT_CODEC_REGISTRY)
  private val studentCollection = db.getCollection[Student]("students").withCodecRegistry(codecRegistry)

  override def getStudent(id: StudentId): Future[Option[Student]] =
    studentCollection.find(equal("_id", id)).first() toFuture() map (c => Option(c))

  override def listStudentsForCourse(courseId: String): Observable[Student] =
    studentCollection.find(Filters.elemMatch("courses", equal("courseId", new CourseId(courseId))))

  override def listStudents: Observable[ListStudent] = studentCollection.withDocumentClass[ListStudent]
    .aggregate(Seq(project(Document(
      """{_id: "$_id", firstName: "$firstName", lastName: "$lastName", email: "$email", avg: {$avg: "$courses.score"}}"""))))


  override def createStudent(student: Student): Future[Completed] = studentCollection.insertOne(student) toFuture

  override def updateStudent(student: Student): Future[UpdateResult] = studentCollection.replaceOne(equal("_id", student._id), student) toFuture

  override def assignCourse(studentId: StudentId, courseId: CourseId): Future[Try[Unit]] =
    studentCollection.updateOne(equal("_id", studentId), Updates.addToSet("courses", CourseScore(courseId, None))).toFuture
      .map(r => r.getModifiedCount match {
        case 0 => Failure(new Exception)
        case _ => Success(Unit)
      })


  override def addScore(studentId: StudentId, courseId: CourseId, score: Int) = studentCollection.
    updateOne(and(equal("_id", studentId), equal("courses.courseId", courseId)), Updates.set("courses.$.score", score))
    .toFuture.map(r => r.getModifiedCount match {
    case 0 => throw new MongoException("Error adding score student or course are not exist")
    case _ => Unit
  })
}



