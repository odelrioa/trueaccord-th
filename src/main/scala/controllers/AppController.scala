package main.scala.controllers

import main.scala.models.{Debt, Payment, PaymentPlan}
import main.scala.services.PaymentService
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

object AppController extends App {
  lazy val injector = new GuiceApplicationBuilder().injector()
  implicit def ws: WSClient = injector.instanceOf[WSClient]

  //TODO: This could be in a cofig file. Putting here to keep things simple
  val apiPath = "/druska/trueaccord-mock-payments-api"
  val paymentService = new PaymentService()

  val debtsF: Future[Seq[Debt]] = paymentService.get[Seq[Debt]](s"$apiPath/debts")
  val paymentPlansF: Future[Seq[PaymentPlan]] = paymentService.get[Seq[PaymentPlan]](s"$apiPath/payment_plans")
  val paymentsF: Future[Seq[Payment]] = paymentService.get[Seq[Payment]](s"$apiPath/payments")

  def getDebts(debtsF: Future[Seq[Debt]], paymentPlansF: Future[Seq[PaymentPlan]], paymentsF: Future[Seq[Payment]]) = {
    for {
      debts <- debtsF
      paymentPlans <- paymentPlansF
      payments <- paymentsF
    } yield {
      debts.map { debt =>
        //Here if the debt does not have Payment Plan, then the value for is_in_payment_plan is going to be false
        val paymentPlanOpt = paymentPlans.find(_.debt_id.equals(debt.id))
        val debtPayments = payments.filter(_.payment_plan_id.equals(paymentPlanOpt.map(_.id).getOrElse(-1)))
        //Check if is in an active plan
        val isPaymentPlanActive = paymentPlanOpt.exists(PaymentPlan.isActive(_, debtPayments))
        //Get the remaining amount to pay for a debt
        val remainingAmount: Double = paymentPlanOpt.map(PaymentPlan.getRemainingAmount(_, debtPayments)).getOrElse(0)
        //Get next payment due date if exists
        val nextPaymentDueDateOpt: Option[String] = paymentPlanOpt.flatMap(PaymentPlan.getNextPaymentDueDate(_, debtPayments))

        val newDebt = debt.copy(
          is_in_payment_plan = Some(isPaymentPlanActive),
          remaining_amount = Some(remainingAmount),
          next_payment_due_date = nextPaymentDueDateOpt
        )
        newDebt
      }
    }
  }

  getDebts(debtsF, paymentPlansF, paymentsF).map { debtList =>
    println("################################################################")
    println("Fasten your seat belt. We are ready for take off")
    println("################################################################\n\n")
    debtList.foreach(d  => println(Json.toJson(d)))
  }
}
