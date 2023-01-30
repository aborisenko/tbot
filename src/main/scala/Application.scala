import DB.ZioDataSourceService
import configuration.BotConfig
import zhttp.http._
import zhttp.service.client.ClientSSLHandler
import zhttp.service.client.ClientSSLHandler.ClientSSLOptions
import zhttp.service.{ChannelFactory, Client, EventLoopGroup, Server}
import zio._

import java.io.{IOException, InputStream}
import java.security.KeyStore
import java.time.Duration
import javax.net.ssl.TrustManagerFactory

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

  import DB.Ctx
  import DB.Ctx._

  case class Person(name: String, age: Int)

  val people = quote {
    query[Person]
  }

  val app = Ctx.run(people).tap(result => Console.printLine(result.toString))

  def run = app.provide(ZioDataSourceService.layer)
}