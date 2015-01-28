/**
 * 
 */
package com.lpi.sauvegarde.Sauvegarde;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.lpi.sauvegarde.AlarmReceiver;
import com.lpi.sauvegarde.R;

/**
 * @author lucien
 *
 */
public class Plannificateur
{
	private final static String TAG = "Sauvegarde" ; //$NON-NLS-1$
	
	private Context _context ;
	public final static String COMMANDE_SAVE_ALARM = "lpi.Sauvegarde.Alarme"; //$NON-NLS-1$
	
	public Plannificateur(Context context)
	{
		_context = context ;
	}
	
	/***
	 * Plannifie la prochaine alarme Supprime la precedente si elle existe
	 * 
	 * @param calendar
	 */
	public void setAlarm(Calendar calendar)
	{
		AlarmManager alarmManager = (AlarmManager) _context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(_context, AlarmReceiver.class);
		intent.setAction(COMMANDE_SAVE_ALARM);
		
		// Supprimer l'ancienne alarme
		PendingIntent pendingIntentCancel = PendingIntent.getBroadcast(_context, 0, intent, 0 );
		alarmManager.cancel(pendingIntentCancel);

		if (calendar != null)
		{
			Log.d(TAG, "Set alarme " +  calendar.get(Calendar.YEAR) + '/' + (calendar.get(Calendar.MONTH)+1) + '/' + calendar.get(Calendar.DAY_OF_MONTH) //$NON-NLS-1$
					+ ' ' + calendar.get(Calendar.HOUR_OF_DAY) + ':' + calendar.get(Calendar.MINUTE) + ':' + calendar.get(Calendar.SECOND)) ;
			PendingIntent pendingIntent = PendingIntent.getBroadcast(_context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT); 
			alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
		}
	}
	
	/***
	 * Retourne l'heure de la prochaine sauvegarde a partir de maintenant
	 * 
	 * @param c
	 * @return un Calendar ou null si sauvegarde desactivee
	 */
	static public Calendar getProchaineSauvegarde(Context c)
	{
		SharedPreferences settings = c.getSharedPreferences(Sauvegarde.PREFERENCES, Context.MODE_PRIVATE);
		boolean bActivee = settings.getBoolean(Sauvegarde.PREF_SAUVEGARDE_AUTO_ACTIVEE, false);

		if (!bActivee)
			return null;
		else
		{
			int heure = settings.getInt(Sauvegarde.PREF_SAUVEGARDE_AUTO_HEURE, 10);
			int minute = settings.getInt(Sauvegarde.PREF_SAUVEGARDE_AUTO_MINUTE, 0);

			Calendar calendar = Calendar.getInstance();
			setProchaineHeure(calendar, heure, minute);
			return calendar;
		}
	}
	


	/***
	 * Calcule le CALENDAR reprensentant l'heure de la prochaine sauvegarde
	 * 
	 * @param calendar
	 * @param heure
	 * @param minute
	 */
	static public void setProchaineHeure(Calendar calendar, int heure, int minute)
	{
		calendar.set(Calendar.HOUR_OF_DAY, heure);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, 0);

		while (calendar.compareTo(Calendar.getInstance()) < 0)
			calendar.add(Calendar.DAY_OF_YEAR, 1);
	}

	/***
	 * Changement de l'heure de la sauvegarde automatique
	 * 
	 * @param context
	 */
	public void plannifieSauvegarde()
	{
		Calendar calendar = getProchaineSauvegarde(_context);

		if (calendar == null)
		{
			// Pas de sauvegarde automatique
			Toast t = Toast.makeText(_context, Sauvegarde.formatResourceString(_context, R.string.sauvegarde_auto_desactivee),
					Toast.LENGTH_SHORT);
			t.show();

		} else
		{
			setAlarm(calendar);
			Toast t = Toast.makeText(_context,
					getTextProchaineSauvegarde(calendar),
					// "alarme dans " + (( calendar.getTimeInMillis() -
					// Calendar.getInstance().getTimeInMillis())/1000) + " secondes",
					Toast.LENGTH_SHORT);
			t.show();
		}
	}
	
	public String getTextProchaineSauvegarde(Calendar calendar)
	{
		if ( calendar == null)
			calendar = getProchaineSauvegarde(_context);
		return Sauvegarde.formatResourceString(_context, R.string.sauvegarde_auto_programmee, 
				Sauvegarde.getLocalizedTimeAndDate(_context, calendar)) ;
	}
}
