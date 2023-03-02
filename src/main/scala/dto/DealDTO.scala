package dto

import models.deals

import java.time.LocalDateTime
import scala.util.Try
case class DealDTO(id: Option[String] = None,
                   date: Option[java.util.Date] = None,
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

  def profit(d: deals): Option[BigDecimal] = for {
    br <- d.buy_rate
    sr <- d.sell_rate
    t <- d.`type`
    b <- d.buy
    s <- d.sell
  } yield t match {
      case "B" => (sr-br)*s
      case "S" => (br-sr)*b
  }

  def profitUSD(d: deals): Option[BigDecimal] = for {
    ba <- d.buy
    sa <- d.sell
    t <- d.`type`
  } yield t match {
    case "B" => sa-ba
    case "S" => ba-sa
  }

  def bonus(p: Option[BigDecimal]) = p.map(_ * .3f)
  def toDeal(d: DealDTO): deals = {
    val dd = deals(
      uid = None,
      date = d.date,
      id = d.id,
      `type` = d.transactionType.map(_.toUpperCase()) match {
        case Some("ПОКУПКА") => Some("B")
        case Some("ПРОДАЖА") => Some("S")
        case _ => None
      },
      buy = d.buyAmount.flatMap(a => Try(BigDecimal(a.replace(",","."))).toOption),
      buy_currency_id = d.buyCurrency,
      buy_rate = d.buyRate.flatMap(r => Try(r.replace(",",".").toFloat).toOption),
      sell = d.sellAmount.flatMap(a => Try(BigDecimal(a.replace(",","."))).toOption),
      sell_currency_id = d.sellCurrency,
      sell_rate = d.sellRate.flatMap(r => Try(r.replace(",",".").toFloat).toOption),
      spread = None,
      profit = None,
      bonus = None
    )

    val p = profit(dd) orElse profitUSD(dd)

    dd.copy(profit = p, bonus = bonus(p))
  }
}