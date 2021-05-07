package services

import java.net.URL

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.ws.WSClient
import play.api.libs.json.JsValue
import play.mvc.Http.Status._


trait Service {

  val name: String

  def getJson(url: URL)(implicit ws: WSClient, executionContext: ExecutionContext): Future[JsValue] = {
    ws.url(url.toString).withHttpHeaders(("Accept", "application/json")).get flatMap { response =>
      response.status match {
        case OK => Future(response.json)
        case _ => Future.failed(new Throwable(s"${response.status} ${response.statusText} $url"))
      }
    } recoverWith {
      case exception => Future.failed(new Throwable(s"Service call failed for $url: $exception"))
    }
  }

}
