package dao

import io.getquill.jdbczio.Quill
import io.getquill.Literal
import models.deals
import zio.{ZIO, ZLayer}

import java.sql.SQLException

class DealDAO(quill: Quill.Mysql[Literal]){
  import quill._
  def insert(d: deals): ZIO[Any, SQLException, Long] = run(quote( query[deals].insertValue(lift(d)) ))
  def insertMany(l: List[deals]): ZIO[Any, SQLException, List[Long]] = run {
    quote {
      liftQuery(l).foreach(d => query[deals].insertValue(d))
    }
  }
}

object DealDAO {
//  def insert(d: deals): ZIO[DealDAO, SQLException, Long] =
//    ZIO.serviceWithZIO[DealDAO](_.insert(d))

  def insertMany(d: List[deals]): ZIO[DealDAO, SQLException, List[Long]] =
    ZIO.serviceWithZIO[DealDAO](_.insertMany(d))

  val live = ZLayer.fromFunction(new DealDAO(_))
}