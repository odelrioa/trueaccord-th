package controller

import main.scala.controllers.AppController
import main.scala.models.{Debt, Payment, PaymentPlan}
import main.scala.services.PaymentService
import org.scalatest._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AppControllerSpec extends FlatSpec with Matchers {
  "The printDebts" should "get the list of debts" in {

    lazy val injector = new GuiceApplicationBuilder().injector()
    implicit def ws: WSClient = injector.instanceOf[WSClient]
    val apiPath = "/druska/trueaccord-mock-payments-api"
    val paymentService = new PaymentService()

    //This requests/responses should be mocked
    //Tests should not execute real requests
    //For timing purposes just doing it this way, I only want to be able to test the functionality
    val debtsF: Future[Seq[Debt]] = paymentService.get[Seq[Debt]](s"$apiPath/debts")
    val paymentPlansF: Future[Seq[PaymentPlan]] = paymentService.get[Seq[PaymentPlan]](s"$apiPath/payment_plans")
    val paymentsF: Future[Seq[Payment]] = paymentService.get[Seq[Payment]](s"$apiPath/payments")

    val debts = AppController.getDebts(debtsF, paymentPlansF, paymentsF)
    debts.map{ debtList =>
      debtList.size should be > 0
      debtList.foreach{ debt =>
        debt.is_in_payment_plan.isDefined shouldEqual true
        debt.remaining_amount.isDefined shouldEqual true
      }
    }
  }
}
