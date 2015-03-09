/**
 * 
 */
package com.lpi.sauvegarde.SavedObjects;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.CallLog;

import com.lpi.sauvegarde.R;
import com.lpi.sauvegarde.Report;
import com.lpi.sauvegarde.Sauvegarde.Sauvegarde;

/**
 * @author lucien
 *
 */
public class AppelTelephoniqueReader extends SavedObjectReader
{
	static private String COLONNES[] = new String[]
	{ CallLog.Calls._ID, CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.DURATION, CallLog.Calls.TYPE };

	private int _colID, _colNumber, _colDate, _colDuration, _colType;

	public AppelTelephoniqueReader(Context c, long depuis)
	{
		super(c, depuis, c.getContentResolver().query(android.provider.CallLog.Calls.CONTENT_URI, COLONNES,
				CallLog.Calls.DATE + " >= " + depuis, //$NON-NLS-1$
				null, CallLog.Calls.DATE + " ASC")); //$NON-NLS-1$

		_colID = _cursor.getColumnIndexOrThrow(CallLog.Calls._ID);
		_colNumber = _cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER);
		_colDate = _cursor.getColumnIndexOrThrow(CallLog.Calls.DATE);
		_colDuration = _cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION);
		_colType = _cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE);
	}

	/*
	 * (non-Javadoc)
	 * @see com.lpi.sauvegarde.SavedObjects.SavedObjectReader#currentObject()
	 */
	@Override
	public SavedObject currentObject()
	{
		if (_cursor == null)
			return null;

		// Construire un SMS a partir des infos de la base
		String id = _cursor.getString(_colID);
		AppelTelephonique appelTelephonique = new AppelTelephonique();
		appelTelephonique._ID = id ;
		appelTelephonique._date = _cursor.getLong(_colDate) ;
		appelTelephonique._duration = _cursor.getLong(_colDuration) ;
		appelTelephonique._number = _cursor.getString(_colNumber) ;
		appelTelephonique._type = _cursor.getInt(_colType) ;
 		return appelTelephonique ;
	}

	@Override
	public void remplitReport(Report r, int NbSauvegardes)
	{
		r.SauvegardeVideos = getResourceString(R.string.sauvegardes_appels, Integer.valueOf(NbSauvegardes));
	}

	@Override
	public void remplitReportPasDeNouveau(Report report)
	{
		report.SauvegardeVideos = getResourceString(R.string.pas_de_nouveau_appel);
	}

	public String getNotificationMessage(int no, int total)
	{
		return getResourceString(R.string.sauvegarde_en_cours_appels, Integer.valueOf(no), Integer.valueOf(total));
	}

	public String getNotificationMessageFin(int total)
	{
		return getResourceString(R.string.sauvegardes_appels, Integer.valueOf(total));
	}

	@Override
	public void setDerniereSauvegarde(long now)
	{
		SharedPreferences settings = _context.getSharedPreferences(Sauvegarde.PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(Sauvegarde.PREF_DERNIERE_JOURNAL_TELEPHONE, now);
		editor.commit();
	}



}
