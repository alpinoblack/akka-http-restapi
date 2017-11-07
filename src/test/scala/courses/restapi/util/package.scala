package courses.restapi

import courses.restapi.core.storage.{ListStudent, Student}

 package object util {
  implicit class avgHelper(seq: Seq[Int]) {
    def avg = seq.foldLeft((0.0, 1)) { case ((avg, idx), next) => (avg + (next - avg) / idx, idx + 1) }._1
  }

  implicit class StudentHelper(student: Student) {

    import student._

    def toListStudent = ListStudent(_id, firstName, lastName, email, courses.flatMap(_.score).avg)
  }


}
