package main.scala.models

import play.api.libs.json.{Format, JsPath, Json, Writes}
import play.api.libs.functional.syntax._

//Attributes in class are created as they are in the api json.
//Could be different format but I do this to avoid creating custom parsers
case class Debt(id: Int,
                amount: Double,
                is_in_payment_plan: Option[Boolean],
                remaining_amount: Option[Double],
                next_payment_due_date: Option[String])

object Debt {
  implicit val reads = Json.reads[Debt]
  //Implementing the writes in order to parse next_payment_due_date into a null when there is no value
  implicit val writes: Writes[Debt] = (
    (JsPath \ "id").write[Int] and
    (JsPath \ "amount").write[Double] and
      (JsPath \ "is_in_payment_plan").writeNullable[Boolean] and
      (JsPath \ "remaining_amount").writeNullable[Double] and
      (JsPath \ "next_payment_due_date").writeOptionWithNull[String]
    )(unlift(Debt.unapply))
}
