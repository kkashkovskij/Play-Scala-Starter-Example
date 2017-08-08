package models


case class Article (shortName: Option[String], fullName: String, text: String, chapterId: Int)

case class Chapter (id :Int, shortName: Option[String], fullName: String, text: Option[String], parentId: Option[Int])

