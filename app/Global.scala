import java.util.Date
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.duration.DurationInt
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.{Actor, Props}
import play.api._


object Global extends GlobalSettings {

  override def onStart(app: Application) = {
    val syncActor = Akka.system.actorOf(Props[SyncActor], name = "syncActor")

    Akka.system.scheduler.schedule(0.seconds, 2.minutes, syncActor, Sync)

  }

}

case object Sync

class SyncActor extends Actor {

  def receive = {
    case Sync => {
      Logger.info(new Date().toString + " - Remove events")
      Logger.info(new Date().toString + " - Synchronize events")
      Logger.info("*******************************************")
    }
  }
}