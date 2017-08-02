package controllers

import javax.inject.Inject


import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.text
import play.api.mvc.{ AbstractController, ControllerComponents }
import scala.concurrent.ExecutionContext


class Application @Inject() (
                              catDao: CatDAO,
                              dogDao: DogDAO,
                              controllerComponents: ControllerComponents
                            )(implicit executionContext: ExecutionContext) extends AbstractController(controllerComponents){

}
