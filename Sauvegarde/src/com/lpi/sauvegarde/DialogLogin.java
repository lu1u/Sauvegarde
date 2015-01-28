/**
 * 
 */
package com.lpi.sauvegarde;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.lpi.sauvegarde.Sauvegarde.Sauvegarde;

/**
 * @author lucien
 *
 */
public class DialogLogin
{
	Activity _activity;
	Intent _intent;
	Dialog _dialog ;
	public DialogLogin(Activity a, Intent intent)
	{
		_activity = a;
		_intent = intent;
	}

	public boolean show()
	{
		_dialog = new Dialog(_activity);
		_dialog.setContentView(R.layout.login);
		_dialog.setTitle(_activity.getResources().getString(R.string.compte_mail_envoi));

		// Lire les preferences
		SharedPreferences settings = _activity.getSharedPreferences(Sauvegarde.PREFERENCES, Context.MODE_PRIVATE);

		// Compte mail pour envoyer
		((TextView) _dialog.findViewById(R.id.editTextFromAdress)).setText(settings.getString(Sauvegarde.PREF_FROMADDRESS,
				MainActivity.getDefaultAccount(_activity)));
		// Mot de passe
		((TextView) _dialog.findViewById(R.id.editTextPassword)).setText(settings.getString(Sauvegarde.PREF_PASSWORD, "")); //$NON-NLS-1$

		_dialog.setCancelable(true);

		Button btn = (Button) _dialog.findViewById(R.id.buttonOk); 
		// if button is clicked, close the custom dialog 
		btn.setOnClickListener(new OnClickListener() 
		{
		  @Override public void onClick(View v) 
		  	{ OnOK(); }
		});
		 
		btn = (Button) _dialog.findViewById(R.id.buttonCancel); 
		// if button is clicked, close the custom dialog 
		btn.setOnClickListener(new OnClickListener() 
		{
		  @Override public void onClick(View v) 
		  	{_dialog.dismiss(); }
		});
		_dialog.show();

		return true;
	}
	
	public void OnOK()
	{
		// Enregistrer les infos saisies
		SharedPreferences settings = _activity.getSharedPreferences(Sauvegarde.PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(Sauvegarde.PREF_FROMADDRESS, ((TextView)_dialog.findViewById(R.id.editTextFromAdress)).getText().toString());
		editor.putString(Sauvegarde.PREF_PASSWORD, ((TextView)_dialog.findViewById(R.id.editTextPassword)).getText().toString());
		editor.commit() ;
		
		if ( MainActivity.IsLoginOK(_activity))
		{
		_dialog.dismiss();
		_activity.sendBroadcast(_intent);
		}
		else
		{
			CustomToast.Show(_activity, _activity.getResources().getString(R.string.infos_compte_incorrectes), Toast.LENGTH_SHORT) ;
		}
	}
}
