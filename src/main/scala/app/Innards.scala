package app

import im.mange.sews.{Config, JsonCodec, Program}
import elmtype.ElmTypeShapeless._
import elmtype.{ElmTypeMain, _}
import im.mange.sews.db.{Db, FileStore}
import io.shaka.http.Http.HttpHandler
import io.shaka.http.Request.GET
import io.shaka.http.StaticResponse.static
import shapeless._


object Types extends ElmTypeMain(Shared.types)


object Shared {
  val types = ToElmTypes[ToServer :: FromServer :: HNil]().apply
}


object Codecs {
  import argonaut.DecodeJson
  import argonaut._, ArgonautShapeless._ //TIP: do not optimise imports

  val dbCodec = JsonCodec(DecodeJson.of[ServerModel], EncodeJson.of[ServerModel])
  val msgCodec = JsonCodec(DecodeJson.of[ToServer], EncodeJson.of[FromServer])
}


object Endpoints {
  private val root = "src/main/resources"

  val all: HttpHandler = {
    case GET("/")  => static(root, "/index.html")
    case GET(path) => static(root, path)
  }
}


//TODO: could program be a function in main?
object Configs {
  private val db = Db(FileStore("target"), Codecs.dbCodec)

  //TODO: be nice if we had an init function defined in main
  val default = Config(Endpoints.all,
    Program(
      model = db.loadOrElse("counter", ServerModel(0)),
      update = ServerUpdate(Codecs.msgCodec, db),
      updateDebug = true
    ))
}
