package com.lpi.sauvegarde.Sauvegarde;

import com.lpi.sauvegarde.MainActivity;
import com.lpi.sauvegarde.R;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

public class SauvegardeAuto extends AsyncTask<Void, Void, Void>  implements ProgressDlg
{
	Context _context ;
	NotificationManager _notificationManager;
	Notification _notification;
	NotificationCompat.Builder _builder;
	static final int NOTIFICATION_ID = 1;

	public SauvegardeAuto( Context c )
	{
		_context = c ;
	}
	
	@SuppressLint("NewApi")
	public void Notification(String message)
	{
		try
		{
			if (_builder == null)
			{
				_builder = new NotificationCompat.Builder(_context).setSmallIcon(R.drawable.ic_stat_sauvegarde)
						.setContentTitle(formatResourceString(R.string.app_name)).setContentText(message);
				Intent resultIntent = new Intent(_context, MainActivity.class);
				PendingIntent resultPendingIntent = PendingIntent.getActivity(_context, 0, resultIntent,
						PendingIntent.FLAG_UPDATE_CURRENT);

				_builder.setContentIntent(resultPendingIntent);
			} else
				_builder.setContentText(message);

			if (_notificationManager == null)
				_notificationManager = (NotificationManager) _context.getSystemService(Context.NOTIFICATION_SERVICE);

			_notificationManager.notify(NOTIFICATION_ID, _builder.build());
		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	@Override
	public void setStage(String title)
	{
		Notification( title ) ;
	}

	@Override
	public void setProgress(String format, int step, int Max)
	{
		if(  step % 10 == 0)
		{
			Notification( String.format(format, step, Max));
		}
	}
	

	@Override
	protected Void doInBackground(Void... params)
	{
		Sauvegarde sauve = new Sauvegarde(_context, this) ;
		sauve.execute(Sauvegarde.TYPE_LAUNCHED.MANUEL);
		return null;
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

	@Override
	public boolean isCanceled()
	{
		return false ;
	}
	
	/* (non-Javadoc)
	 * @see com.lpi.sauvegarde.Sauvegarde.ProgressDlg#notification(java.lang.String)
	 */
	@Override
	public void notification(int i, Object... args)
	{
		Notification( String.format(_context.getResources().getString(i), args));
	}
	/* (non-Javadoc)
	 * @see com.lpi.sauvegarde.Sauvegarde.ProgressDlg#notification(java.lang.String)
	 */
	@Override
	public void notification(String s)
	{
		Notification( s);
	}
	
	
}
