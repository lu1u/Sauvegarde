/**
 * 
 */
package com.lpi.sauvegarde.SavedObjects;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.lpi.sauvegarde.R;
import com.lpi.sauvegarde.Report;
import com.lpi.sauvegarde.Sauvegarde.Sauvegarde;

/**
 * @author lucien
 *
 */
public class ContactsReader extends SavedObjectReader
{
	private static final String[] _colonneS_CONTACT =
	{ ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME,
			ContactsContract.Contacts.HAS_PHONE_NUMBER, ContactsContract.Contacts.TIMES_CONTACTED,
			ContactsContract.Contacts.LAST_TIME_CONTACTED };

	private int _colonneID, _colonneDisplayName, _colonneHasPhoneNumber, _colonneTimesUpdated, _colonneLastContacted;

	private static final String[] _colonneS_PHONENUMBER =
	{ ContactsContract.CommonDataKinds.Phone.NUMBER };
	private static final String[] _colonneS_EMAIL =
	{ ContactsContract.CommonDataKinds.Email.DATA };

	public ContactsReader(Context context)
	{
		super(context, 0 /* depuis */, context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
				_colonneS_CONTACT, null, null, null));

		_colonneID = _cursor.getColumnIndex(ContactsContract.Contacts._ID);
		_colonneDisplayName = _cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
		_colonneHasPhoneNumber = _cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
		_colonneTimesUpdated = _cursor.getColumnIndex(ContactsContract.Contacts.TIMES_CONTACTED);
		_colonneLastContacted = _cursor.getColumnIndex(ContactsContract.Contacts.LAST_TIME_CONTACTED);
	}

	/***
	 * Retourne true s'il y a des nouveaux objets depuis la derniere sauvegarde
	 * 
	 * @return
	 */
	@Override
	public boolean nouveauxDepuisDerniereSauvegarde()
	{
		String allContacts = getAllContactString();
		if (allContacts == null)
			return true;

		SharedPreferences settings = _context.getSharedPreferences(Sauvegarde.PREFERENCES, Context.MODE_PRIVATE);
		if (allContacts.equals(settings.getString(Sauvegarde.PREF_CONTACTS_ALL_VERSIONS, ""))) //$NON-NLS-1$
			// Pas de difference depuis la derniere sauvegarde
			return false;

		SharedPreferences.Editor editor = settings.edit();
		editor.putString(Sauvegarde.PREF_CONTACTS_ALL_VERSIONS, allContacts);
		editor.commit();

		return true;
	}

	/***
	 * Construit une chaine representative de la version de chaque contact Comme on n'a pas
	 * d'enregistrement de la date de modification des contacts, c'est le seul moyen de verifier
	 * s'il y a eu des modifications depuis la derniere sauvegarde Inspire de
	 * http://stackoverflow.com
	 * /questions/10702547/how-to-get-the-last-modification-date-for-contacts-list-add-delete-modify
	 * 
	 * @return
	 */
	private String getAllContactString()
	{
		Cursor allContacts = null;
		try
		{
			allContacts = _context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, null,
					null, null);

			StringBuilder sbCurrentVersion = new StringBuilder();
			int col = allContacts.getColumnIndex(ContactsContract.RawContacts.VERSION);
			allContacts.moveToFirst();

			while (allContacts.moveToNext())
				sbCurrentVersion.append(allContacts.getString(col));

			return sbCurrentVersion.toString();
		} catch (Exception e)
		{

		} finally
		{
			if (allContacts != null)
				allContacts.close();
		}

		return null;
	}

	/***
	 * Construit un objet Contact a partir des infos de la base
	 */
	public Contact currentObject()
	{
		if (_cursor == null)
			return null;

		// Construire un SMS a partir des infos de la base
		Contact contact = new Contact();

		String contact_id = _cursor.getString(_colonneID);
		contact._nom = _cursor.getString(_colonneDisplayName);
		contact._lastContacted = _cursor.getLong(_colonneLastContacted);
		contact._timesContacted = _cursor.getLong(_colonneTimesUpdated);

		{
			// Numeros de telephone
			int hasPhoneNumber = Integer.parseInt(_cursor.getString(_colonneHasPhoneNumber));
			if (hasPhoneNumber > 0)
			{
				Cursor phoneCursor = null;
				try
				{
					phoneCursor = _context.getContentResolver().query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI, _colonneS_PHONENUMBER,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] //$NON-NLS-1$
							{ contact_id }, null);

					contact._numeros = new String[phoneCursor.getCount()];
					final int _colonneIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
					int i = 0;
					while (phoneCursor.moveToNext())
					{
						contact._numeros[i] = phoneCursor.getString(_colonneIndex);
						i++;
					}
				} finally
				{
					phoneCursor.close();
				}
			}
		}
		{
			// Adresses mail
			Cursor emailCursor = null;
			try
			{
				emailCursor = _context.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
						_colonneS_EMAIL, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[] //$NON-NLS-1$
						{ contact_id }, null);

				if (emailCursor.getCount() > 0)
				{
					final int _colonneIndex = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
					contact._eMails = new String[emailCursor.getCount()];
					int i = 0;
					while (emailCursor.moveToNext())
					{
						contact._eMails[i] = emailCursor.getString(_colonneIndex);
						i++;
					}
				}
			} finally
			{
				emailCursor.close();
			}

		}
		return contact;
	}

	@Override
	public void remplitReport(Report r, int NbSauvegardes)
	{
		r.SauvegardeContacts = getResourceString(R.string.sauvegardes_contact, Integer.valueOf(NbSauvegardes));
	}

	@Override
	public void remplitReportPasDeNouveau(Report report)
	{
		report.SauvegardeContacts = getResourceString(R.string.pas_de_nouveau_contact);
	}

	public String getNotificationMessage(int no, int total)
	{
		return getResourceString(R.string.sauvegarde_en_cours_contact, Integer.valueOf(no), Integer.valueOf(total));
	}

	public String getNotificationMessageFin(int total)
	{
		return getResourceString(R.string.sauvegardes_contact, Integer.valueOf(total));
	}

	@Override
	public void setDerniereSauvegarde(long now)
	{
		// Rien a faire (voir getAllContactString)
	}

}
