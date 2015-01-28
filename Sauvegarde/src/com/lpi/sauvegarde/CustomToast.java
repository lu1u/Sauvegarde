/**
 * 
 */
package com.lpi.sauvegarde;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AnalogClock;
import android.widget.TextView;
import android.widget.Toast;

import com.lpi.sauvegarde.R.string;

/**
 * @author lucien
 *
 */
public class CustomToast
{
	public static void Show( Activity a, String message, int duree)
	{
		LayoutInflater myInflator= a.getLayoutInflater();
		 View myLayout=myInflator.inflate(R.layout.custom_toast,(ViewGroup)a.findViewById(R.id.toast_layout_root) );
		 TextView myMessage=(TextView)myLayout.findViewById(R.id.textView);
		 myMessage.setText(message);
		Toast myToast=new Toast(a.getApplicationContext());
		 myToast.setDuration(Toast.LENGTH_LONG);
		 myToast.setView(myLayout);
		 myToast.show();
	}
	
	public static void Show( Activity a, int message, int duree)
	{
		String m = a.getResources().getString(message) ;
		Show( a, m, duree ) ;
	}
}
