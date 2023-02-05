import zio.{ZIO, ZLayer}

import javax.sql.DataSource
import liquibase._
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor

import java.io.IOException
package object db {

  object LiquibaseService {

    private def mkLiquibase: ZIO[DataSource, Nothing, liquibase.Liquibase] = for {
      ds <- ZIO.service[DataSource]
      classLoaderAccessor <- ZIO.succeed(new ClassLoaderResourceAccessor())
      jdbcConn <- ZIO.succeed(new JdbcConnection(ds.getConnection()))
      liqui <-ZIO.succeed(new Liquibase("liquibase/main.xml", classLoaderAccessor, jdbcConn))
    } yield liqui

    val live: ZLayer[DataSource, IOException, liquibase.Liquibase] =
      ZLayer.fromZIO(mkLiquibase)
  }
}
