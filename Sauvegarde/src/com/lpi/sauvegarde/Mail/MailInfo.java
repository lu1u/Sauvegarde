/**
 * 
 */
package com.lpi.sauvegarde.Mail;

import android.content.SharedPreferences;

import com.lpi.sauvegarde.Sauvegarde.Sauvegarde;

/**
 * @author lucien
 *
 */
public class MailInfo
{
	public String destAddress ;
	public String fromAdress ;
	public String password ;
	public String SMTP ;
	public String Port ;
	
	@SuppressWarnings("nls")
	public void read( SharedPreferences settings )
	{
		destAddress = settings.getString(Sauvegarde.PREF_DESTADDRESS, "");
		fromAdress = settings.getString(Sauvegarde.PREF_FROMADDRESS, "");
		password = settings.getString(Sauvegarde.PREF_PASSWORD, "");
		SMTP = settings.getString(Sauvegarde.PREF_SMTP, Mail.DEFAULT_SMTP);
		Port = Integer.toString(settings.getInt(Sauvegarde.PREF_PORT, Integer.parseInt(Mail.DEFAULT_PORT)));
	}
}
