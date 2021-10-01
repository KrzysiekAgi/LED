package controllers

import com.typesafe.config.ConfigFactory
import play.api.mvc._

import java.net.URLDecoder
import javax.inject._
import scala.util.{Failure, Success, Try}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class LEDController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def dispatch(editor: String, file: String) = Action { implicit request: Request[AnyContent] =>
    val result: Try[String] = openFile(editor, file)
    result match {
      case Success(out) => Ok(s"Open file with $editor, result: $out")
      case Failure(ex) => BadRequest(ex.getMessage)
    }
  }

  private def openFile(editorName: String, file: String): Try[String] = {
    val conf = ConfigFactory.load
    try {
      val editorResult = Option(conf.getString(s"editors.$editorName"))
      if (editorResult.isEmpty) {
        return new Failure[String](new IllegalArgumentException(s"There is no mapping for editor: $editorName"))
      }

      import scala.sys.process._
      val decoded = URLDecoder.decode(file, "UTF-8")
      Seq(s"${editorResult.get} $decoded").run()
      Success(s"${editorResult.get} $decoded")
    } catch {
      case ex: Exception => new Failure[String](ex)
    }
  }
}
