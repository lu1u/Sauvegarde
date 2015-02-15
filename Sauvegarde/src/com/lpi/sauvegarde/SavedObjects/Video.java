/**
 * 
 */
package com.lpi.sauvegarde.SavedObjects;

import android.content.Context;
import android.content.SharedPreferences;

import com.lpi.sauvegarde.R;
import com.lpi.sauvegarde.Mail.Mail;
import com.lpi.sauvegarde.Sauvegarde.Sauvegarde;

/**
 * @author lucien
 *
 */
public class Video extends SavedObject
{
	public String _absolutePath ;
	public String _displayName ;
	public String _description ;
	public long _dateTaken, _dateAdded, _dateModified ;
	
	@Override
	public void construitMail(Context c, Mail m)
	{
		try
		{
			m.setSubject(SavedObjectReader.getResourceString( R.string.sujet_video ,_displayName));
			
			StringBuilder b = new StringBuilder() ;
			b.append( SavedObjectReader.getResourceString( R.string.mail_nom_video ,_displayName)) ;
			b.append( SavedObjectReader.getResourceString( R.string.mail_description_video, _description)) ;
			b.append( SavedObjectReader.getResourceString( R.string.mail_date_taken_video, sqliteDateToString(c, _dateTaken))) ;
			b.append( SavedObjectReader.getResourceString( R.string.mail_date_added_video, sqliteDateToString(c, _dateAdded))) ;
			b.append( SavedObjectReader.getResourceString( R.string.mail_date_added_video, sqliteDateToString(c, _dateModified))) ;
			b.append(SavedObjectReader.getResourceString(R.string.message_fin_mail )) ;
			
			m.setBody(b.toString());
			m.addAttachment(_absolutePath);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public String identification(Context c)
	{
		return _displayName + ' ' + _absolutePath + ' ' + sqliteDateToString(c, _dateModified) ; 
	}
	/* (non-Javadoc)
	 * @see com.lpi.sauvegarde.SavedObjects.SavedObject#confirmeEnvoi(android.content.Context)
	 */
	@Override
	public void confirmeEnvoi(Context c)
	{
		SharedPreferences settings = c.getSharedPreferences(Sauvegarde.PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(Sauvegarde.PREF_DERNIERE_SAUVEGARDE_VIDEO, _dateModified);
		editor.commit();
	}
}
