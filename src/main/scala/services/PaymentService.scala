package main.scala.services

import java.net.URL

import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, Reads}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}



class PaymentService @Inject()(implicit ws: WSClient, executionContext: ExecutionContext) extends Service {
  //TODO: Define this in a config file
  private val url = "https://my-json-server.typicode.com"
  val name = "PaymentService"
  val log = Logger(name)

  def get[A](path: String)(implicit reads: Reads[A]): Future[A] = {
    getJson(new URL(url + path)) flatMap { json =>
      val result = json.validate[A]
      result match {
        case JsSuccess(_, _) => Future.successful(result.get)
        case JsError(errors) =>
          log.error(s"Failed to parse json from $url. " + errors.mkString("\n"))
          Future.failed(new Throwable(s"$name - Failed to parse json from $url"))
      }
    }
  }
}
