package db

import liquibase.Liquibase
import zio.{Config, Console, RIO, ZIO, ZLayer}

trait MigrationService {
  def performMigration: RIO[Liquibase, Unit]
}

object MigrationService{
  val live: ZLayer[Liquibase, Nothing, MigrationService] = ZLayer.fromFunction(MigrationServiceLive.apply _)
}

final case class MigrationServiceLive(liquibase: Liquibase) extends MigrationService {
  override def performMigration: RIO[Liquibase, Unit] = ZIO.succeed( liquibase.update() )
}