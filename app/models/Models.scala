package models

case class Article (shortName: Option[String], fullName: String, text: String, chapterId: Int)

case class Chapter (shortName: Option[String], fullName: String, text: Option[String], parentId: Option[Int])

