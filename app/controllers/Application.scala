package controllers

import play.api.mvc._

import play.api.Logger
import models.CalendarSynchronizer._
import java.util.Date


object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }


  def removeFromNow = Action {
    val nb = removeAllEventsAfter(new Date())
    Ok(nb.toString + " events removed")
  }

  def sync = Action {
    val nb = syncAfter(new Date())
    Ok(nb.toString + " events syncrhonized")
  }

}