package main.scala.models

import play.api.libs.json.{Format, Json}

//Attributes in class are created as they are in the api json.
//Could be different format but I do this to avoid creating custom parsers
case class PaymentPlan(
                        id: Int,
                        debt_id: Int,
                        amount_to_pay: Double,
                        installment_frequency: String,
                        installment_amount: Double,
                        start_date: String
                      )

object PaymentPlan {
  implicit val format: Format[PaymentPlan] = Json.format[PaymentPlan]
}
