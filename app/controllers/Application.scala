package controllers

import javax.inject.Inject

import dao.{ArticleDAO, ChapterDAO}
import models.{Article, Chapter}
import play.api.data.Form
import play.api.data.Forms.{mapping, number, text}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, ControllerComponents}
import scala.concurrent.duration._
import scala.collection.mutable
import scala.concurrent.{Await, ExecutionContext, Future}



class Application @Inject() (
                              articleDao: ArticleDAO,
                              chapterDao: ChapterDAO,
                              controllerComponents: ControllerComponents
                            )(implicit executionContext: ExecutionContext) extends AbstractController(controllerComponents) with I18nSupport{



  def index = Action.async {implicit request =>
      val messages: Messages = request.messages
      val message: String = messages("info.error")
    articleDao.all().zip(chapterDao.all()).map { case (articles, chapters) => {

      getFromdb()
      setTreeNodes(null, 1)
      getAllPath()

      Ok(views.html.index(articleForm, chapterForm, pathList))} }
  }


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
    chapterDao.insert(
      Chapter(1, trimToOption(chapter.shortName),
        chapter.fullName,
        trimToOption(chapter.text),
      if(chapter.parentId == -1) None else Some(chapter.parentId))).map(_ => Redirect(routes.Application.index))
  }

  def insertArticle =
    Action.async{ implicit request =>
    val article: ArticleFormModel = articleForm.bindFromRequest.get
    articleDao.insert(Article(trimToOption(article.shortName), article.fullName, article.text,
      article.chapterId)).map(_ => Redirect(routes.Application.index))
  }


  var chapters: Seq[Chapter] = Seq[Chapter]()
  var articles: Seq[Article] = Seq[Article]()
  var treeRoots: mutable.MutableList[TreeNode[Chapter]] = new mutable.MutableList[TreeNode[Chapter]]()
  var numbersList: mutable.MutableList[String] = new mutable.MutableList[String]()
  var pathList: mutable.MutableList[String] = new mutable.MutableList[String]()

  def getFromdb(): Unit = {

    val chaptersF: Future[Seq[Chapter]] = chapterDao.all()
    val articlesF: Future[Seq[Article]] = articleDao.all()
    chapters = Await.result(chaptersF, 1.second)
    articles = Await.result(articlesF, 1.second)

    }



  def setTreeNodes (treeNode: TreeNode[Chapter], count: Int): Unit ={
    var bufferNode: TreeNode[Chapter] = null
    var i: Int = 1

    for(c <- chapters) {
      if (treeNode == null && c.parentId.isEmpty) {
        bufferNode = new TreeNode[Chapter](c, null, i, new mutable.MutableList[TreeNode[Chapter]], new mutable.MutableList[Article])
        setArticles(bufferNode)
        treeRoots.+=:(bufferNode)
        setTreeNodes(bufferNode, 1)
        i+=1
      } else if (treeNode!=null && (treeNode.getData().id == c.parentId.getOrElse(0))){
        bufferNode = new TreeNode[Chapter](c, treeNode, i, new mutable.MutableList[TreeNode[Chapter]], new mutable.MutableList[Article])
        treeNode.addChild(bufferNode)
        setArticles(bufferNode)
        setTreeNodes(bufferNode, 1)
        i+=1
      }
    }
  }

  def setArticles(treeNode: TreeNode[Chapter]): Unit = {
    for (a <- articles){
      if(a.chapterId == treeNode.getData().id) {
        treeNode.addArticle(a)
      }
    }
  }

  def getPathList(n: TreeNode[Chapter], path: String, number: String): Unit = {

    var str: String = ""
    var num: String = ""
    if (number == "") num = n.getChapterNumber().toString + "."
    else num = number + n.getChapterNumber().toString + "."
    str = path + "/" + num + n.getData().shortName.getOrElse("")

    for(c <- n.getChildren()){
      getPathList(c, str, num)
    }

    for(a <- n.getArticles()){
      pathList.+=:(str + "/" + a.shortName.getOrElse(""))
      numbersList.+=:("art:")
    }
    pathList.+=:(str)
    numbersList.+=:(num)
  }

  def getAllPath(): Unit = {
    for (root <- treeRoots){
      getPathList(root, "", "")
    }
  }
}

case class ChapterFormModel(shortName: String, fullName: String, text: String, parentId: Int)

object ChapterFormModel{}

case class ArticleFormModel(shortName: String, fullName: String, text: String, chapterId: Int)

object ArticleFormModel{}

class TreeNode[T] (data: T, parent: TreeNode[T], chapterNumber: Int, children: mutable.MutableList[TreeNode[T]], articleList: mutable.MutableList[Article]){
  def addChild(treeNode: TreeNode[T]): Unit ={
    children.+=:(treeNode)
  }
  def addArticle(article: Article): Unit ={
    articleList.+=:(article)
  }
  def getData(): T= {
    data
  }
  def getChildren(): mutable.MutableList[TreeNode[T]] = {
    this.children
  }

  def getChapterNumber(): Int = {
    this.chapterNumber
  }

  def getArticles(): mutable.MutableList[Article] = {
    this.articleList
  }
}

