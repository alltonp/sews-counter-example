package app

import im.mange.sews.LaunchApplication
import im.mange.sews._
import im.mange.sews.db.{Db, DbCmd}
import im.mange.sews.JsonCodec


object Main extends App { LaunchApplication(9000, Configs.default) }


//MODEL
case class ServerModel(count: Int)


//MSG
sealed trait ToServer
case class Init() extends ToServer
case class Increment() extends ToServer
case class Decrement() extends ToServer

sealed trait FromServer
case class ModelUpdated(model: ServerModel) extends FromServer


//UPDATE
//TODO: could serverUpdate be a function too?
case class ServerUpdate(msgCodec: JsonCodec[ToServer, FromServer], db: Db[ServerModel]) extends Update[ToServer, ServerModel, FromServer] {

  private val wsCmd = WsCmd(msgCodec, subscribers)
  private val dbCmd = DbCmd(db)

  override def update(msg: ToServer, model: ServerModel, from: Option[Subscriber]): (ServerModel, Cmd) = {
    msg match {

      case Init() =>
        (model, wsCmd.send(ModelUpdated(model), from))

      case Increment() =>
        val model_ = model.copy(count = model.count + 1)
        (model_, Cmd.batch(
          dbCmd.save("counter", model_),
          wsCmd.sendAll(ModelUpdated(model_))
        ))

      case Decrement() =>
        val model_ = model.copy(count = model.count - 1)
        (model_, Cmd.batch(
          dbCmd.save("counter", model_),
          wsCmd.sendAll(ModelUpdated(model_))
        ))

    }
  }
}

