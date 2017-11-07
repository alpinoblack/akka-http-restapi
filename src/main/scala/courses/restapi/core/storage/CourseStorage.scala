package courses.restapi.core.storage

import courses.restapi.core.CourseId
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala._
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.model.Filters._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


sealed trait CourseStorage {

  def getCourses: Observable[Course]

  def getCourse(id: CourseId): Future[Option[Course]]

  def saveCourse(course: Course): Future[Completed]
}





class MongoCourseStorage extends CourseStorage with MongoStorage  {

  private val codecRegistry = fromRegistries(fromProviders(classOf[Course]), DEFAULT_CODEC_REGISTRY)
  private val courseCollection = db.getCollection[Course]("courses").withCodecRegistry(codecRegistry)

  override def getCourses: FindObservable[Course] = courseCollection.find()

  override def getCourse(id: CourseId): Future[Option[Course]] =
    courseCollection.find(equal("_id", id)).first() toFuture() map(c => Option(c))

  override def saveCourse(course: Course): Future[Completed] = courseCollection.insertOne(course).toFuture()
}



