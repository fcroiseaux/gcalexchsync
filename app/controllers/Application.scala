package controllers

import play.api.mvc._

import play.api.Logger
import models.CalendarSynchronizer._
import java.util.Date
import java.text.SimpleDateFormat


object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }


  def removeFromNow = Action {
    val nb = removeAllEventsAfter(new Date())
    Ok(nb.toString + " events removed")
  }

  def syncFromNow = Action {
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val nb = syncBetween(new Date(), dateFormat.parse("2014-01-01 00:00:00"))
    Ok(nb.toString + " events syncrhonized")
  }

  def syncAll = Action {
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val nb = syncBetween(dateFormat.parse("2013-01-01 00:00:00"), dateFormat.parse("2014-01-01 00:00:00"))
    Ok(nb.toString + " events syncrhonized")
  }

  def removeAll = Action {
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val nb = removeAllEventsAfter(dateFormat.parse("2012-01-01 00:00:00"))
    Ok(nb.toString + " events removed")
  }

  def test = Action {
    val ev = client.events().list(INTECH_CAL).execute()
    Ok(ev.getItems.toString)
  }

  def test1 = Action {
    val cals = client.events().insert(INTECH_CAL, newGoogleEvent()).execute()
    Ok(cals.toString)
  }

  def test2 = Action {
    val cals = client.calendarList().list().execute()
    val it = cals.getItems.iterator()
    while (it.hasNext) {
      val cal = it.next()
      Logger.info(cal.getId.toString + " " + cal.getSummary + " " + cal.getPrimary)
    }
    Ok("DONE")
  }
}