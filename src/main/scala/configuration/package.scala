import zio._
import zio.config.ReadError
import zio.config._
import zio.config.magnolia.descriptor
import zio.config.typesafe.TypesafeConfigSource

package object configuration {

  case class BotConfig(token: String)

  case class LiquibaseConfig(changeLog: String)
  case class Api(host: String, port: Int)
  case class DbConfig(driver: String, url: String, user: String, password: String)

  object BotConfig {

    val layer: ZLayer[Any, ReadError[String], BotConfig] =
        ZLayer {
          read {
            descriptor[BotConfig].from(
              TypesafeConfigSource.fromResourcePath
                .at(PropertyTreePath.$("BotConfig"))
            )
          }
        }
  }
}
