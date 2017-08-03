package controllers

import javax.inject.Inject

import dao.{ArticleDAO, ChapterDAO}
import models.{Article, Chapter}
import play.api.data.Form
import play.api.data.Forms.{mapping, number, text}
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

//  val chapterForm = Form(
//    mapping(
//      "id" -> number(),
//      "short name" -> text(),
//      "full name" -> text(),
//      "text" -> text(),
//      "parent id" -> number()
//    )(Chapter.apply)(Chapter.unapply)
//  )

  val chapterForm = Form(
    mapping(
      "shortName" -> text,
      "fullName" -> text,
      "text" -> text,
      "parentId" -> number
    )(ChapterFormModel.apply)(ChapterFormModel.unapply)
  )

  val articleForm = Form(
    mapping(
      "short name" -> text,
      "full name" -> text,
      "text" -> text,
      "parent id" -> number
    )(ArticleFormModel.apply)(ArticleFormModel.unapply)
  )

  private def trimToOption(str: String): Option[String] = {
    val trimed =  str.trim
    if(trimed.isEmpty) None else Some(trimed)
  }

  def insertChapter = Action.async{ implicit request =>
    val chapter: ChapterFormModel = chapterForm.bindFromRequest.get
    chapterDao.insert(Chapter(1, trimToOption(chapter.shortName), chapter.fullName, trimToOption(chapter.text),
      if(chapter.parentId == -1) None else Some(chapter.parentId) )).map(_ => Redirect(routes.Application.index))
  }

  def insertArticle = Action.async{ implicit request =>
    val article: ArticleFormModel = articleForm.bindFromRequest.get
    articleDao.insert(Article(1, trimToOption(article.shortName), article.fullName, article.text,
      article.chapterId)).map(_ => Redirect(routes.Application.index))
  }

  /*def insertArticle = Action.async{ implicit request =>
    val article: ArticleFormModel = articleForm.bindFromRequest.get
    articleDao.insert(Article(1, trimToOption(article.shortName), article.fullName, article.text,article.chapterId))

  }*/

  /*def insertArticle = Action.async
  { implicit request =>

    val chapterModel: ChapterFormModel = articleForm.bindFromRequest.get
  }*/
}

case class ChapterFormModel(shortName: String, fullName: String, text: String, parentId: Int)

object ChapterFormModel{}

case class ArticleFormModel(shortName: String, fullName: String, text: String, chapterId: Int)

object ArticleFormModel{}