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
		String adress = getContactFromNumber(c, _number) ;
		switch( _type )
		{
		case CallLog.Calls.INCOMING_TYPE :
		case CallLog.Calls.MISSED_TYPE :
				m.setSubject(SavedObjectReader.getResourceString(R.string.sujet_calllog_incoming,
							getContactFromNumber( c, _number)));
			break ;
		case CallLog.Calls.OUTGOING_TYPE :
			m.setSubject(SavedObjectReader.getResourceString(R.string.sujet_calllog_outgoing,
							getContactFromNumber( c, _number)));
			break ;
		default:
			m.setSubject(SavedObjectReader.getResourceString(R.string.sujet_calllog_unkown,
							getContactFromNumber( c, _number)));
			break ;
		}
		
		// Corps du message
		m.setBody(SavedObjectReader.getResourceString(R.string.appel_body,
				sqliteDateToString(c, _date), 
				sqliteDurationToString(c, _duration),
				SavedObjectReader.getResourceString(R.string.message_fin_mail)));
	}

	/* (non-Javadoc)
	 * @see com.lpi.sauvegarde.SavedObjects.SavedObject#identification(android.content.Context)
	 */
	@Override
	public String identification(Context c)
	{
		return getContactFromNumber( c, _number) + ' ' + sqliteDateToString(c, _date) ;
	}
	

	/* (non-Javadoc)
	 * @see com.lpi.sauvegarde.SavedObjects.SavedObject#confirmeEnvoi(android.content.Context)
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
