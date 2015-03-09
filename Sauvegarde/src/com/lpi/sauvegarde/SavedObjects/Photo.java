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
public class Photo extends SavedObject
{
	public String _absolutePath;
	public String _displayName;
	public String _description;
	public long _dateTaken, _dateAdded, _dateModified;

	@Override
	public String identification(Context c)
	{
		return _displayName + ' ' + _absolutePath + ' ' + sqliteDateToString(c, _dateModified) ;
	}
	
	@Override
	public void construitMail(Context c, Mail m) throws Exception
	{
		m.setSubject(SavedObjectReader.getResourceString(R.string.sujet_photo, _displayName, sqliteDateToString(c, _dateTaken)) );

		StringBuilder b = new StringBuilder();
		b.append(SavedObjectReader.getResourceString(R.string.mail_nom_photo, _displayName));

		if (_description != null)
			b.append(SavedObjectReader.getResourceString(R.string.mail_description_photo, _description));

		b.append(SavedObjectReader.getResourceString(R.string.mail_date_taken_photo,
					sqliteDateToString(c, _dateTaken)));

		b.append(SavedObjectReader.getResourceString(R.string.mail_date_added_photo,
					sqliteDateToString(c, _dateAdded)));

		b.append(SavedObjectReader.getResourceString(R.string.mail_date_modified_photo,
					sqliteDateToString(c, _dateModified)));

		b.append(SavedObjectReader.getResourceString(R.string.message_fin_mail));

		m.setBody(b.toString());
		m.addAttachment(_absolutePath);
	}

	/* (non-Javadoc)
	 * @see com.lpi.sauvegarde.SavedObjects.SavedObject#confirmeEnvoi(android.content.Context)
	 */
	@Override
	public void confirmeEnvoi(Context c)
	{
		SharedPreferences settings = c.getSharedPreferences(Sauvegarde.PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(Sauvegarde.PREF_DERNIERE_SAUVEGARDE_PHOTOS, _dateModified);
		editor.commit();
	}
}
