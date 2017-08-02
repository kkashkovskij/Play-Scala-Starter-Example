package dao

import scala.concurrent.{ ExecutionContext, Future }
import javax.inject.Inject

import models.Cat
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

case class article(id: Int, shortName: String, fullName: String, parentChapterId: Int) {

}
