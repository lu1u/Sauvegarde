/**
 * 
 */
package com.lpi.sauvegarde;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.lpi.sauvegarde.Mail.Mail;
import com.lpi.sauvegarde.Sauvegarde.Sauvegarde;

/**
 * @author lucien
 *
 */
public class ParametresAvancesActivity extends Activity
{
	RadioGroup radioGroup;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.advanced);

		radioGroup = (RadioGroup) findViewById(R.id.myRadioGroup);

		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId)
			{
				// find which radio button is selected
				if (checkedId == R.id.radioBtnJamais)
				{
					Sauvegarde.setPreferenceRapport(ParametresAvancesActivity.this, Sauvegarde.RAPPORT_JAMAIS);
				} else if (checkedId == R.id.radioBtnErreur)
				{
					Sauvegarde.setPreferenceRapport(ParametresAvancesActivity.this, Sauvegarde.RAPPORT_ERREUR);
				} else
				{
					Sauvegarde.setPreferenceRapport(ParametresAvancesActivity.this, Sauvegarde.RAPPORT_TOUJOURS);
				}
			}

		});
		LitPreferences();
	}

	private void LitPreferences()
	{
		// Lire les preferences
		SharedPreferences settings = getSharedPreferences(Sauvegarde.PREFERENCES, Context.MODE_PRIVATE);

		// Compte mail pour envoyer
		((TextView) findViewById(R.id.editTextFromAdress)).setText(settings.getString(Sauvegarde.PREF_FROMADDRESS,
				MainActivity.getDefaultAccount(this)));
		// Mot de passe
		((TextView) findViewById(R.id.editTextPassword)).setText(settings.getString(Sauvegarde.PREF_PASSWORD, "")); //$NON-NLS-1$

		// Uniquement par WIFI
		((CheckBox) findViewById(R.id.checkBoxWIFI)).setChecked(settings.getBoolean(Sauvegarde.PREF_WIFI, true)); //

		// Envoyer rapport
		// ((CheckBox)
		// findViewById(R.id.checkBoxSendReport)).setChecked(settings.getBoolean(Sauvegarde.PREF_SEND_REPORT,
		// false)); //

		// Serveur SMTP
		((TextView) findViewById(R.id.editTextSMTP)).setText(settings
				.getString(Sauvegarde.PREF_SMTP, Mail.DEFAULT_SMTP));
		// Port
		((TextView) findViewById(R.id.editTextPort)).setText(Integer.toString(settings.getInt(Sauvegarde.PREF_PORT,
				Integer.parseInt(Mail.DEFAULT_PORT))));

		switch (Sauvegarde.LitPreferenceRapport(this))
		{
		case Sauvegarde.RAPPORT_ERREUR:
			radioGroup.check(R.id.radioBtnErreur);
			break;
		case Sauvegarde.RAPPORT_TOUJOURS:
			radioGroup.check(R.id.radioBtnTjrs);
			break;
		default:
			radioGroup.check(R.id.radioBtnJamais);
			break;
		}
	}

	/***
	 * Bouton "back": sauver les parametres
	 */
	@Override
	public void onBackPressed()
	{
		EnregistreParametres();

		super.onBackPressed();
	}

	private void EnregistreParametres()
	{
		SharedPreferences settings = getSharedPreferences(Sauvegarde.PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(Sauvegarde.PREF_FROMADDRESS, ((TextView) findViewById(R.id.editTextFromAdress)).getText()
				.toString());
		editor.putString(Sauvegarde.PREF_PASSWORD, ((TextView) findViewById(R.id.editTextPassword)).getText()
				.toString());
		editor.putBoolean(Sauvegarde.PREF_WIFI, ((CheckBox) findViewById(R.id.checkBoxWIFI)).isChecked());
		// editor.putBoolean(Sauvegarde.PREF_SEND_REPORT, ((CheckBox)
		// findViewById(R.id.checkBoxSendReport)).isChecked());
		editor.putString(Sauvegarde.PREF_SMTP, ((TextView) findViewById(R.id.editTextSMTP)).getText().toString());
		editor.putInt(Sauvegarde.PREF_PORT,
				Integer.parseInt(((TextView) findViewById(R.id.editTextPort)).getText().toString()));

		int rapport = 0;
		switch (radioGroup.getCheckedRadioButtonId())
		{
		case R.id.radioBtnTjrs:
			rapport = Sauvegarde.RAPPORT_TOUJOURS;
			break;
		case R.id.radioBtnErreur:
			rapport = Sauvegarde.RAPPORT_ERREUR;
			break;
		default:
			rapport = Sauvegarde.RAPPORT_JAMAIS;
			break;
		}

		editor.putInt(Sauvegarde.PREF_RAPPORT, rapport);
		editor.commit();
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy()
	{
		EnregistreParametres();
		super.onDestroy();
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause()
	{
		EnregistreParametres();
		super.onPause();
	}

	public void onClickParametresParDefaut(View v)
	{
		SharedPreferences settings = getSharedPreferences(Sauvegarde.PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.clear();
		editor.commit();
		CustomToast.Show(this, R.string.reinit_params, Toast.LENGTH_SHORT);
		LitPreferences();
	}

	/***
	 * Reinitialise les dates des dernieres sauvegardes pour que tous soit a nouveau sauvegarde la
	 * prochaine fois
	 * 
	 * @param v
	 */
	public void onClickReinitDates(View v)
	{
		SharedPreferences settings = getSharedPreferences(Sauvegarde.PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.remove(Sauvegarde.PREF_CONTACTS_ALL_VERSIONS);
		editor.remove(Sauvegarde.PREF_DERNIERE_SAUVEGARDE_SMS);
		editor.remove(Sauvegarde.PREF_DERNIERE_SAUVEGARDE_PHOTOS);
		editor.remove(Sauvegarde.PREF_DERNIERE_SAUVEGARDE_VIDEO);
		editor.remove(Sauvegarde.PREF_DERNIERE_SAUVEGARDE);
		editor.commit();
		CustomToast.Show(this, R.string.reinit_dates, Toast.LENGTH_SHORT);
	}

}
