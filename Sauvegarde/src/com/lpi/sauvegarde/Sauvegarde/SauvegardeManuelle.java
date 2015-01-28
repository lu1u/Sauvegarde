package com.lpi.sauvegarde.Sauvegarde;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.lpi.sauvegarde.MainActivity;
import com.lpi.sauvegarde.R;

public class SauvegardeManuelle extends AsyncTask<Void, Void, Void> implements ProgressDlg
{
	private ProgressDialog dialog;
	private Context _context ;
	private String _message ;
	private boolean _canceled ;
	
	public SauvegardeManuelle(MainActivity activity)
	{
		_canceled = false ;
		dialog = new ProgressDialog(activity);
		_context = activity ;
	}

	@Override
	protected void onPreExecute()
	{
		dialog.setTitle(_context.getResources().getString(R.string.app_name));
		dialog.setMessage(""); //$NON-NLS-1$
		dialog.setIndeterminate(false);
		dialog.setCancelable(true);
		dialog.setMax(1000);
		dialog.setButton(ProgressDialog.BUTTON_NEUTRAL, _context.getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				_canceled = true ;
			}
		});

		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.show();
	}

	@Override
	protected void onPostExecute(Void result)
	{
		if (dialog.isShowing())
		{
			dialog.dismiss();
		}
	}

	@Override
	protected Void doInBackground(Void... params)
	{
		Sauvegarde sauve = new Sauvegarde(_context, this) ;
		sauve.execute(Sauvegarde.TYPE_LAUNCHED.MANUEL);
		return null;
	}

	@Override
	public void setStage(String title)
	{
		try
		{
			_message = title;
			publishProgress();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void setProgress(String s, int step, int Max)
	{
		dialog.setMax(Max);
		dialog.setProgress(step);
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onProgressUpdate(java.lang.Object[])
	 */
	@Override
	protected void onProgressUpdate(Void... values)
	{
		if ( _message != null)
			dialog.setMessage(_message);
		super.onProgressUpdate(values);
	}

	@Override
	public boolean isCanceled()
	{
		return _canceled ;
	}

	/* (non-Javadoc)
	 * @see com.lpi.sauvegarde.Sauvegarde.ProgressDlg#notification(java.lang.String)
	 */
	@Override
	public void notification(int i, Object... args)
	{
		_message = String.format(_context.getResources().getString(i), args);
		publishProgress();
	}

	/* (non-Javadoc)
	 * @see com.lpi.sauvegarde.Sauvegarde.ProgressDlg#notification(java.lang.String)
	 */
	@Override
	public void notification(String s)
	{
		_message = s;
		publishProgress();
	}
	
	

}