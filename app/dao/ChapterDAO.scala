package dao

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}
import models.Chapter
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import play.api.db.slick.HasDatabaseConfigProvider


import scala.concurrent.ExecutionContext

class ChapterDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  private val Chapters = TableQuery[ChaptersTable]

  def all(): Future[Seq[Chapter]] = db.run(Chapters.result)

  def insert(chapter: Chapter): Future[Unit] = db.run(Chapters += chapter).map { _ => () }
  private class ChaptersTable(tag: Tag) extends Table[Chapter] (tag, "chapters"){

    //def id = column[Int]("id", O.PrimaryKey)
    def shortName = column[Option[String]]("shortname")
    def fullName = column[String]("fullname")
    def text = column[Option[String]]("text")
    def parentId = column[Option[Int]]("parentid")


    def * = (shortName, fullName, text, parentId) <> (Chapter.tupled, Chapter.unapply)
  }
}
