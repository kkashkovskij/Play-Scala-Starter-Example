package dao

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}
import models.Article
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import play.api.db.slick.HasDatabaseConfigProvider

import scala.concurrent.ExecutionContext

class ArticleDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  private val Articles = TableQuery[ArticlesTable]

  def all(): Future[Seq[Article]] = db.run(Articles.result)

  def insert(article: Article): Future[Unit] = db.run(Articles += article).map { _ => () }

  private class ArticlesTable(tag: Tag) extends Table[Article] (tag, "Chapter"){

    def id = column[Int]("ID", O.PrimaryKey)
    def shortName = column[String]("SHORT NAME")
    def fullName = column[String]("FULL NAME")
    def text = column[String]("TEXT")
    def parentId = column[Int]("PARENT ID")

    def * = (id, shortName, fullName, text, parentId) <> (Article.tupled, Article.unapply)
  }
}