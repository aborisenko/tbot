import com.zaxxer.hikari.HikariDataSource
import io.getquill.{CamelCase, Escape, JdbcContextConfig, Literal, MysqlJdbcContext, MysqlZioJdbcContext, NamingStrategy}
import io.getquill.context.ZioJdbc
import io.getquill.jdbczio.Quill
import io.getquill.util.LoadConfig
import zio.{Console, RIO, ULayer, URIO, ZIO, ZIOAppDefault, ZLayer}

import javax.sql.DataSource
import liquibase._
import _configuration.LiquibaseConfig
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.{ClassLoaderResourceAccessor, FileSystemResourceAccessor}
import shapeless.Lazy.apply
import zio.managed.ZManaged
package object DB {

  object Ctx extends MysqlZioJdbcContext(NamingStrategy(Literal))

  object ZioDataSourceService {
    val layer: ZLayer[Any, Throwable, DataSource] = Quill.DataSource.fromPrefix("ctx")
  }

  object LiquibaseService {

    type LiquibaseService = Service

    trait Service {
      def performMigration: RIO[Liquibase, Unit]
    }

    class Impl extends Service {
      override def performMigration = liquibase.map(_.update())
    }

    def mkLiquibase(config: LiquibaseConfig): ZIO[DataSource, Throwable, Liquibase] = for {
      ds <- ZIO.service[DataSource]
      fileOpener <- ZIO.attempt(new ClassLoaderResourceAccessor())
      jdbcConn <- ZIO.attempt(new JdbcConnection(ds.getConnection()))
      liqui <- ZIO.attempt(new Liquibase(config.changeLog, fileOpener, jdbcConn))
    } yield liqui

    val liquibaseLayer: ZLayer[DataSource, Throwable, Liquibase] = ZLayer.fromZIO(
      for {
//        config <- zio.config.getConfig[Config].toManaged_
        liquibase <- mkLiquibase(LiquibaseConfig("liquibase/main.xml"))
      } yield (liquibase)
    )


    def liquibase = ZIO.service[Liquibase]

    val live: ULayer[LiquibaseService] = ZLayer.succeed(new Impl)

  }
}
