import DB.LiquibaseService
import _configuration.BotConfig
import db.{MigrationService, MigrationServiceLive, QuillContext}
import liquibase.Liquibase
import zhttp.http._
import zhttp.service.client.ClientSSLHandler
import zhttp.service.client.ClientSSLHandler.ClientSSLOptions
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}
import zio._

import java.io.IOException
import javax.sql.DataSource

object Request {

//  val env     = ChannelFactory.auto ++ EventLoopGroup.auto()
  val urlTemplate     = "https://api.telegram.org/bot"
  val headers = Headers.host("api.telegram.org")

  val sslOption: ClientSSLOptions =
    ClientSSLOptions.CustomSSL(ClientSSLHandler.ssl(ClientSSLOptions.DefaultSSL))

  def send(method: String): ZIO[EventLoopGroup with ChannelFactory with BotConfig, IOException, Body] = (for {
    config <- ZIO.service[BotConfig]
      url = urlTemplate + config.token
    res  <- Client.request(url ++ "/" ++ method, headers = headers, ssl = sslOption)
    data <- res.body.asString
    _ <- Console.printLine(data)
  } yield res.body).catchAll( ex => Console.printLine(ex.getMessage) *> ZIO.succeed(Body.empty))
}

//object Application extends ZIOAppDefault {

//  import com.zaxxer.hikari.HikariDataSource

//  val ds = new HikariDataSource

//  private val app: Http[zio.ZEnv with Has[Config], Nothing, Request, Response] =
//    Http.collectZIO[Request] {
//      case Method.GET -> !! / method  => for {
//        result <- Request1(method)
//      } yield Response.apply( data = result)
//    }

//  val app: ZIO[EventLoopGroup with ChannelFactory with BotConfig, IOException, Unit] = for {
//    _ <- Request.send("getUpdates").schedule(Schedule.fixed(Duration.ofSeconds(10))).forever
//  } yield ()
//
//  def run = app.provide(ChannelFactory.auto, EventLoopGroup.auto(), BotConfig.layer)
//
//}

object test extends ZIOAppDefault {
//
//  import DB.Ctx
//  import DB.Ctx._
//
//  case class Person(name: String, age: Int)
//
//  val people = quote {
//    query[Person]
//  }
//
  val layers = QuillContext.dataSourceLayer >>> LiquibaseService.live >+> MigrationService.live

  val app = for {
    migration <- ZIO.service[MigrationService]
    _ <- migration.performMigration
  } yield ()
//
  def run = app.provide(layers)
}

//object test extends App {
//  lazy val ctx = new MysqlJdbcContext(Literal, "ctx")
//  val ds = ctx.dataSource
//  val accessor = new ClassLoaderResourceAccessor()
//  val conn = new JdbcConnection(ds.getConnection())
//  val liqui = new Liquibase("liquibase/main.xml", accessor, conn)
//
//  liqui.update()
//
//}