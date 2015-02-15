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

	public String[] _numeros;
	public String[] _eMails;

	@Override
	public void construitMail(Context c, Mail m)
	{
	}

	@Override
	public String identification(Context c)
	{
		return _nom ;
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
}
