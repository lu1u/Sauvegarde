package com.lpi.sauvegarde;

import java.util.Calendar;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.lpi.sauvegarde.Sauvegarde.Plannificateur;
import com.lpi.sauvegarde.Sauvegarde.Sauvegarde;
import com.lpi.sauvegarde.Sauvegarde.SauvegardeManuelle;

public class MainActivity extends Activity
{
	static final String NonInitialise = "lpi.com.not.initialized!"; //$NON-NLS-1$
	static final String ACTION_SAVE_NOW = "lpi.com.sauvegarde.savenow"; //$NON-NLS-1$
	static final String ACTION_PLANNIFIE = "lpi.com.sauvegarde.plannifie"; //$NON-NLS-1$
	static final int HEURE_DIALOG_ID = 1;

	private boolean _sauvegardeEnCours;
	private TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute)
		{
			if (view.isShown()) // Pour palier a un bug d'android qui appelle cette fonction deux
								// fois au lieu d'une seule
			{
				SharedPreferences settings = getSharedPreferences(Sauvegarde.PREFERENCES, Context.MODE_PRIVATE);
				SharedPreferences.Editor edit = settings.edit();

				edit.putInt(Sauvegarde.PREF_SAUVEGARDE_AUTO_HEURE, hourOfDay);
				edit.putInt(Sauvegarde.PREF_SAUVEGARDE_AUTO_MINUTE, minute);
				edit.commit();

				Plannificateur plan = new Plannificateur(getBaseContext());
				plan.plannifieSauvegarde();

				setDefaultPlannificationValues();
			}
		}
	};

	// /////////////////////////////////////////////////////////////////////////
	/***
	 * Creation de l'activity
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setDefaultValues();

		// Receiver pour les intent qu'on nous envoie
		BroadcastReceiver br = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent)
			{
				onActivityReceive(intent);
			}
		};

		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_PLANNIFIE);
		filter.addAction(ACTION_SAVE_NOW);
		registerReceiver(br, filter);

		_sauvegardeEnCours = false;
	}

	protected void onActivityReceive(Intent intent)
	{
		if (intent == null)
			return;

		String action = intent.getAction();
		if (ACTION_SAVE_NOW.equals(action))
			lancerSauvegarde();
		else if (ACTION_PLANNIFIE.equals(action))
			plannifier();
	}

	private void plannifier()
	{
		Plannificateur plan = new Plannificateur(this);
		plan.plannifieSauvegarde();
		setDefaultPlannificationValues();
	}

	private void lancerSauvegarde()
	{
		((TextView) findViewById(R.id.buttonSaveNow)).setText(getResources().getString(R.string.btn_annuler));
		_sauvegardeEnCours = true;
		// Une sauvegarde est commencee
		((TextView) findViewById(R.id.buttonSaveNow)).setText(getResources().getString(R.string.btn_sauver_maintenant));
		_sauvegardeEnCours = false;

		SauvegardeManuelle task = new SauvegardeManuelle(this);
		task.execute();

		// Une sauvegarde est commencee
		((TextView) findViewById(R.id.buttonSaveNow)).setText(getResources().getString(R.string.btn_sauver_maintenant));
		_sauvegardeEnCours = false;

	}

	// /////////////////////////////////////////////////////////////////////////
	/***
	 * Teste si une adresse mail est valide
	 * 
	 * @param email
	 * @return
	 */
	public static boolean isValidEmailAddress(String email)
	{
		boolean result = true;
		try
		{
			InternetAddress emailAddr = new InternetAddress(email);
			emailAddr.validate();
		} catch (AddressException ex)
		{
			result = false;
		}
		return result;
	}

	/***
	 * Test si les informations de login pour envoyer les mails sont correctes
	 * 
	 * @param c
	 * @return
	 */
	static public boolean IsLoginOK(Context c)
	{
		SharedPreferences settings = c.getSharedPreferences(Sauvegarde.PREFERENCES, Context.MODE_PRIVATE);
		String adress = settings.getString(Sauvegarde.PREF_FROMADDRESS, NonInitialise);
		String mdp = settings.getString(Sauvegarde.PREF_PASSWORD, NonInitialise);

		if (!isValidEmailAddress(adress) || mdp.equals(NonInitialise))
			return false;

		return true;
	}

	/***
	 * Verifie qu'on a bien les infos du compte utilise pour envoyer les mails, ouvre un dialogue si
	 * besoin
	 * 
	 * @param action
	 *            : l'action qui accompagnera le message envoye apres validation du dialogue
	 * @return: true si les infos de login sont bien renseignees
	 */
	public void actionIfLoginOk(String action)
	{
		Intent intent = new Intent();
		intent.setAction(action);

		if (IsLoginOK(this))
			sendBroadcast(intent);
		else
		{
			// Afficher le dialogue pour saisir les infos de login
			DialogLogin dlg = new DialogLogin(this, intent);
			dlg.show();
		}
	}

	/***
	 * Initialise les champs de l'interface
	 */
	@SuppressWarnings("boxing")
	private void setDefaultValues()
	{
		// Lire les preferences
		SharedPreferences settings = getSharedPreferences(Sauvegarde.PREFERENCES, Context.MODE_PRIVATE);
		/*
		 * Map<String, ?> keys = settings.getAll(); for (Map.Entry<String, ?> entry :
		 * keys.entrySet()) Log.d("Preferences", entry.getKey() + ": " +
		 * entry.getValue().toString()); //$NON-NLS-1$ //$NON-NLS-2$
		 */
		// Adresse de destination

		// Derniere sauvegarde
		long date = settings.getLong(Sauvegarde.PREF_DERNIERE_SAUVEGARDE, 0);
		if (date != 0)
		{
			String sDate = Report.getLocalizedDate(date);
			// Toast.makeText(this,
			// String.format(getResources().getString(R.string.derniere_sauvegarde), sDate),
			// Toast.LENGTH_SHORT).show();

			CustomToast.Show(this, String.format(getResources().getString(R.string.derniere_sauvegarde), sDate),
					Toast.LENGTH_SHORT);
			((TextView) findViewById(R.id.textViewDerniereSauvegarde)).setText(String.format(
					getResources().getString(R.string.derniere_sauvegarde), sDate));
		}

		AutoCompleteTextView av = (AutoCompleteTextView) findViewById(R.id.editTextDestAddress);
		av.setText(settings.getString(Sauvegarde.PREF_DESTADDRESS, getDefaultAccount(this)));
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item,
				getDefaultAccounts(this));
		av.setThreshold(1);// will start working from first character
		av.setAdapter(adapter);// setting the adapter data into the AutoCompleteTextView

		// Contacts
		((CheckBox) findViewById(R.id.checkBoxContacts))
				.setChecked(settings.getBoolean(Sauvegarde.PREF_CONTACTS, true));

		// Journal Telephonique
		((CheckBox) findViewById(R.id.checkBoxJournalTelephone)).setChecked(settings.getBoolean(
				Sauvegarde.PREF_JOURNAL_TELEPHONE, true));

		// SMS
		((CheckBox) findViewById(R.id.checkBoxSMS)).setChecked(settings.getBoolean(Sauvegarde.PREF_SMS, true));

		// MMS
		((CheckBox) findViewById(R.id.checkBoxMMS)).setChecked(settings.getBoolean(Sauvegarde.PREF_MMS, true));

		// Photos
		((CheckBox) findViewById(R.id.checkBoxPhotos)).setChecked(settings.getBoolean(Sauvegarde.PREF_PHOTOS, true));

		// Video
		((CheckBox) findViewById(R.id.checkBoxVideo)).setChecked(settings.getBoolean(Sauvegarde.PREF_VIDEOS, true));

		// Heure de sauvegarde automatique
		((CheckBox) findViewById(R.id.checkBoxSauvegardeAuto)).setChecked(settings.getBoolean(
				Sauvegarde.PREF_SAUVEGARDE_AUTO_ACTIVEE, false));

		// Texte du bouton pour l'heure de sauvegarde
		Calendar cal = Plannificateur.getProchaineSauvegarde(this);
		Button buttonHeure = (Button) findViewById(R.id.buttonHeure);
		if (cal == null)
		{
			buttonHeure.setText("00:00"); //$NON-NLS-1$
			((TextView) findViewById(R.id.textViewProchaineSauvegarde)).setText(getResources().getString(
					R.string.sauvegarde_auto_desactivee));
		} else
		{
			int heure = settings.getInt(Sauvegarde.PREF_SAUVEGARDE_AUTO_HEURE, 10);
			int minute = settings.getInt(Sauvegarde.PREF_SAUVEGARDE_AUTO_MINUTE, 0);
			buttonHeure.setText(String.format("%d:%02d", heure, minute)); //$NON-NLS-1$
			((TextView) findViewById(R.id.textViewProchaineSauvegarde)).setText(Sauvegarde.formatResourceString(this,
					R.string.sauvegarde_auto_programmee, Sauvegarde.getLocalizedTimeAndDate(this, cal)));
		}
	}

	/***
	 * Initialise les champs de l'interface
	 */
	private void setDefaultPlannificationValues()
	{
		// Lire les preferences
		SharedPreferences settings = getSharedPreferences(Sauvegarde.PREFERENCES, Context.MODE_PRIVATE);

		// Heure de sauvegarde automatique
		((CheckBox) findViewById(R.id.checkBoxSauvegardeAuto)).setChecked(settings.getBoolean(
				Sauvegarde.PREF_SAUVEGARDE_AUTO_ACTIVEE, false));

		// Texte du bouton pour l'heure de sauvegarde
		Calendar cal = Plannificateur.getProchaineSauvegarde(this);
		Button buttonHeure = (Button) findViewById(R.id.buttonHeure);
		if (cal == null)
		{
			buttonHeure.setText("00:00"); //$NON-NLS-1$
			((TextView) findViewById(R.id.textViewProchaineSauvegarde)).setText(getResources().getString(
					R.string.sauvegarde_auto_desactivee));
		} else
		{
			int heure = settings.getInt(Sauvegarde.PREF_SAUVEGARDE_AUTO_HEURE, 10);
			int minute = settings.getInt(Sauvegarde.PREF_SAUVEGARDE_AUTO_MINUTE, 0);
			buttonHeure.setText(String.format("%d:%02d", heure, minute)); //$NON-NLS-1$
			((TextView) findViewById(R.id.textViewProchaineSauvegarde)).setText(Sauvegarde.formatResourceString(this,
					R.string.sauvegarde_auto_programmee, Sauvegarde.getLocalizedTimeAndDate(this, cal)));
		}
	}

	/***
	 * Enregistre les informations saisies
	 */
	private void enregistreParametres()
	{
		SharedPreferences settings = getSharedPreferences(Sauvegarde.PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();

		editor.putString(Sauvegarde.PREF_DESTADDRESS, ((AutoCompleteTextView) findViewById(R.id.editTextDestAddress))
				.getText().toString());
		editor.putBoolean(Sauvegarde.PREF_CONTACTS, ((CheckBox) findViewById(R.id.checkBoxContacts)).isChecked());
		editor.putBoolean(Sauvegarde.PREF_JOURNAL_TELEPHONE, ((CheckBox) findViewById(R.id.checkBoxJournalTelephone)).isChecked());
		editor.putBoolean(Sauvegarde.PREF_SMS, ((CheckBox) findViewById(R.id.checkBoxSMS)).isChecked());
		editor.putBoolean(Sauvegarde.PREF_MMS, ((CheckBox) findViewById(R.id.checkBoxMMS)).isChecked());
		editor.putBoolean(Sauvegarde.PREF_PHOTOS, ((CheckBox) findViewById(R.id.checkBoxPhotos)).isChecked());
		editor.putBoolean(Sauvegarde.PREF_VIDEOS, ((CheckBox) findViewById(R.id.checkBoxVideo)).isChecked());
		editor.putBoolean(Sauvegarde.PREF_SAUVEGARDE_AUTO_ACTIVEE,
				((CheckBox) findViewById(R.id.checkBoxSauvegardeAuto)).isChecked());

		editor.commit();
	}

	/**
	 * Cuisine android pour recuperer les infos si on ferme la fenetre
	 */
	protected void onSaveInstanceState(Bundle saveInstanceState)
	{

		super.onSaveInstanceState(saveInstanceState);
		// Lire les preferences
		enregistreParametres();
	}

	/***
	 * Retourne le compte mail par defaut
	 * 
	 * @param context
	 * @return
	 */
	static String getDefaultAccount(Context context)
	{
		AccountManager accountManager = AccountManager.get(context);
		Account[] accounts = accountManager.getAccountsByType("com.google"); //$NON-NLS-1$

		if (accounts != null)
			if (accounts.length > 0)
				return accounts[0].name;

		return ""; //$NON-NLS-1$
	}

	/***
	 * Retourne le compte mail par defaut
	 * 
	 * @param context
	 * @return
	 */
	@SuppressWarnings("nls")
	static String[] getDefaultAccounts(Context context)
	{
		AccountManager accountManager = AccountManager.get(context);
		Account[] accounts = accountManager.getAccountsByType("com.google");

		final int nb = accounts.length;
		String[] names = new String[nb];

		for (int i = 0; i < nb; i++)
			names[i] = accounts[i].name;
		return names;
	}

	/***
	 * Sauvegarde manuelle
	 * 
	 * @param v
	 */
	public void onClickSaveNow(View v)
	{
		if (_sauvegardeEnCours)
		{
			// Annuler la sauvegarde
			// sendBroadcast(new Intent(Sauvegarde.COMMANDE_SAUVE_ANNULE));
		} else
		{
			// Lancer une sauvegarde
			enregistreParametres();
			actionIfLoginOk(ACTION_SAVE_NOW);
		}
	}

	public void onClickParametresAvances(View v)
	{
		Intent i = new Intent(getBaseContext(), ParametresAvancesActivity.class);
		startActivity(i);
	}

	public void onClickSauvegardeAuto(View v)
	{
		SharedPreferences settings = getSharedPreferences(Sauvegarde.PREFERENCES, Context.MODE_PRIVATE);
		boolean activee = ((CheckBox) findViewById(R.id.checkBoxSauvegardeAuto)).isChecked();

		if (activee != settings.getBoolean(Sauvegarde.PREF_SAUVEGARDE_AUTO_ACTIVEE, false))
		{
			SharedPreferences.Editor edit = settings.edit();
			edit.putBoolean(Sauvegarde.PREF_SAUVEGARDE_AUTO_ACTIVEE, activee);
			edit.commit();
			actionIfLoginOk(ACTION_PLANNIFIE);
		}
	}

	public void onClickHeureSauvegarde(View v)
	{
		SharedPreferences settings = getSharedPreferences(Sauvegarde.PREFERENCES, Context.MODE_PRIVATE);
		int heure = settings.getInt(Sauvegarde.PREF_SAUVEGARDE_AUTO_HEURE, 10);
		int minute = settings.getInt(Sauvegarde.PREF_SAUVEGARDE_AUTO_MINUTE, 0);

		TimePickerDialog tp = new TimePickerDialog(this, timeSetListener, heure, minute, true);
		tp.show();
	}

	public String getLocalizedTime(Calendar c)
	{
		if (c == null)
			c = Calendar.getInstance();

		return android.text.format.DateFormat.getTimeFormat(this).format(c.getTime());
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause()
	{
		enregistreParametres();
		super.onPause();
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume()
	{
		setDefaultValues();
		super.onResume();
	}
}
