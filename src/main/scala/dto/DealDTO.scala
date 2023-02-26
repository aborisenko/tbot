package dto

import models.Deal
import org.joda.time.DateTime

import scala.util.Try
case class DealDTO(id: Option[String] = None,
                   date: Option[DateTime] = None,
                   transactionType: Option[String] = None,

                   buyAmount: Option[String] = None,
                   buyCurrency: Option[String] = None,
                   buyRate: Option[String] = None,

                   sellAmount: Option[String] = None,
                   sellCurrency: Option[String] = None,
                   sellRate: Option[String] = None,

                   commission: Option[String] = None,
                   unrecognized: List[String] = Nil
                  )

object DealDTO {
  def toDeal(d: DealDTO): Deal = {
    Deal(
      uid = None,
      date = None,
      id = d.id,
      `type` = d.transactionType.map(_.toUpperCase()) match {
        case Some("ПОКУПКА") => Some("B")
        case Some("ПРОДАЖА") => Some("S")
        case _ => None
      },
      buy = d.buyAmount.flatMap(a => Try(BigDecimal(a)*100).toOption),
      buy_currency_id = d.buyCurrency,
      buy_rate = d.buyRate.flatMap(r => Try(r.toFloat).toOption),
      sell = d.sellAmount.flatMap(a => Try(BigDecimal(a)*100).toOption),
      sell_currency_id = d.sellCurrency,
      sell_rate = d.sellRate.flatMap(r => Try(r.toFloat).toOption),
      spread = None,
      profit = None,
      bonus = None
    )
  }
}