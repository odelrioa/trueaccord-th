package main.scala.models

import play.api.libs.json.{Format, Json}

//Attributes in class are created as they are in the api json.
//Could be different format but I do this to avoid creating custom parsers
case class Payment(
                    payment_plan_id: Int,
                    amount: Double,
                    date: String
                  )

object Payment {
  implicit val format: Format[Payment] = Json.format[Payment]
}


