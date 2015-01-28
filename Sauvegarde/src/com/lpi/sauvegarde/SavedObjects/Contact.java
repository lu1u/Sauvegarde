/**
 * 
 */
package com.lpi.sauvegarde.SavedObjects;

import android.content.Context;

import com.lpi.sauvegarde.Mail.Mail;

/**
 * @author lucien
 *
 */
public class Contact extends SavedObject
{
	public String _nom;
	public long _lastContacted;
	public long _timesContacted ;

	// public ArrayList<String> Numeros = new ArrayList<String>();
	// public ArrayList<String> EMails = new ArrayList<String>();
	public String[] _numeros;
	public String[] _eMails;

	@Override
	public void construitMail(Context c, Mail m)
	{
	}

	/**
	 * Ajoute le texte correspondant a ce contact
	 * @param s
	 */
	@SuppressWarnings("nls")
	public void appendToMail(Context context, StringBuilder s)
	{
		s.append("Contact: ").append(_nom).append("\n");
		
		if ( _timesContacted > 0 )
			s.append( "Contacté ").append(_timesContacted).append(" fois\n") ;
		
		try
		{
			if ( _timesContacted != 0)
				s.append("Contacté la dernière fois: ").append(sqliteDateToString(context, _lastContacted)).append("\n");
		} catch (NumberFormatException e)
		{
		}

		if (_numeros != null)
		{
			// Numeros de telephone
			for (String tel : _numeros)
				s.append("  Téléphone: ").append(tel).append("\n");
		}

		if (_eMails != null)
		{
			// Adresses mail
			for (String mail : _eMails)
				s.append("  E-mail: ").append(mail).append("\n");
		}

		s.append("\n");
	}

	/***
	 * Retrouve les informations de tous les contacts Inspire de
	 * http://tausiq.wordpress.com/2012/08/23/android-get-contact-details-id-name-phone-photo/
	 * 
	 * @param context
	 * @return
	 */
	/*
	 * @SuppressWarnings("nls") static public ArrayList<Contact> getContacts(Context context) {
	 * ArrayList<Contact> listecontacts = new ArrayList<Contact>(); Cursor cursor =
	 * context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null,
	 * null); while (cursor.moveToNext()) { Contact contact = new Contact(); String contact_id =
	 * cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID)); contact.Nom =
	 * cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)); int
	 * hasPhoneNumber = Integer.parseInt(cursor.getString(cursor
	 * .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))); if (hasPhoneNumber > 0) { //
	 * Numeros de telephone Cursor phoneCursor = context.getContentResolver().query(
	 * ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
	 * ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { contact_id },
	 * null); while (phoneCursor.moveToNext()) contact.Numeros.add(phoneCursor.getString(phoneCursor
	 * .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))); phoneCursor.close(); } //
	 * Adresses mail Cursor emailCursor =
	 * context.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
	 * ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[] { contact_id },
	 * null); while (emailCursor.moveToNext()) contact.EMails.add(emailCursor.getString(emailCursor
	 * .getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))); emailCursor.close(); // On
	 * n'ajoute que les contacts qui ont des numeros de telephone ou un email if
	 * (contact.Numeros.size() > 0 || (contact.EMails.size() > 0)) listecontacts.add(contact); }
	 * cursor.close(); return listecontacts; }
	 */
}
