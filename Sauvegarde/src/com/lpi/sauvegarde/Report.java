/**
 * 
 */
package com.lpi.sauvegarde;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.lpi.sauvegarde.Mail.Mail;

/**
 * @author lucien
 *
 */
@SuppressWarnings("nls")
public class Report
{
	public String _message;
	public String SauvegardeContacts = "";
	public String SauvegardeSMS = "";
	public String SauvegardeMMS = "";
	public String SauvegardePhotos = "";
	public String SauvegardeVideos = "";
	public String SauvegardeCallLog = "";
	private List<String> log = new ArrayList<String>();

	public String toString()
	{
		if (_message == null)
			return SauvegardeContacts + "\n" + SauvegardeCallLog + "\n" + SauvegardeSMS + "\n" + SauvegardeMMS + "\n"
					+ SauvegardePhotos + "\n" + SauvegardeVideos;
		else
			return _message;

	}

	public void FillMail(Mail m)
	{
		m.setSubject("[Sauvegarde Auto]  rapport " + getLocalizedDate());

		StringBuilder b = new StringBuilder();

		for (String s : log)
			b.append(s).append("\n");

		m.setBody(b.toString());
	}

	public void Log(String s)
	{
		log.add(getLocalizedDate() + ":" + s);
	}

	@SuppressWarnings("boxing")
	public static String getLocalizedDate(long date)
	{
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(date);

		return String.format("%02d/%02d/%02d %02d:%02d:%02d", c.get(Calendar.DAY_OF_MONTH),
				(c.get(Calendar.MONTH) + 1), c.get(Calendar.YEAR), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE),
				c.get(Calendar.SECOND)); // + ":" + c.get(Calendar.MILLISECOND) ;
	}

	public static String getLocalizedDate()
	{
		return getLocalizedDate(System.currentTimeMillis());
	}

	public void Log(Exception e)
	{
		_message = "Erreur lors de l'envoi d\'un mail, vérifiez les paramètres"; //$NON-NLS-1$

		Log(e.getLocalizedMessage());
		for (StackTraceElement s : e.getStackTrace())
			Log(s.getClassName() + '/' + s.getMethodName() + ':' + s.getLineNumber());

	}
}
