package models

import com.independentsoft.exchange._
import java.text.SimpleDateFormat
import play.api.{Play, Logger}
import com.google.api.services.calendar.model.{EventDateTime, Event}
import java.io.FileInputStream
import com.google.api.client.googleapis.auth.oauth2.{GoogleAuthorizationCodeFlow, GoogleClientSecrets}
import com.google.api.client.extensions.java6.auth.oauth2.{AuthorizationCodeInstalledApp, FileCredentialStore}
import java.util.{Date, TimeZone, Collections}
import com.google.api.services.calendar.CalendarScopes
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.json.jackson.JacksonFactory
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.util.DateTime

import play.api.Play.current

/**
 * Created with IntelliJ IDEA.
 * User: croiseaux
 * Date: 14/04/13
 * Time: 17:46
 * To change this template use File | Settings | File Templates.
 */
object CalendarSynchronizer {

  val JSON_FACTORY = new JacksonFactory()
  val HTTP_TRANSPORT = new NetHttpTransport()
  val APPLICATION_NAME = "gcalexchsync_play"
  val INTECH_CAL = "i88utuhmrkt77n8sov0qp3kd80@group.calendar.google.com"


  def credential = {
    // load client secrets
    val secret = new FileInputStream(Play.getFile("conf/client_secrets.json"))
    val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, secret)

    // set up file credential store
    val credentialStore = new FileCredentialStore(Play.getFile("conf/calendar.json"), JSON_FACTORY)
    // set up authorization code flow
    val flow = new GoogleAuthorizationCodeFlow.Builder(
      HTTP_TRANSPORT, JSON_FACTORY, clientSecrets,
      Collections.singleton(CalendarScopes.CALENDAR)).setCredentialStore(credentialStore).build()
    // authorize
    new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user")
  }

  val client = new com.google.api.services.calendar.Calendar.Builder(
    HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(
    APPLICATION_NAME).build()

  def syncAfter(startTime: Date) = {
    val service = new Service("https://exch.intech.lan/ews/Exchange.asmx", "fabrice.croiseaux", "virtual")

    val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val endTime = new Date(startTime.getTime + 1000 * 60 * 60 * 24 * 365)

    val view = new CalendarView(startTime, endTime)

    val response = service.findItem(StandardFolder.CALENDAR, AppointmentPropertyPath.getAllPropertyPaths(), view)

    Logger.info("Nb event :" + response.getItems().size().toString)
    (0 to response.getItems().size() - 1).map(
      i => response.getItems().get(i)
    ).filter(item => item.isInstanceOf[Appointment]).map {
      app =>
        val appointment = app.asInstanceOf[Appointment]
        insertGoogleEvent(googleEventFromExchange(appointment))
    }
    response.getItems().size()
  }


  def removeAllEventsAfter(startDate: Date) = {
    val events = client.events().list(INTECH_CAL).execute().getItems()
    var nbEvents = 0
    if (events != null) {
      val it = events.iterator()
      Logger.info("Now : " + startDate)
      while (it.hasNext) {
        val event = it.next()
        Logger.info("Event : " + event.getStart.getDateTime)
        //   if (event.getStart.getDateTime.getValue > startDate.getTime) {
        client.events().delete(INTECH_CAL, event.getId()).execute()
        nbEvents = nbEvents + 1
        //   }
      }
    }
    nbEvents
  }

  def insertGoogleEvent(ev: Event) = {
    client.events().insert(INTECH_CAL, ev).execute()
  }

  def googleEventFromExchange(app: Appointment) = {
    val ev = new Event()
    ev.setSummary(app.getSubject())
    ev.setDescription(app.getBodyPlainText())
    val start = new DateTime(app.getStartTime(), TimeZone.getTimeZone("UTC"))
    ev.setStart(new EventDateTime().setDateTime(start))
    val end = new DateTime(app.getEndTime(), TimeZone.getTimeZone("UTC"))
    ev.setEnd(new EventDateTime().setDateTime(end))
    ev
  }

}
