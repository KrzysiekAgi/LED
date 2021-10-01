package models

import play.api.libs.json.{ Format, Json }

case class DispatchResult(editor: String, editorPath: String, file: String, output: String)

object DispatchResult {
  implicit val jsonFormat: Format[DispatchResult] = Json.format
}