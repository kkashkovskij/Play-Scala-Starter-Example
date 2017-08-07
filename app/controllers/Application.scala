package controllers

import javax.inject.Inject

import dao.{ArticleDAO, ChapterDAO}
import models.{Article, Chapter}
import play.api.data.Form
import play.api.data.Forms.{mapping, number, text}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext


class Application @Inject() (
                              articleDao: ArticleDAO,
                              chapterDao: ChapterDAO,
                              controllerComponents: ControllerComponents
                            )(implicit executionContext: ExecutionContext) extends AbstractController(controllerComponents) with I18nSupport{

  def index = Action.async {implicit request =>
      val messages: Messages = request.messages
      val message: String = messages("info.error")
    articleDao.all().zip(chapterDao.all()).map { case (articles, chapters) => Ok(views.html.index(articleForm, chapterForm, articles, chapters)) }
  }


//def index = Action { implicit request =>
//  val messages: Messages = request.messages
//  val message: String = messages("info.error")
//  Ok(views.html.index(articleForm, chapterForm, articles, chapters))
//}


  val chapterForm :Form[ChapterFormModel] = Form(
    mapping(
      "shortName" -> text,
      "fullName" -> text,
      "text" -> text,
      "parentId" -> number
    )(ChapterFormModel.apply)(ChapterFormModel.unapply)
  )

  val articleForm: Form[ArticleFormModel] = Form(
    mapping(
      "shortName" -> text,
      "fullName" -> text,
      "text" -> text,
      "chapterId" -> number
    )(ArticleFormModel.apply)(ArticleFormModel.unapply)
  )

  private def trimToOption(str: String): Option[String] = {
    val trimed =  str.trim
    if(trimed.isEmpty) None else Some(trimed)
  }

  def insertChapter = Action.async{ implicit request =>
    val chapter: ChapterFormModel = chapterForm.bindFromRequest.get
    chapterDao.insert(Chapter(trimToOption(chapter.shortName), chapter.fullName, trimToOption(chapter.text),
      if(chapter.parentId == -1) None else Some(chapter.parentId) )).map(_ => Redirect(routes.Application.index))
  }

  def insertArticle =
    Action.async{ implicit request =>
    val article: ArticleFormModel = articleForm.bindFromRequest.get
    articleDao.insert(Article(trimToOption(article.shortName), article.fullName, article.text,
      article.chapterId)).map(_ => Redirect(routes.Application.index))
  }

  def getCategoryArray() {

  }

  def buildTree(parentId: Int, treeLevel: Int){

  }

}

case class ChapterFormModel(shortName: String, fullName: String, text: String, parentId: Int)

object ChapterFormModel{}

case class ArticleFormModel(shortName: String, fullName: String, text: String, chapterId: Int)

object ArticleFormModel{}

case class TreeNode(number: Int, shortName: String, children: TreeNode*)

