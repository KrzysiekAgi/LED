package controllers

import models.DispatchResult
import play.api.Configuration
import play.api.libs.json.{ Format, Json }
import play.api.mvc._

import java.io.File
import java.net.URLDecoder
import javax.inject._
import scala.sys.process._
import scala.util.{ Failure, Success, Try }

@Singleton
class LEDController @Inject() (
    config: Configuration,
    val controllerComponents: ControllerComponents
) extends BaseController {

  def dispatch(editor: String, file: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>

    dispatchFile(editor, file) match {
      case Success(result)    => Ok(Json.toJson(result))
      case Failure(exception) => BadRequest(exception.getMessage)
    }
  }

  private def dispatchFile(editor: String, file: String): Try[DispatchResult] = {

    def getEditorPath: Try[String] = config.getOptional[String](s"editors.$editor") match {
      case Some(path) => Success(path)
      case None       => Failure(new IllegalArgumentException(s"No config found for editor '$editor'"))
    }

    def editorPathExists(editorPath: String): Try[String] = if (new File(editorPath).exists()) {
      Success(editorPath)
    } else {
      Failure(new IllegalStateException(s"Editor '$editor' expected at path '$editorPath' but not found"))
    }

    def filePathExists: Try[String] = {
      val utf8file = URLDecoder.decode(file, "UTF-8")
      if (new File(utf8file).exists()) {
        Success(utf8file)
      } else {
        Failure(new IllegalStateException(s"Can't edit file '$utf8file', it doesn't exist"))
      }
    }

    for {
      editorPath <- getEditorPath
      _ <- editorPathExists(editorPath)
      filePath <- filePathExists
    } yield {
      val cmd = s"""$editorPath "$filePath""""
      val output = cmd.!!
      DispatchResult(editor, editorPath, filePath, output)
    }
  }
}
