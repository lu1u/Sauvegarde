package com.lpi.sauvegarde;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lpi.sauvegarde.Sauvegarde.Plannificateur;
import com.lpi.sauvegarde.Sauvegarde.Sauvegarde;
import com.lpi.sauvegarde.Sauvegarde.SauvegardeAuto;

/**
 * 
 */

/**
 * @author lucien
 *
 */
public class AlarmReceiver extends BroadcastReceiver
{
	Context _context ;
		
	@Override
	public void onReceive(Context context, Intent intent)
	{
		_context = context ;
		
		if (Sauvegarde.COMMANDE_SAVE_ALARM.equals(intent.getAction()))
		{
			lanceSauvegardeAuto(context) ;
			
		} else if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()) //$NON-NLS-1$
				|| "android.intent.action.QUICKBOOT_POWERON".equals(intent.getAction())) //$NON-NLS-1$
		{

			Plannificateur plannificateur = new Plannificateur(context) ;
			plannificateur.plannifieSauvegarde();
		}
	}

	private void lanceSauvegardeAuto(Context context)
	{
		// TODO Auto-generated method stub
		SauvegardeAuto task = new SauvegardeAuto(context);
		task.execute();	
	}
}
