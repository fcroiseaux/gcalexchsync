package controllers

import play.api._
import play.api.mvc._

import play.api.Logger

import com.independentsoft.exchange._

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.{Collections, Date}
;

import com.google.api.client.googleapis.auth.oauth2.{GoogleAuthorizationCodeFlow, GoogleClientSecrets, GoogleCredential}
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson.JacksonFactory
import java.io.File
import com.google.api.services.calendar.CalendarScopes
import com.google.api.client.extensions.java6.auth.oauth2.{AuthorizationCodeInstalledApp, FileCredentialStore}
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver


object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def connect = Action {
    val service = new Service("https://exch.intech.lan/ews/Exchange.asmx", "fabrice.croiseaux", "virtual");

    val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    val startTime = dateFormat.parse("2013-03-01 00:00:00");
    val endTime = dateFormat.parse("2013-06-01 00:00:00");

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


  def authorize = {
    // load client secrets
    val JSON_FACTORY = new JacksonFactory()
    val HTTP_TRANSPORT = new NetHttpTransport()
    val secretDetails = new GoogleClientSecrets.Details()
    secretDetails.setClientId("")
    secretDetails.setClientSecret("")
    val clientSecrets = new GoogleClientSecrets()
    clientSecrets.setInstalled(secretDetails)

    // set up file credential store
    val credentialStore = new FileCredentialStore(
      new File(System.getProperty("user.home"), ".credentials/calendar.json"), JSON_FACTORY);
    // set up authorization code flow
    val flow = new GoogleAuthorizationCodeFlow.Builder(
      HTTP_TRANSPORT, JSON_FACTORY, clientSecrets,
      Collections.singleton(CalendarScopes.CALENDAR)).setCredentialStore(credentialStore).build();
    // authorize
    new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
  }


}