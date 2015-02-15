/**
 * 
 */
package com.lpi.sauvegarde.SavedObjects;

import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;

import com.lpi.sauvegarde.R;
import com.lpi.sauvegarde.Mail.Mail;

/**
 * @author lucien
 *
 */
public abstract class SavedObject
{
	private static final String[] COLONNES_NUMERO = new String[]
			{ PhoneLookup.DISPLAY_NAME };

	abstract public void construitMail( Context c, Mail  m ) throws Exception ;
	abstract public String identification(Context c) ;
	
	/***
	 * Traitements a faire une fois que l'objet a ete envoye par mail
	 * @param c
	 */
	public void confirmeEnvoi(Context c)
	{
	}
	
	public static String sqliteDateToString(Context context, long l)
	{
		try
		{
			return android.text.format.DateFormat.getDateFormat(context).format(new Date(l));
		} catch (Exception e)
		{
			return l + " (format de date non reconnue)" ; //$NON-NLS-1$
		}
	}
	
	/***
	 * Converti en texte une valeur representant une duree en secondes
	 * @param context
	 * @param l
	 * @return
	 */
	public static String sqliteDurationToString(Context context, long l)
	{
		try
		{
			int secondes = (int)l % 60 ;
			l /= 60 ;
			int minutes = (int)l % 60 ;
			l /= 60 ;
			int heures = (int)l ;
			
			return SavedObjectReader.getResourceString(R.string.duration, heures, minutes, secondes ) ;
		} catch (Exception e)
		{
			return l + " (format de date non reconnue)" ; //$NON-NLS-1$
		}
	}
		
	/**
	 * Essaie de retrouver le nom d'un contact a partir de son numero de telephone
	 *
	 * @param numero
	 *            : numero appelant
	 * @return le nom du contact ou "numero inconnu "+numero
	 */
	static public String getContactFromNumber(Context context, String numero)
	{
		String res;
		Cursor c = null;
		try
		{
			c = context.getContentResolver().query(
					Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(numero)), COLONNES_NUMERO, null,
					null, null);
			c.moveToFirst();
			res = c.getString(c.getColumnIndexOrThrow(PhoneLookup.DISPLAY_NAME));
			c.close();
		} catch (Exception e)
		{
			if (numero.startsWith("+33")) //$NON-NLS-1$
			{
				numero = "0" + numero.substring(3); //$NON-NLS-1$
				if (c != null)
					c.close();
				return getContactFromNumber(context, numero);
			} else
				res = null;
		}

		if (c != null)
			c.close();

		if (res != null)
			return res;

		String strFormat = "Inconnu %s"; //$NON-NLS-1$
		return String.format(strFormat, numero);
	}



}
