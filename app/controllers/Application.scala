package controllers

import javax.inject.Inject

import dao.{ArticleDAO, ChapterDAO}
import models.{Article, Chapter}
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.text
import play.api.data.Forms.number
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext


class Application @Inject() (
                              articleDao: ArticleDAO,
                              chapterDao: ChapterDAO,
                              controllerComponents: ControllerComponents
                            )(implicit executionContext: ExecutionContext) extends AbstractController(controllerComponents){

  def index = Action.async {
    articleDao.all().zip(chapterDao.all()).map { case (articles, chapters) => Ok(views.html.index(articles, chapters)) }
  }

  val chapterForm = Form(
    mapping(
      "id" -> number(),
      "short name" -> text(),
      "full name" -> text(),
      "text" -> text(),
      "parent id" -> number()
    )(Chapter.apply)(Chapter.unapply)
  )

  val articleForm = Form(
    mapping(
      "id" -> number(),
      "short name" -> text(),
      "full name" -> text(),
      "text" -> text(),
      "parent id" -> number()
    )(Article.apply)(Article.unapply)
  )

  def insertChapter = Action.async{ implicit request =>
    val chapter: Chapter = chapterForm.bindFromRequest.get
    chapterDao.insert(chapter).map(_ => Redirect(routes.Application.index))
  }

  def insertArticle = Action.async{ implicit request =>
    val article: Article = articleForm.bindFromRequest.get
    articleDao.insert(article).map(_ => Redirect(routes.Application.index))
  }
}

