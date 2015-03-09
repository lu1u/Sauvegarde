package com.lpi.sauvegarde.Sauvegarde;

import java.util.Calendar;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.lpi.sauvegarde.R;
import com.lpi.sauvegarde.Report;
import com.lpi.sauvegarde.Mail.Mail;
import com.lpi.sauvegarde.Mail.MailInfo;
import com.lpi.sauvegarde.SavedObjects.AppelTelephoniqueReader;
import com.lpi.sauvegarde.SavedObjects.Contact;
import com.lpi.sauvegarde.SavedObjects.ContactsReader;
import com.lpi.sauvegarde.SavedObjects.MMSReader;
import com.lpi.sauvegarde.SavedObjects.PhotosReader;
import com.lpi.sauvegarde.SavedObjects.SMSReader;
import com.lpi.sauvegarde.SavedObjects.SavedObject;
import com.lpi.sauvegarde.SavedObjects.SavedObjectReader;
import com.lpi.sauvegarde.SavedObjects.VideosReader;

public class Sauvegarde
{
	public static final String TAG = "Sauvegarde"; //$NON-NLS-1$
	public static final String PREFERENCES = "com.lpi.sauvegarde.preferences"; //$NON-NLS-1$
	public static final String PREF_SAUVEGARDE_EN_COURS = "SauvegardeEnCours"; //$NON-NLS-1$
	public static final String PREF_CONTACTS_ALL_VERSIONS = "Contacts.AllVersions"; //$NON-NLS-1$
	public static final String PREF_DERNIERE_SAUVEGARDE = "DerniereSauvegarde"; //$NON-NLS-1$
	public static final String PREF_DERNIERE_SAUVEGARDE_SMS = "SMS.DerniereSauvegarde"; //$NON-NLS-1$
	public static final String PREF_DERNIERE_JOURNAL_TELEPHONE = "JournalTelephone.DerniereSauvegarde"; //$NON-NLS-1$
	public static final String PREF_DERNIERE_SAUVEGARDE_MMS = "MMS.DerniereSauvegarde"; //$NON-NLS-1$
	public static final String PREF_DERNIERE_SAUVEGARDE_PHOTOS = "Photos.DerniereSauvegarde"; //$NON-NLS-1$
	public static final String PREF_DERNIERE_SAUVEGARDE_VIDEO = "Videos.DerniereSauvegarde"; //$NON-NLS-1$
	public static final String PREF_RAPPORT = "Send.Report" ;//$NON-NLS-1$
	public static final String PREF_FROMADDRESS = "FromAddress"; //$NON-NLS-1$
	public static final String PREF_PASSWORD = "Password"; //$NON-NLS-1$
	public static final String PREF_CONTACTS = "Contacts"; //$NON-NLS-1$
	public static final String PREF_JOURNAL_TELEPHONE = "JournalTelephone"; //$NON-NLS-1$
	public static final String PREF_SMS = "SMS"; //$NON-NLS-1$
	public static final String PREF_MMS = "MMS"; //$NON-NLS-1$
	public static final String PREF_PHOTOS = "Photos"; //$NON-NLS-1$
	public static final String PREF_VIDEOS = "Videos"; //$NON-NLS-1$
	public static final String PREF_SAUVEGARDE_AUTO_HEURE = "HeureAutomatique.Heure"; //$NON-NLS-1$
	public static final String PREF_SAUVEGARDE_AUTO_MINUTE = "HeureAutomatique.Minute"; //$NON-NLS-1$
	public static final String PREF_SAUVEGARDE_AUTO_ACTIVEE = "SauvegardeAutomatique"; //$NON-NLS-1$
	public static final String PREF_WIFI = "WIFI"; //$NON-NLS-1$
	public static final String PREF_SMTP = "SMTP"; //$NON-NLS-1$
	public static final String PREF_PORT = "Port"; //$NON-NLS-1$
	public static final String PREF_DESTADDRESS = "DestAddress"; //$NON-NLS-1$
	public final static String COMMANDE_SAVE_ALARM = "lpi.Sauvegarde.Alarme"; //$NON-NLS-1$

	static final int NOTIFICATION_ID = 1;
	static final long DELAI_MIN = 30 * 1000; // Delai en millisecondes entre deux sauvegardes
												// programmees
	public static final int RAPPORT_JAMAIS = 0 ;
	public static final int RAPPORT_ERREUR = 1 ;
	public static final int RAPPORT_TOUJOURS = 2 ;
	
