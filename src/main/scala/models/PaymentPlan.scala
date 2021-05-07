package main.scala.models

import org.joda.time.LocalDate
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
                      ) {
  val startDate: LocalDate = new LocalDate(start_date)
}

object PaymentPlan {
  implicit val format: Format[PaymentPlan] = Json.format[PaymentPlan]

  val BI_WEEKLY = 14
  val WEEKLY = 7 // Weekly is half of 14 days

  def getFrequencyFromString(frequency: String): Int = {
    frequency match {
      case "BI_WEEKLY" => BI_WEEKLY
      case _ => WEEKLY // By default assuming weekly
    }
  }

  // If the amount to pay is grater than the total amount payed, then the Payment Plan is active
  //Assuming that while the total amount is not payed and there is Payment Plan the client needs to continue doing payments until payed off
  def isActive(paymentPlan: PaymentPlan, payments: Seq[Payment]): Boolean = {
    val totalAmountOfPaymentsDone: Double = payments.map(_.amount).sum
    paymentPlan.amount_to_pay > totalAmountOfPaymentsDone
  }

  //Remaining amount. Assuming that if the debt does not have payment plan, then the debt was paid in full,
  //Thus there is no Remaining amount to pay
  def getRemainingAmount(paymentPlan: PaymentPlan, payments: Seq[Payment]): Double = {
    val totalAmountOfPaymentsDone: Double = payments.map(_.amount).sum
    paymentPlan.amount_to_pay - totalAmountOfPaymentsDone
  }

  //This is to get the installment dates when client is delayed in payments
  //Example if last installment was 2020-02-20 but the client is not done yet and he did a last payment in 2020-08-08
  def getInstallmentDates(installmentDates: Seq[LocalDate], lastPaymentDateOpt: Option[LocalDate], frequency: Int): Seq[LocalDate] = {
    val lastInstallmentDateOpt: Option[LocalDate] = installmentDates.sortWith(_.isBefore(_)).lastOption

    (for {
      lastPaymentDate <- lastPaymentDateOpt
      lastInstallmentDate <- lastInstallmentDateOpt
    } yield {
       if (lastInstallmentDate.isBefore(lastPaymentDate)) {
         val newInstallmentDates = installmentDates :+ lastInstallmentDate.plusDays(frequency)
         getInstallmentDates(newInstallmentDates, lastPaymentDateOpt, frequency)
       } else {
         installmentDates
       }
    }).getOrElse(Seq())

  }

  def getNextPaymentDueDate(paymentPlan: PaymentPlan, payments: Seq[Payment]): Option[String] = {
    //Checking if there is still an active Payment Plan in order to proceed on calculating next payment due
    //Otherwise there is no payment due
    if (isActive(paymentPlan, payments)) {
      val frequency: Int = getFrequencyFromString(paymentPlan.installment_frequency)
      val lastPaymentDateOpt: Option[LocalDate] = payments.map(payment => new LocalDate(payment.date)).sortWith(_.isBefore(_)).lastOption
      val paymentsNeededToDo = Math.ceil(paymentPlan.amount_to_pay / paymentPlan.installment_amount)

      //Getting a list of all possible installment dates
      val installmentDates = List.fill(paymentsNeededToDo.toInt)(paymentPlan.startDate).foldLeft(List[LocalDate]()) {
        case (List(), e) => List(e)
        case (ls, _) => ls :+ ls.last.plusDays(frequency)
      }
      //Getting real installment dates including when client is delayed in payments
      val realInstallmentDates = getInstallmentDates(installmentDates, lastPaymentDateOpt, frequency)

      val nextPaymentDueDateOpt: Option[LocalDate] = lastPaymentDateOpt.flatMap(d => realInstallmentDates.find(_.isAfter(d)))
      nextPaymentDueDateOpt.map(_.toString("yyyy-MM-dd"))
    } else None
  }
}
