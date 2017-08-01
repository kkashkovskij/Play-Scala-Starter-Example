import play.api.ApplicationLoader.Context
import play.api.BuiltInComponentsFromContext
import play.api.db.{Database, DBComponents, HikariCPComponents}
import play.api.db.evolutions.EvolutionsComponents
import play.api.routing.Router
import play.filters.HttpFiltersComponents

class AppComponents(cntx: Context)
  extends BuiltInComponentsFromContext(cntx)
    with DBComponents
    with EvolutionsComponents
    with HikariCPComponents
    with HttpFiltersComponents
{
  // this will actually run the database migrations on startup
  applicationEvolutions

 // override def router: Router = ???
  override def router: Router = ???
}