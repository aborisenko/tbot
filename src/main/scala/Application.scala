import db.LiquibaseService
import db.{MigrationService, QuillContext}
import io.circe.Json
import zhttp.http._
import zhttp.service.client.ClientSSLHandler
import zhttp.service.client.ClientSSLHandler.ClientSSLOptions
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}
import zio._

import java.io.IOException
import scala.language.postfixOps
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import scala.util.matching.Regex._

case class TelegramMessage(message_id: Long, date: Long, text: Option[String])
case class UpdateMessage(update_id: Long, message: TelegramMessage)
case class UpdateResponse(ok: Boolean, result: Array[UpdateMessage])



object Request {

//  val env     = ChannelFactory.auto ++ EventLoopGroup.auto()
  val urlTemplate     = "https://api.telegram.org/bot"
  val headers = Headers.host("api.telegram.org")

  val sslOption: ClientSSLOptions =
    ClientSSLOptions.CustomSSL(ClientSSLHandler.ssl(ClientSSLOptions.DefaultSSL))

  val url = urlTemplate + "5700138842:AAG6uET_pcvL8M57o3W0aHrTWKA8etkLBzU"
  def pull(updateIdR: Ref[Long]): ZIO[EventLoopGroup with ChannelFactory, IOException, Body] = (for {
    updateId <- updateIdR.get
    _url = url ++ "/" ++ s"getUpdates?offset=${updateId}&timeout=10&limit=100"
    _ <- Console.printLine(_url)
    res  <- Client.request(_url, headers = headers, ssl = sslOption)
    data <- res.body.asString
//    _ <- Console.printLine(data)
  } yield res.body).catchAll( ex => Console.printLine(ex.getMessage) *> ZIO.succeed(Body.empty) )
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

  val migrations = (for {
    migration <- ZIO.service[MigrationService]
    _ <- migration.performMigration
  } yield ()).provideSomeLayer(LiquibaseService.live >+> MigrationService.live)

  import Request.pull

  val updateIdRef: UIO[Ref[Long]] = Ref.make(0L)
  def start(updateIdRef: UIO[Ref[Long]]) = for {
    updateId <- updateIdRef
    _ <- pooling(updateId)
  } yield ()

  def updateCounter(list: Array[Long], ref: Ref[Long]) = for {
    maxUpdateId <- ZIO.attempt(list.max)
    _ <- ref.update(_ => maxUpdateId + 1)
  } yield ()


  case class CustomerRequestDTO(id:               Option[String] = None,
                                transactionType:  Option[String] = None,
                                takeAmount:       Option[String] = None,
                                takeCurrency:     Option[String] = None,
                                releaseAmount:    Option[String] = None,
                                releaseCurrency:  Option[String] = None,
                                inRate:           Option[String] = None,
                                outRate:          Option[String] = None,
                                commission:       Option[String] = None
                               )

  def parseTextMessage(list: Array[(Long, Option[String])]) = {
    val pattern = raw".*(\w+ )".r.pattern

    val idPattern = raw"️.*ID:\s*([\w\s\d]+)".r
    val transactionType = raw".*Тип сделки:(\w+)".r
    val take = raw".*Принял:\s*(\d+)\s*([\w]+)".r
    val inRate = raw".*Курс:([\d\,\.]+)".r
    val outRate = raw".*Гар:([\d\.\,]+)".r
    val release = raw".*Выдал\s*(\d+)\s*([\w]+)".r
//    val phonePattern = raw".*️Телефон\s*:\s*(.+)".r
//    val transactionInfo = raw"(.+)→(.+)".r
//    val walletPattern = raw".*Кошелек\s*:\s*(.+)".r
//    val passwordPattern = raw".*Пароль\s*:\s*(.+)".r
//    val appointmentPattern = raw"(.+офис.+)".r

    for {
      messages <- ZIO.succeed(list.collect{ case (_, Some(str)) => str})
      res = messages.map { msg =>
        msg.split("\n").foldLeft(CustomerRequestDTO()) {
          case (acc, idPattern(id)) => acc.copy(id = Some(id.trim))
          case (acc, transactionType(tType)) => acc.copy(transactionType = Some(tType.trim))
          case (acc, take(amount, currency)) => acc.copy(takeAmount = Some(amount.trim), takeCurrency = Some(currency.trim))
          case (acc, inRate(rate)) => acc.copy(inRate = Some(rate.trim))
          case (acc, release(amount, currency)) => acc.copy(releaseAmount = Some(amount.trim), releaseCurrency = Some(currency.trim))
          case (acc, outRate(rate)) => acc.copy(outRate = Some(rate.trim))
          case (acc, _) => acc
        }
      }
    } yield (res)
  }

  def pooling(updateId: Ref[Long]): ZIO[EventLoopGroup with ChannelFactory, Throwable, Unit] = for {
    b <- pull(updateId)
    str <- b.asString
//    json = Json.fromString(str)
//    array = json // "return"
    responseMsg <- ZIO.fromEither(decode[UpdateResponse](str))
    res = responseMsg.result.map(ur => (ur.update_id,ur.message.text))
    _ <- updateCounter(res.map( _._1 ), updateId).catchAll(_ => ZIO.unit)
    _ <- Console.printLine(res.mkString)
    arr <- parseTextMessage(res)
    _ <- Console.printLine(arr.mkString("(", ", ", ")"))
    _ <- pooling( updateId)
  } yield ()
  def run = migrations.provide(QuillContext.dataSourceLayer) *> start(updateIdRef).schedule(Schedule.fixed(10 seconds)).forever.provide(ChannelFactory.auto, EventLoopGroup.auto())
}