package controllers

import play.api._
import play.api.mvc._

import play.api.Logger

import play.api.Play.current

import com.independentsoft.exchange._

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.{Collections, Date}

import com.google.api.client.googleapis.auth.oauth2.{GoogleAuthorizationCodeFlow, GoogleClientSecrets, GoogleCredential}
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson.JacksonFactory
import java.io.{FileInputStream, File}
import com.google.api.services.calendar.CalendarScopes
import com.google.api.client.extensions.java6.auth.oauth2.{AuthorizationCodeInstalledApp, FileCredentialStore}
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver

import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.google.api.services.calendar.model.Events
import com.google.api.client.util.DateTime

import java.util.TimeZone

object Application extends Controller {

  val JSON_FACTORY = new JacksonFactory()
  val HTTP_TRANSPORT = new NetHttpTransport()
  val APPLICATION_NAME = "gcalexchsync_play"
  val INTECH_CAL = "i88utuhmrkt77n8sov0qp3kd80@group.calendar.google.com"

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def connect = Action {
    val service = new Service("https://exch.intech.lan/ews/Exchange.asmx", "fabrice.croiseaux", "virtual");

    val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    val startTime = dateFormat.parse("2013-04-08 00:00:00");
    val endTime = dateFormat.parse("2013-04-13 00:00:00");

    val view = new CalendarView(startTime, endTime);

    val response = service.findItem(StandardFolder.CALENDAR, AppointmentPropertyPath.getAllPropertyPaths(), view);
    (0 to response.getItems().size() - 1).map(
      i => response.getItems().get(i)
    ).filter(item => item.isInstanceOf[Appointment]).map {
      app =>
        val appointment = app.asInstanceOf[Appointment]
        Logger.debug("Id = " + appointment.getUid);
        Logger.debug("Subject = " + appointment.getSubject());
        Logger.debug("StartTime = " + appointment.getStartTime());
        Logger.debug("EndTime = " + appointment.getEndTime());
        Logger.debug("Body Preview = " + appointment.getBodyPlainText());
        Logger.debug("----------------------------------------------------------------");
    }
    Ok(response.toString)
  }

  def sync = Action {
    val service = new Service("https://exch.intech.lan/ews/Exchange.asmx", "fabrice.croiseaux", "virtual");

    val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    val startTime = dateFormat.parse("2013-01-01 00:00:00");
    val endTime = dateFormat.parse("2014-01-11 00:00:00");

    val view = new CalendarView(startTime, endTime);

    val response = service.findItem(StandardFolder.CALENDAR, AppointmentPropertyPath.getAllPropertyPaths(), view);

    Logger.info("Nb event :" + response.getItems().size().toString)
    (0 to response.getItems().size() - 1).map(
      i => response.getItems().get(i)
    ).filter(item => item.isInstanceOf[Appointment]).map {
      app =>
        val appointment = app.asInstanceOf[Appointment]
        insertGoogleEvent(googleEventFromExchange(appointment))
    }
    Ok(response.getItems().size().toString + " EVENTS SYNCHRONIZED")
  }

  def gcal = Action {
    val feed = client.calendarList().list().execute()
    val myCal = client.calendars().get(INTECH_CAL)
    val events = client.events().list(INTECH_CAL).execute()

    Ok(events.toString)
  }


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

  def insertEvent = Action {
    val result = client.events().insert(INTECH_CAL, newEvent).execute();
    Ok(result.toString)
  }

  def removeAllEvents = Action {
    val events = client.events().list(INTECH_CAL).execute().getItems()
    val it = events.iterator()
    while (it.hasNext) {
      client.events().delete(INTECH_CAL, it.next().getId()).execute()
    }
    Ok(events.size().toString + " EVENTS DELETED")
  }

  def insertGoogleEvent(ev: Event) = {
    client.events().insert(INTECH_CAL, ev).execute()
  }

  def newEvent = {
    val ev = new Event()
    ev.setSummary("Test event from Play!")
    val now = new Date()
    val start = new DateTime(now, TimeZone.getTimeZone("UTC"))
    ev.setStart(new EventDateTime().setDateTime(start))
    val end = new DateTime(new Date(now.getTime() + 1000 * 60 * 60), TimeZone.getTimeZone("UTC"))
    ev.setEnd(new EventDateTime().setDateTime(end))
    ev.setDescription("Description détaillée de l'event")
    ev
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