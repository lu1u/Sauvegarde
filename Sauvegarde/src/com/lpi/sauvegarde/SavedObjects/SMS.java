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
public class SMS extends SavedObject
{
	public static final int MESSAGE_TYPE_ALL = 0;
	public static final int MESSAGE_TYPE_INBOX = 1;
	public static final int MESSAGE_TYPE_SENT = 2;
	public static final int MESSAGE_TYPE_DRAFT = 3;
	public static final int MESSAGE_TYPE_OUTBOX = 4;
	public static final int MESSAGE_TYPE_FAILED = 5; // for failed outgoing messages
	public static final int MESSAGE_TYPE_QUEUED = 6; // for messages to send later

	
	String _adresse;
	String _message;
	long _date;

	public enum SMSType
	{
		ENVOYE, RECU, INCONNU
	}

	SMSType _type;

	@Override
	public String identification(Context c)
	{
		return _adresse + ' ' + sqliteDateToString(c, _date) ;
	}
	
	private void setSubject(Context context, Mail m) throws Exception
	{
		switch (_type)
		{
		case RECU:
			m.setSubject(SavedObjectReader.getResourceString(R.string.sujet_sms_recu,
					getContactFromNumber(context, _adresse)));
			break;

		case ENVOYE:
			m.setSubject(SavedObjectReader.getResourceString(R.string.sujet_sms_envoye,
					getContactFromNumber(context, _adresse)));
			break;
		default:
			m.setSubject(SavedObjectReader.getResourceString(R.string.sujet_sms_inconnu,
					getContactFromNumber(context, _adresse)));

			break;
		}
	}

	private void setBody(Context context, Mail m) throws Exception
	{
		m.setBody(SavedObjectReader.getResourceString(R.string.sms_body,
				sqliteDateToString(context, _date), _message,
				SavedObjectReader.getResourceString(R.string.message_fin_mail)));
	}

	public void construitMail(Context context, Mail m) throws Exception
	{
		setSubject(context, m);
		setBody(context, m);
	}

	public static SMSType parseSMSType(String type)
	{
		try
		{
			switch (Integer.parseInt(type))
			{
			case MESSAGE_TYPE_ALL:
				return SMSType.INCONNU;
			case MESSAGE_TYPE_INBOX:
				return SMSType.RECU;
			case MESSAGE_TYPE_SENT:
				return SMSType.ENVOYE;
			case MESSAGE_TYPE_DRAFT:
				return SMSType.ENVOYE;
			case MESSAGE_TYPE_OUTBOX:
				return SMSType.ENVOYE;
			case MESSAGE_TYPE_FAILED:
				return SMSType.INCONNU;
			case MESSAGE_TYPE_QUEUED:
				return SMSType.ENVOYE;

			default:
				return SMSType.INCONNU;
			}
		} catch (Exception e)
		{
			return SMSType.INCONNU;
		}
	}

	/* (non-Javadoc)
	 * @see com.lpi.sauvegarde.SavedObjects.SavedObject#confirmeEnvoi(android.content.Context)
	 */
	@Override
	public void confirmeEnvoi(Context c)
	{
		SharedPreferences settings = c.getSharedPreferences(Sauvegarde.PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(Sauvegarde.PREF_DERNIERE_SAUVEGARDE_SMS, _date);
		editor.commit();
	}

}
