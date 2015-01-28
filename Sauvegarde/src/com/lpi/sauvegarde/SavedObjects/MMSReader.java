/**
 * 
 */
package com.lpi.sauvegarde.SavedObjects;

import java.text.MessageFormat;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;

import com.lpi.sauvegarde.R;
import com.lpi.sauvegarde.Report;
import com.lpi.sauvegarde.Sauvegarde.Sauvegarde;

/**
 * @author lucien
 *
 */
public class MMSReader extends SavedObjectReader
{
	static private final String COL_ID = "_id";//$NON-NLS-1$
	static private final String COL_DATE = "date";//$NON-NLS-1$
	static private String[] COLONNES =
	{ COL_ID, COL_DATE };

	private int _colonneId;

	public MMSReader(Context c, long depuis)
	{
		super(c, depuis, c.getContentResolver().query(Uri.parse("content://mms"), //$NON-NLS-1$
				COLONNES, COL_DATE + " >= " + depuis, //$NON-NLS-1$
				null, COL_DATE + " ASC")); //$NON-NLS-1$

		_colonneId = _cursor.getColumnIndexOrThrow(COL_ID);
	}

	public SavedObject currentObject()
	{
		if (_cursor == null)
			return null;

		// Construire un SMS a partir des infos de la base
		String id = _cursor.getString(_colonneId);
		MMS message = new MMS();
		message._date = _cursor.getLong(_cursor.getColumnIndex(COL_DATE)) ;
		message._adresse = getAddressNumber(id) ;
		
		String selectionPart = "mid=" + id ; //$NON-NLS-1$
		Uri uri = Uri.parse("content://mms/part"); //$NON-NLS-1$
		Cursor cursor = null;

		try
		{
			cursor = _context.getContentResolver().query(uri, null, selectionPart, null, null);
			while (cursor.moveToNext())
				message.addParts(cursor);				
			
		} finally
		{
			if (cursor != null)
				cursor.close();
		}
		return message;
	}

	
	@SuppressWarnings("nls")
	private String getAddressNumber(String id) {
	    String selectionAdd = new String("msg_id=" + id);
	    String uriStr = MessageFormat.format("content://mms/{0}/addr", id);
	    Uri uriAddress = Uri.parse(uriStr);
	    Cursor cAdd = _context.getContentResolver().query(uriAddress, null,
	        selectionAdd, null, null);
	    String name = null;
	    if (cAdd.moveToFirst()) {
	        do {
	            String number = cAdd.getString(cAdd.getColumnIndex("address"));
	            if (number != null) {
	                try {
	                    Long.parseLong(number.replace("-", ""));
	                    name = number;
	                } catch (NumberFormatException nfe) {
	                    if (name == null) {
	                        name = number;
	                    }
	                }
	            }
	        } while (cAdd.moveToNext());
	    }
	    if (cAdd != null) {
	        cAdd.close();
	    }
	    return name;
	}
	@Override
	public void remplitReport(Report r, int NbSauvegardes)
	{
		r.SauvegardeMMS = getResourceString(R.string.sauvegardes_sms, Integer.valueOf(NbSauvegardes));
	}

	@Override
	public void remplitReportPasDeNouveau(Report report)
	{
		report.SauvegardeMMS = getResourceString(R.string.pas_de_nouveau_sms);
	}

	public String getNotificationMessage(int no, int total)
	{
		return getResourceString(R.string.sauvegarde_en_cours_sms, Integer.valueOf(no), Integer.valueOf(total));
	}

	public String getNotificationMessageFin(int total)
	{
		return getResourceString(R.string.sauvegardes_sms, Integer.valueOf(total));
	}

	@Override
	public void setDerniereSauvegarde(long now)
	{
		SharedPreferences settings = _context.getSharedPreferences(Sauvegarde.PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(Sauvegarde.PREF_DERNIERE_SAUVEGARDE_MMS, now);
		editor.commit();
	}

}