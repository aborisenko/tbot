package models

import org.joda.time.DateTime

import java.time.LocalDateTime

case class Deal(uid: Option[String] = None,
                date: Option[LocalDateTime] = None,
                id: Option[String] = None,
                `type`: Option[String] = None,
                buy: Option[BigDecimal] = None,
                buy_currency_id: Option[String] = None,
                buy_rate: Option[Float] = None,
                sell: Option[BigDecimal] = None,
                sell_currency_id: Option[String] = None,
                sell_rate: Option[Float] = None,
                spread: Option[Float] = None,
                profit: Option[Float] = None,
                bonus: Option[Float] = None
               )
