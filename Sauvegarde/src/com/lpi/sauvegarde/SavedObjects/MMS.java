/**
 * 
 */
package com.lpi.sauvegarde.SavedObjects;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;

import com.lpi.sauvegarde.R;
import com.lpi.sauvegarde.Mail.Mail;
import com.lpi.sauvegarde.Sauvegarde.Sauvegarde;

/**
 * @author lucien
 *
 */
public class MMS extends SavedObject
{
	public static final String MULTIPART_TEXT = "text"; //$NON-NLS-1$
	public static final String MULTIPART_PLAIN_TEXT = "text/plain"; //$NON-NLS-1$
	public static final String MULTIPART_IMAGE = "image/"; //$NON-NLS-1$
	public static final String MULTIPART_SMIL = "application/smil"; //$NON-NLS-1$
	public String _adresse;
	public long _date;

	private class MMSPart
	{
		public MMSPart(String t, String d)
		{
			_type = t;
			_data = d;
		}

		public String _type;
		public String _data;
	}

	private Vector<MMSPart> _donnees = new Vector<MMSPart>();
	private List<String> _fichiersTemporaires = new LinkedList<String>();

	@Override
	public String identification(Context c)
	{
		return _adresse + ' ' +  sqliteDateToString(c, _date) ;
	}
	private void setSubject(Context context, Mail m)
	{
		m.setSubject(SavedObjectReader.getResourceString(R.string.sujet_mms_inconnu,getContactFromNumber( context, _adresse)));
	}

	private void setBody(Context context, Mail m) throws Exception
	{
		StringBuffer b = new StringBuffer();
		b.append( Sauvegarde.formatResourceString(context, R.string.mms_addresse, getContactFromNumber(context, _adresse)));
		b.append(Sauvegarde.formatResourceString(context, R.string.mms_date, sqliteDateToString(context, _date)));

		Iterator<MMSPart> itr = _donnees.iterator();
		while (itr.hasNext())
		{
			MMSPart part = itr.next();
			if (MULTIPART_TEXT.equals(part._type))
			{
				// Texte brut: _data contient le texte
				b.append((String) part._data).append('\n');
			} else if (MULTIPART_PLAIN_TEXT.equals(part._type))
			{
				// Plain texte: _data contient l'id
				b.append("Texte:\n").append(getMmsText(context, (String) part._data)).append('\n'); //$NON-NLS-1$
			} else
			{
				File attachement = getMmsAttachement(context, part._data, part._type);
				String name = attachement.getName();
				m.addAttachment(attachement.getAbsolutePath(), name, part._type);
				_fichiersTemporaires.add(attachement.getAbsolutePath());
			}
		}
		m.setBody(b.toString());
	}

	@Override
	public void construitMail(Context context, Mail m) throws Exception
	{
		setSubject(context, m);
		setBody(context, m);
	}

	/***
	 * Copie une des parties du MMS dans un fichier temporaire et en retourne le nom
	 * pour le joindre au mail
	 * @param context
	 * @param id
	 * @param mimeType
	 * @return
	 * @throws Exception
	 */
	private File getMmsAttachement(Context context, String id, String mimeType) throws Exception
	{
		Uri partURI = Uri.parse("content://mms/part/" + id); //$NON-NLS-1$
		File fichier = getFichier(context, mimeType, id);
		OutputStream outS = null;
		InputStream inS = null;

		try
		{
			inS = context.getContentResolver().openInputStream(partURI);
			outS = new FileOutputStream(fichier);
			byte[] buffer = new byte[1024];
			int len = inS.read(buffer);
			while (len >= 0)
			{
				outS.write(buffer, 0, len);
				len = inS.read(buffer);
			}
		} catch (IOException e)
		{
			throw e;
		} finally
		{
			if (inS != null)
				inS.close();
			if (outS != null)
				outS.close();
		}

		return fichier;
	}

	
	/***
	 * Ajoute toutes les informations qui nous permettrons de retrouver les donnees de cette partie
	 * d'un MMS
	 * 
	 * @param cursor
	 */
	public void addParts(Cursor cursor)
	{
		String partId = cursor.getString(cursor.getColumnIndexOrThrow("_id")); //$NON-NLS-1$
		String type = cursor.getString(cursor.getColumnIndexOrThrow("ct")); //$NON-NLS-1$
		if (MULTIPART_PLAIN_TEXT.equals(type))
		{
			String data = cursor.getString(cursor.getColumnIndexOrThrow("_data")); //$NON-NLS-1$
			if (data != null)
				_donnees.add(new MMSPart(type, partId));
			else
				_donnees.add(new MMSPart(MULTIPART_TEXT, cursor.getString(cursor.getColumnIndexOrThrow("text")))); //$NON-NLS-1$
		} else if (type.startsWith(MULTIPART_IMAGE))
			_donnees.add(new MMSPart(type, partId));
		else if (!type.equals(MULTIPART_SMIL))
			// On ignore celles la
			_donnees.add(new MMSPart(type, partId));
	}

	/**
	 * Lit une image, la copie dans un fichier temporaire, retourne le chemin de ce fichier
	 * 
	 * @param context
	 * @param id
	 * @return
	 */
	private String getMmsText(Context context, String id)
	{
		Uri partURI = Uri.parse("content://mms/part/" + id); //$NON-NLS-1$
		InputStream is = null;
		StringBuilder sb = new StringBuilder();
		try
		{
			is = context.getContentResolver().openInputStream(partURI);
			if (is != null)
			{
				InputStreamReader isr = new InputStreamReader(is, "UTF-8"); //$NON-NLS-1$
				BufferedReader reader = new BufferedReader(isr);
				String temp = reader.readLine();
				while (temp != null)
				{
					sb.append(temp);
					temp = reader.readLine();
				}
			}
		} catch (IOException e)
		{
		} finally
		{
			if (is != null)
			{
				try
				{
					is.close();
				} catch (IOException e)
				{
				}
			}
		}
		return sb.toString();
	}

	private File getFichier(Context c, String Type, String Id)
	{
		String ext = Type.substring(Type.indexOf('/') + 1);
		return new File(c.getCacheDir().getAbsolutePath() + '/' + Sauvegarde.formatResourceString(c, R.string.mms_filename, Id, ext));

	}

	/***
	 * Traitements a faire une fois que l'objet a ete envoye par mail
	 * 
	 * @param c
	 */
	@Override
	public void confirmeEnvoi(Context c)
	{
		// Supprimer les fichiers temporaires
		Iterator<String> itr = _fichiersTemporaires.iterator();
		while (itr.hasNext())
		{
			File fichier = new File(itr.next());
			fichier.delete();
		}
		
		// Enregistrer la date de cet objet, pour avoir une date de depart lors de la prochaine sauvegarde
		SharedPreferences settings = c.getSharedPreferences(Sauvegarde.PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(Sauvegarde.PREF_DERNIERE_SAUVEGARDE_MMS, _date);
		editor.commit();
	
	}
}
