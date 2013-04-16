import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.duration.DurationInt
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.{Actor, Props}
import play.api._
import models.CalendarSynchronizer._
import org.joda.time._


object Global extends GlobalSettings {

  override def onStart(app: Application) = {
    val syncActor = Akka.system.actorOf(Props[SyncActor], name = "syncActor")

    Akka.system.scheduler.schedule(0.seconds, 60.minutes, syncActor, Sync)

  }

}

case object Sync

class SyncActor extends Actor {

  def receive = {
    case Sync => {
      val firstDayOfWeek = new LocalDate().withDayOfWeek(1)
      val nbDel = removeAllEventsAfter(firstDayOfWeek.toDate())
      Logger.info(new LocalDateTime().toString + " - " + nbDel + " events removed")
      val endOfPeriod = firstDayOfWeek.plusMonths(6)
      val nbIns = syncBetween(firstDayOfWeek.toDate, endOfPeriod.toDate)
      Logger.info(new LocalDateTime().toString + " - " + nbIns + " events inserted")
      Logger.info("*******************************************")
    }
  }
}