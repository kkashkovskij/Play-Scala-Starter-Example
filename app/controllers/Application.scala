package controllers

import javax.inject.Inject

import com.fasterxml.jackson.core.TreeNode
import dao.{ArticleDAO, ChapterDAO}
import models.{Article, Chapter}
import org.apache.commons.lang3.mutable.Mutable
import play.api.data.Form
import play.api.data.Forms.{mapping, number, text}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.collection.mutable
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}


class Application @Inject() (
                              articleDao: ArticleDAO,
                              chapterDao: ChapterDAO,
                              controllerComponents: ControllerComponents
                            )(implicit executionContext: ExecutionContext) extends AbstractController(controllerComponents) with I18nSupport{



  def index = Action.async {implicit request =>
      val messages: Messages = request.messages
      val message: String = messages("info.error")
    articleDao.all().zip(chapterDao.all()).map { case (articles, chapters) => Ok(views.html.index(articleForm, chapterForm, articles, chapters, treeRoots)) }
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



  val chaptersF: Future[Seq[Chapter]] = chapterDao.all();
  val chapters: Seq[Chapter] = Seq[Chapter]()
  chaptersF.map{case (chapters) => chapters}
  val articlesF: Future[Seq[Article]] = articleDao.all();
  val articles: Seq[Article] = Seq[Article]()
  articlesF.map{case (articles) => articles}
  var treeRoots: mutable.MutableList[TreeNode[Chapter]] = new mutable.MutableList[TreeNode[Chapter]]()
  var numbersList: mutable.MutableList[String] = new mutable.MutableList[String]()
  var pathList: mutable.MutableList[String] = new mutable.MutableList[String]()


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
      } else if (treeNode.getData().id == c.parentId.getOrElse(0)){
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
    num = num + n.getChapterNumber().toString
    str = str + "/" + num + n.getData().shortName

    pathList.+=:(str)
    numbersList.+=:(num)

    for(c <- n.getChildren()){
      getPathList(c, str, number)
    }
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
}


//case class GeneralTree(){
//
//  class Node(val data: Chapter, val children: Seq[Node])
//  class Leaf(val data: Article)
//
//  private val root:Node = null
//  private var children: mutable.MutableList[Node]
//
//  def preorder(visit: A => Unit): Unit = {
//    def recur(n: Node): Unit = {
//      visit(n.data)
//      for (c <- n.children) recur(c)
//    }
//
//    recur(root)
//  }
//
//  def postorder(visit: A => Unit): Unit = {
//    def recur(n: Node): Unit = {
//      for(c <- n.children) recur(c)
//      visit(n.data)
//    }
//
//    recur(root)
//  }
//
//  def height(n:Node): Int = {
//    1 + n.children.foldLeft(-1)((h, c) => h max height(c))
//  }
//
//  def size (n: Node): Int = {
//    n.children.foldLeft(0)((s,c) => s+size(c))
//  }
//
//  def setChildren (chapters: Seq[Chapter], node: Node): Unit ={
//
//    for (c <- chapters){
//      if (c.parentId.isEmpty){
//        rootChildren.+=:(new Node(c, null))
//
//      }
//      root = new Node(new Chapter(), rootChildren)
//    }
//  }
//}
//
//object GeneralTree{
//
//}
