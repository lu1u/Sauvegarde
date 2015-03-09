/**
 * 
 */
package com.lpi.sauvegarde.SavedObjects;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.CallLog;

import com.lpi.sauvegarde.R;
import com.lpi.sauvegarde.Mail.Mail;
import com.lpi.sauvegarde.Sauvegarde.Sauvegarde;

/**
 * @author lucien
 *
 */
public class AppelTelephonique extends SavedObject
{
	public String _ID ;
	public String _number ;
	public long _date ;
	public long _duration ;
	public int _type ;
	
	/***
	 * Construction du mail a partir des informations de l'appel telephonique
	 */
	@Override
	public void construitMail(Context c, Mail m) throws Exception
	{
		// Sujet
		String adresse = getContactFromNumber(c, _number) ;
		switch( _type )
		{
		case CallLog.Calls.INCOMING_TYPE :
		case CallLog.Calls.MISSED_TYPE :
				m.setSubject(SavedObjectReader.getResourceString(R.string.sujet_calllog_incoming,
						adresse));
			break ;
		case CallLog.Calls.OUTGOING_TYPE :
			m.setSubject(SavedObjectReader.getResourceString(R.string.sujet_calllog_outgoing,
					adresse));
			break ;
		default:
			m.setSubject(SavedObjectReader.getResourceString(R.string.sujet_calllog_unkown,
					adresse));
			break ;
		}
		
		// Corps du message
		m.setBody(SavedObjectReader.getResourceString(R.string.appel_body,
				adresse,
				sqliteDateToString(c, _date), 
				sqliteDurationToString(c, _duration),
				SavedObjectReader.getResourceString(R.string.message_fin_mail)));
	}

	/***
	 * Retourne une chaine de caracteres qui permet d'identifier cet appel de facon unique
	 */
	@Override
	public String identification(Context c)
	{
		return getContactFromNumber( c, _number) + ' ' + sqliteDateToString(c, _date) ;
	}
	

	/***
	 * Operation a effectuer quand le mail a ete envoye
	 */
	@Override
	public void confirmeEnvoi(Context c)
	{
		SharedPreferences settings = c.getSharedPreferences(Sauvegarde.PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(Sauvegarde.PREF_DERNIERE_JOURNAL_TELEPHONE, _date);
		editor.commit();
	}

}
