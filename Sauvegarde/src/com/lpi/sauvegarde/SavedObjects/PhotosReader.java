/**
 * 
 */
package com.lpi.sauvegarde.SavedObjects;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;

import com.lpi.sauvegarde.R;
import com.lpi.sauvegarde.Report;
import com.lpi.sauvegarde.Sauvegarde.Sauvegarde;

/**
 * @author lucien
 *
 */
public class PhotosReader extends SavedObjectReader
{
	private int _colonneAbsolutePath, _colonneDisplayName, _colonneDescription, _colonneDateModified,
	_colonneDateTaken, _colonneDateAdded;
	
	private static String[] COLONNES =
		{
		MediaStore.Video.Media.DATA,MediaStore.Video.Media.DISPLAY_NAME,MediaStore.Video.Media.DESCRIPTION,
		MediaStore.Video.Media.DATE_MODIFIED,MediaStore.Video.Media.DATE_TAKEN,MediaStore.Video.Media.DATE_ADDED
		};

	public boolean Depuis(long derniereSauvegarde)
	{
		return true;
	}

	public PhotosReader( Context c, long depuis)
	{
		super( c, depuis, c.getContentResolver().query(Images.Media.EXTERNAL_CONTENT_URI, 
	            COLONNES, 
	            MediaStore.Video.Media.DATE_MODIFIED + " >= " + depuis,  //$NON-NLS-1$
	            null, 
	            MediaStore.Video.Media.DATE_MODIFIED + " ASC")); //$NON-NLS-1$

		_colonneAbsolutePath = _cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
		_colonneDisplayName = _cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
		_colonneDescription = _cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DESCRIPTION);
		_colonneDateModified = _cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED);
		_colonneDateTaken = _cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN);
		_colonneDateAdded = _cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED);
	}

	public SavedObject currentObject()
	{
		if (_cursor == null)
			return null;

		// Construire un SMS a partir des infos de la base
		Photo photo = new Photo();
		
		photo._absolutePath =  _cursor.getString(_colonneAbsolutePath); 
		photo._displayName = _cursor.getString(_colonneDisplayName);
		photo._description = _cursor.getString(_colonneDescription); 
		photo._dateModified = _cursor.getLong(_colonneDateModified); 
		photo._dateTaken = _cursor.getLong(_colonneDateTaken); 
		photo._dateAdded = _cursor.getLong(_colonneDateAdded); 
		
		return photo;
	}
	
	@Override
	public void remplitReport(Report r, int NbSauvegardes)
	{
		r.SauvegardePhotos = getResourceString(R.string.sauvegardes_photos, Integer.valueOf(NbSauvegardes) );
	}

	@Override
	public void remplitReportPasDeNouveau(Report report)
	{
		report.SauvegardePhotos = getResourceString(R.string.pas_de_nouvelle_photo ) ;
	}
	public String getNotificationMessage(int no, int total)
	{
		return getResourceString(R.string.sauvegarde_en_cours_photo, Integer.valueOf(no), Integer.valueOf(total) );
	}

	public String getNotificationMessageFin(int total)
	{
		return getResourceString(R.string.sauvegardes_photos, Integer.valueOf(total) );
	}
	
	@Override
	public void setDerniereSauvegarde(long now)
	{
		SharedPreferences settings = _context.getSharedPreferences(Sauvegarde.PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(Sauvegarde.PREF_DERNIERE_SAUVEGARDE_PHOTOS, now);
		editor.commit();
	}

}
