import com.zaxxer.hikari.HikariDataSource
import io.getquill.{Literal,CamelCase, Escape, JdbcContextConfig, MysqlJdbcContext, MysqlZioJdbcContext, NamingStrategy}
import io.getquill.context.ZioJdbc
import io.getquill.jdbczio.Quill
import io.getquill.util.LoadConfig
import zio.{Console, ZIO, ZIOAppDefault, ZLayer}

import javax.sql.DataSource

package object DB {

  object Ctx extends MysqlZioJdbcContext(NamingStrategy(Literal))

  object ZioDataSourceService {
    val layer: ZLayer[Any, Throwable, DataSource] = Quill.DataSource.fromPrefix("ctx")
  }

//  @accessible
//  object LiquibaseService {
//
//    type LiquibaseService = Has[Service]
//
//    type Liqui = Has[Liquibase]
//
//    trait Service {
//      def performMigration: RIO[Liqui, Unit]
//    }
//
//    class Impl extends Service {
//      override def performMigration: RIO[Liqui, Unit] = liquibase.map(_.update("dev"))
//    }
//
//    def mkLiquibase(config: Config): ZManaged[DataSource, Throwable, Liquibase] = for {
//      ds <- ZIO.environment[DataSource].map(_.get).toManaged_
//      fileAccessor <-  ZIO.effect(new FileSystemResourceAccessor()).toManaged_
//      classLoader <- ZIO.effect(classOf[LiquibaseService].getClassLoader).toManaged_
//      classLoaderAccessor <- ZIO.effect(new ClassLoaderResourceAccessor(classLoader)).toManaged_
//      fileOpener <- ZIO.effect(new CompositeResourceAccessor(fileAccessor, classLoaderAccessor)).toManaged_
//      jdbcConn <- ZManaged.makeEffect(new JdbcConnection(ds.getConnection()))(c => c.close())
//      liqui <- ZIO.effect(new Liquibase(config.liquibase.changeLog, fileOpener, jdbcConn)).toManaged_
//    } yield liqui
//
//    val liquibaseLayer: ZLayer[Configuration with DataSource, Throwable, Liqui] = ZLayer.fromManaged(
//      for {
//        config <- zio.config.getConfig[Config].toManaged_
//        liquibase <- mkLiquibase(config)
//      } yield (liquibase)
//    )
//
//
//    def liquibase: URIO[Liqui, Liquibase] = ZIO.service[Liquibase]
//
//    val live: ULayer[LiquibaseService] = ZLayer.succeed(new Impl)
//
//  }
}