	Context _context;
	ProgressDlg _dlg;

	enum TYPE_LAUNCHED
	{
		MANUEL, AUTO
	}

	NotificationManager _notificationManager;
	Notification _notification;
	RemoteViews _remoteViews;
	NotificationCompat.Builder _builder;

	public Sauvegarde(Context context, ProgressDlg dlg)
	{
		_context = context;
		_dlg = dlg;
	}

	public void execute(Activity a, TYPE_LAUNCHED type)
	{
		if (type == TYPE_LAUNCHED.MANUEL && a != null)
		{
			int current_orientation = _context.getResources().getConfiguration().orientation;
			if (current_orientation == Configuration.ORIENTATION_LANDSCAPE)
			{
				a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			} else
			{
				a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
		}

		SharedPreferences settings = _context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
		if (settings.getBoolean(PREF_WIFI, true))
		{
			if (!IsWifiConnected())
			{
				_dlg.notification(R.string.non_connecte_wifi);
				return;
			}
		}

		sauvegardeEnCours(true);
		statusSauvegardeCommencee(settings);

		Report report = new Report();
		report.Log("Depart sauvegarde " + (type == TYPE_LAUNCHED.MANUEL ? "manuelle" : "automatique")); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
		MailInfo mInfo = new MailInfo();
		mInfo.read(settings);
		report.Log("dest adresse: " + mInfo.destAddress); //$NON-NLS-1$
		report.Log("compte envoi: " + mInfo.fromAdress + " host/port: " + mInfo.SMTP + "/" + mInfo.Port); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		sauveContacts(_context, settings, mInfo, report);
		if (!_dlg.isCanceled())
		{
			sauveAppels(_context, settings, mInfo, report);
			if (!_dlg.isCanceled())
			{
				sauveSMS(_context, settings, mInfo, report);
				if (!_dlg.isCanceled())
				{
					sauveMMS(_context, settings, mInfo, report);
					if (!_dlg.isCanceled())
					{
						sauvePhotos(_context, settings, mInfo, report);
						if (!_dlg.isCanceled())
						{
							sauveVideo(_context, settings, mInfo, report);
						}
					}
				}
			}
		}

		if (_dlg.isCanceled())
			report.Log("sauvegarde annulee par l'utilisateur"); //$NON-NLS-1$
		else
			report.Log("Sauvegarde terminee"); //$NON-NLS-1$

		sendReport(report);
		statusSauvegardeFinie(report);
		sauvegardeEnCours(false);

		if (type == TYPE_LAUNCHED.MANUEL && a != null)
		{
			a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		}
	}

	/***
	 * Mise a jour de l'interface pour dire que la sauvegarde est finie
	 * 
	 * @param report
	 */
	private void statusSauvegardeFinie(Report report)
	{
		_dlg.notification(_dlg.isCanceled() ? R.string.sauvegarde_annulee : R.string.sauvegarde_terminee,
				report.toString());
		// Intent intent = new Intent(COMMANDE_INFOS_FROM_SERVICE);
		// intent.putExtra(EXTRA_TYPE, EXTRA_SAUVEGARDE_EN_COURS);
		// intent.putExtra(EXTRA_SAUVEGARDE_EN_COURS, false);
		// sendBroadcast(intent);
	}

	/***
	 * Mise a jour de l'interface pour dire que la sauvegarde est commencee
	 * 
	 * @param report
	 */
	private void statusSauvegardeCommencee(SharedPreferences settings)
	{
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(PREF_DERNIERE_SAUVEGARDE, System.currentTimeMillis());
		editor.commit();
	}

	/***
	 * Affichage de message d'erreur
	 * 
	 * @param localizedMessage
	 */
	public static void Erreur(Context c, String localizedMessage)
	{
		Log.e(TAG, localizedMessage);
		Toast.makeText(c, "ERREUR: " + localizedMessage, Toast.LENGTH_LONG).show(); //$NON-NLS-1$
	}

	/***
	 * Envoi un mail de rapport a l'adresse du developpeur
	 * 
	 * @param report
	 */
	private void sendReport(Report report)
	{
		boolean envoyer = false ; 
		switch( LitPreferenceRapport(_context))
		{
		case RAPPORT_ERREUR :
			envoyer = report._erreurDetectee ;
			break ;
			
		case RAPPORT_TOUJOURS :
			envoyer = true ;
			break ;
		default :
			envoyer = false ;
		}
		
		if ( ! envoyer )
			return ;

		SharedPreferences settings = _context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
		MailInfo mInfo = new MailInfo();
		mInfo.read(settings);

		Mail m = new Mail(mInfo);
		String[] toArr = { "lucien.pilloni@gmail.com" }; //$NON-NLS-1$
		m.setTo(toArr);
		report.FillMail(m);
		try
		{
			m.send();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/***
	 * Retourne true si la WIFI est connectee
	 * 
	 * @return
	 */
	private boolean IsWifiConnected()
	{
		ConnectivityManager connManager = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (mWifi == null)
			return false;

		return mWifi.isConnected();
	}

	/***
	 * Retourne vrai si une sauvegarde est en cours
	 * Info memorisee dans les preferences
	 * @return
	 */
	public synchronized boolean sauvegardeEnCours()
	{
		SharedPreferences settings = _context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
		return settings.getBoolean(PREF_SAUVEGARDE_EN_COURS, false);
	}

	private synchronized void sauvegardeEnCours(boolean enCours)
	{
		SharedPreferences settings = _context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(PREF_SAUVEGARDE_EN_COURS, enCours);
		editor.commit();
	}

	/***
	 * Sauvegarde les videos
	 * 
	 * @param context
	 * @param settings
	 * @param mInfo
	 * @param report
	 */
	private void sauveVideo(Context context, SharedPreferences settings, MailInfo mInfo, Report report)
	{
		if (settings.getBoolean(PREF_VIDEOS, true))
		{
			report.Log("Sauvegarde des videos"); //$NON-NLS-1$
			long derniereSauvegarde = settings.getLong(PREF_DERNIERE_SAUVEGARDE_VIDEO, 0);
			_dlg.setStage(formatResourceString(R.string.sauve_videos));
			sauveObjects(context, mInfo, report, new VideosReader(context, derniereSauvegarde));
		} else
		{
			report.SauvegardeVideos = formatResourceString(R.string.sauvegarde_ignoree_videos);
			report.Log(report.SauvegardeVideos);
		}
	}

	/***
	 * Sauvegarde les photos
	 * 
	 * @param context
	 * @param settings
	 * @param mInfo
	 * @param report
	 */
	private void sauvePhotos(Context context, SharedPreferences settings, MailInfo mInfo, Report report)
	{
		if (settings.getBoolean(PREF_PHOTOS, true))
		{
			report.Log("Sauvegarde des photos"); //$NON-NLS-1$
			long derniereSauvegarde = settings.getLong(PREF_DERNIERE_SAUVEGARDE_PHOTOS, 0);
			_dlg.setStage(formatResourceString(R.string.sauve_photos));
			sauveObjects(context, mInfo, report, new PhotosReader(context, derniereSauvegarde));
		} else
		{
			report.SauvegardePhotos = formatResourceString(R.string.sauvegarde_ignoree_photos);
			report.Log(report.SauvegardePhotos);
		}
	}

	/***
	 * Sauve les SMS
	 * 
	 * @param context
	 * @param settings
	 * @param destMail
	 * @param password
	 * @param sMTP
	 * @param port
	 * @param report
	 */
	private void sauveSMS(Context context, SharedPreferences settings, MailInfo mInfo, Report report)
	{
		if (settings.getBoolean(PREF_SMS, true))
		{
			report.Log("Sauvegarde des SMS"); //$NON-NLS-1$
			long derniereSauvegarde = settings.getLong(PREF_DERNIERE_SAUVEGARDE_SMS, 0);
			_dlg.setStage(formatResourceString(R.string.sauve_sms));
			sauveObjects(context, mInfo, report, new SMSReader(context, derniereSauvegarde));
		} else
		{
			report.SauvegardeSMS = formatResourceString(R.string.sauvegarde_ignoree_sms);
			report.Log(report.SauvegardeSMS);
		}
	}
	
	/***
	 * Sauve les appels telephoniques
	 * 
	 * @param context
	 * @param settings
	 * @param destMail
	 * @param password
	 * @param sMTP
	 * @param port
	 * @param report
	 */
	private void sauveAppels(Context context, SharedPreferences settings, MailInfo mInfo, Report report)
	{
		if (settings.getBoolean(PREF_JOURNAL_TELEPHONE, true))
		{
			report.Log("Sauvegarde des appels téléphoniques"); //$NON-NLS-1$
			long derniereSauvegarde = settings.getLong(PREF_DERNIERE_JOURNAL_TELEPHONE, 0);
			_dlg.setStage(formatResourceString(R.string.sauve_appels));
			sauveObjects(context, mInfo, report, new AppelTelephoniqueReader(context, derniereSauvegarde));
		} else
		{
			report.SauvegardeCallLog = formatResourceString(R.string.sauvegarde_ignoree_appels);
			report.Log(report.SauvegardeSMS);
		}
	}

	/***
	 * Sauve les MMS
	 * 
	 * @param context
	 * @param settings
	 * @param destMail
	 * @param password
	 * @param sMTP
	 * @param port
	 * @param report
	 */
	private void sauveMMS(Context context, SharedPreferences settings, MailInfo mInfo, Report report)
	{
		if (settings.getBoolean(PREF_MMS, true))
		{
			report.Log("Sauvegarde des MMS"); //$NON-NLS-1$
			long derniereSauvegarde = settings.getLong(PREF_DERNIERE_SAUVEGARDE_MMS, 0);
			_dlg.setStage(formatResourceString(R.string.sauve_mms));
			sauveObjects(context, mInfo, report, new MMSReader(context, derniereSauvegarde));
		} else
		{
			report.SauvegardeMMS = formatResourceString(R.string.sauvegarde_ignoree_mms);
			report.Log(report.SauvegardeMMS);
		}
	}

	/***
	 * Fonction qui sauvegarde tous les types d'objets en envoyant un mail par objet
	 * 
	 * @param context
	 * @param Informations
	 *            pour l'envoi de mail
	 * @param lecteur
	 *            et createur des objets
	 */
	private void sauveObjects(Context context, MailInfo mInfo, Report report, SavedObjectReader objectsReader)
	{
		report.Log("Derniere sauvegarde " + Report.getLocalizedDate(objectsReader.derniereSauvegarde())); //$NON-NLS-1$
		if (!objectsReader.nouveauxDepuisDerniereSauvegarde())
		{
			report.Log("Pas de nouveaux depuis la derniere sauvegarde"); //$NON-NLS-1$
			objectsReader.remplitReportPasDeNouveau(report);
		} else
		{
			int total = objectsReader.count();
			report.Log(total + " a sauvegarder"); //$NON-NLS-1$

			int no = 0;

			while (objectsReader.moveToNext())
			{
				if (_dlg.isCanceled())
				{
					report.Log("Annulation de la sauvegarde"); //$NON-NLS-1$
					objectsReader.close();
					return;
				}

				no++;
				_dlg.setProgress("%s/%s", no, total); //$NON-NLS-1$
				SavedObject object = null;
				try
				{
					object = objectsReader.currentObject();
					Mail m = new Mail(mInfo);
					object.construitMail(context, m);

					m.send();
				} catch (Exception e)
				{
					// Erreur dans l'envoi de cet objet, on l'enregistre et on
					// continue a sauvegarder les suivants
					report._erreurDetectee = true ;
					report.Log(object.identification(context));
					report.Log(e);
				} finally
				{
					if (object != null)
						object.confirmeEnvoi(context);
				}
			}

			objectsReader.close();
			_dlg.notification(objectsReader.getNotificationMessageFin(total));

			report.Log("Tous les objets ont été sauvegardés"); //$NON-NLS-1$
			objectsReader.remplitReport(report, total);
		}
		objectsReader.setDerniereSauvegarde(Calendar.getInstance().getTimeInMillis());
	}

	/***
	 * Recuperer la liste des contacts, les regrouper dans un mail et les envoyer a l'adresse donnee
	 * 
	 * @param settings
	 * @param infos
	 *            pour l'envoi de mails
	 */
	private void sauveContacts(Context context, SharedPreferences settings, MailInfo mInfo, Report report)
	{
		if (!settings.getBoolean(PREF_CONTACTS, true))
		{
			report.Log("L'utilisateur a choisi de ne pas sauver les contacts"); //$NON-NLS-1$
			report.SauvegardeContacts = formatResourceString(R.string.sauvegarde_ignoree_contact);
			return;
		}
		_dlg.setStage(formatResourceString(R.string.sauvegarde_contacts));

		ContactsReader objectReader = new ContactsReader(context);
		if (!objectReader.nouveauxDepuisDerniereSauvegarde())
		{
			report.Log("Pas de nouveaux contacts depuis la derniere sauvegarde"); //$NON-NLS-1$
			objectReader.remplitReportPasDeNouveau(report);
			return;
		}

		_dlg.notification(R.string.sauvegarde_contacts);
		StringBuilder s = new StringBuilder();

		int total = objectReader.count();
		int no = 0;
		report.Log(total + " contacts a envoyer"); //$NON-NLS-1$

		while (objectReader.moveToNext())
		{
			if (_dlg.isCanceled())
			{
				report.Log("Annulation de la sauvegarde"); //$NON-NLS-1$
				objectReader.close();
				return;
			}

			no++;
			if (no % 5 == 0)
				_dlg.setProgress("%s/%s", no, total); //$NON-NLS-1$

			// On n'ajoute que les contacts qui ont des numeros de telephone ou un email
			Contact c = objectReader.currentObject();
			if (c._numeros != null || (c._eMails != null))
				c.appendToMail(context, s);
		}

		objectReader.close();
		s.append(formatResourceString(R.string.message_fin_mail));

		// Envoyer la liste des contacts par mail
		Mail m = new Mail(mInfo);
		m.setSubject(formatResourceString(R.string.sujet_contact));
		m.setBody(s.toString());

		_dlg.setStage(formatResourceString(R.string.envoi_contacts));
		try
		{
			m.send();
		} catch (Exception e)
		{
			Log.e(TAG, e.getLocalizedMessage());
			Log.e(TAG, e.getStackTrace().toString());
			report._erreurDetectee = true ;
			report._message = "Erreur lors de l'envoi d\'un mail, vérifiez les paramètres"; //$NON-NLS-1$
			report.Log("Erreur lors de l'envoi des contacts"); //$NON-NLS-1$
			report.Log(e.getLocalizedMessage());
			report.Log(e.getStackTrace().toString());
			return;
		}

		report.Log("Contacts envoyés"); //$NON-NLS-1$

		_dlg.notification(objectReader.getNotificationMessageFin(total));
		objectReader.remplitReport(report, total);
		objectReader.setDerniereSauvegarde(Calendar.getInstance().getTimeInMillis());
	}

	/***
	 * Charge une chaine de caracteres depuis les ressources et ajoute eventuellement des arguments
	 * 
	 * @param resId
	 * @param args
	 * @return
	 */
	public String formatResourceString(int resId, Object... args)
	{
		String format = _context.getResources().getString(resId);
		return String.format(format, args);
	}

	/***
	 * Charge une chaine de caracteres depuis les ressources et ajoute eventuellement des arguments
	 * 
	 * @param resId
	 * @param args
	 * @return
	 */
	static public String formatResourceString(Context context, int resId, Object... args)
	{
		String format = context.getResources().getString(resId);
		return String.format(format, args);
	}

	public String getLocalizedTimeAndDate(Calendar c)
	{
		if (c == null)
			c = Calendar.getInstance();

		String s = android.text.format.DateFormat.getDateFormat(_context).format(c.getTime()) + ' '
				+ android.text.format.DateFormat.getTimeFormat(_context).format(c.getTime());

		return s;
	}

	public static String getLocalizedTimeAndDate(Context context, Calendar c)
	{
		if (c == null)
			c = Calendar.getInstance();

		String s = android.text.format.DateFormat.getDateFormat(context).format(c.getTime()) + ' '
				+ android.text.format.DateFormat.getTimeFormat(context).format(c.getTime());

		return s;
	}

	public static void setPreferenceRapport(Context c, int rapport)
	{
		SharedPreferences settings = c.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
		Editor editor = settings.edit() ;
		editor.putInt( PREF_RAPPORT, rapport ) ;
		editor.commit() ;
	}

	public static int LitPreferenceRapport(Context c)
	{
		SharedPreferences settings = c.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
		return settings.getInt( PREF_RAPPORT, RAPPORT_JAMAIS) ;
	}
}
