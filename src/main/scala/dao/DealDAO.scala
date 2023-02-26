package dao

import io.getquill.jdbczio.Quill
import io.getquill.{CompositeNamingStrategy2, Literal, SnakeCase}
import models.Deal
import zio.{ZIO, ZLayer}

import java.sql.SQLException

class DealDAO(quill: Quill.Mysql[Literal]){
  import quill._
  def insert(d: Deal): ZIO[Any, SQLException, Long] = run(quote( query[Deal].insertValue(lift(d)) ))
  def insertMany(l: List[Deal]): ZIO[Any, SQLException, List[Long]] = run {
    quote {
      liftQuery(l).foreach(d => query[Deal].insertValue(d))
    }
  }
}

object DealDAO {
//  def insert(d: Deal): ZIO[DealDAO, SQLException, Long] =
//    ZIO.serviceWithZIO[DealDAO](_.insert(d))

  def insertMany(d: List[Deal]): ZIO[DealDAO, SQLException, List[Long]] =
    ZIO.serviceWithZIO[DealDAO](_.insertMany(d))

  val live = ZLayer.fromFunction(new DealDAO(_))
}