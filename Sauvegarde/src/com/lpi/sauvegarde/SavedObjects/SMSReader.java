/**
 * 
 */
package com.lpi.sauvegarde.SavedObjects;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.lpi.sauvegarde.R;
import com.lpi.sauvegarde.Report;
import com.lpi.sauvegarde.Sauvegarde.Sauvegarde;

/**
 * @author lucien
 *
 */
public class SMSReader extends SavedObjectReader
{
	static private String COL_ADRESS = "address" ;//$NON-NLS-1$
	static private String COL_TYPE = "type" ;//$NON-NLS-1$
	static private String COL_BODY = "body" ;//$NON-NLS-1$
	static private String COL_DATE = "date" ;//$NON-NLS-1$
	static private String [] COLONNES = { COL_ADRESS, COL_TYPE, COL_BODY, COL_DATE } ;
	
	private int _colonneDestinataire, _colonneType, _colonneBody, _colonneDate ;
	
	public SMSReader( Context c, long depuis)
	{
		super( c, depuis, c.getContentResolver().query(Uri.parse( "content://sms/"),  //$NON-NLS-1$
				COLONNES, 
				android.provider.CallLog.Calls.DATE + " >= " + depuis,  //$NON-NLS-1$
				null, android.provider.CallLog.Calls.DATE + " ASC")); //$NON-NLS-1$
		_colonneDestinataire= _cursor.getColumnIndexOrThrow(COL_ADRESS) ;
		_colonneType = _cursor.getColumnIndexOrThrow(COL_TYPE); 
		_colonneBody = _cursor.getColumnIndexOrThrow(COL_BODY);
		_colonneDate = _cursor.getColumnIndexOrThrow(COL_DATE); 
	}
	
	
	public SavedObject currentObject()
	{
		if (_cursor == null)
			return null ;
		
		// Construire un SMS a partir des infos de la base
		SMS message = new SMS() ;
		message._adresse =  _cursor.getString(_colonneDestinataire); 
		message._type = SMS.parseSMSType(_cursor.getString(_colonneType));
		message._message = _cursor.getString(_colonneBody);  
		message._date = _cursor.getLong(_colonneDate); 
		return message ;
	}

	@Override
	public void remplitReport(Report r, int NbSauvegardes)
	{
		r.SauvegardeMMS = getResourceString(R.string.sauvegardes_sms, Integer.valueOf(NbSauvegardes) );
	}

	@Override
	public void remplitReportPasDeNouveau(Report report)
	{
		report.SauvegardeMMS = getResourceString(R.string.pas_de_nouveau_sms ) ;
	}
	public String getNotificationMessage(int no, int total)
	{
		return getResourceString(R.string.sauvegarde_en_cours_sms, Integer.valueOf(no), Integer.valueOf(total) );
	}

	public String getNotificationMessageFin(int total)
	{
		return getResourceString(R.string.sauvegardes_sms, Integer.valueOf(total) );
	}

	@Override
	public void setDerniereSauvegarde(long now)
	{
		SharedPreferences settings = _context.getSharedPreferences(Sauvegarde.PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(Sauvegarde.PREF_DERNIERE_SAUVEGARDE_SMS, now);
		editor.commit();
	}
	
	

}
