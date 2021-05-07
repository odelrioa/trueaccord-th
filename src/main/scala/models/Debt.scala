package main.scala.models

import play.api.libs.json.{Format, Json}

//Attributes in class are created as they are in the api json.
//Could be different format but I do this to avoid creating custom parsers
case class Debt(id: Int, amount: Double)

object Debt {
  implicit val format: Format[Debt] = Json.format[Debt]
}
