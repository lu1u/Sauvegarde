/**
 * 
 */
package com.lpi.sauvegarde.SavedObjects;

import android.content.Context;
import android.database.Cursor;

import com.lpi.sauvegarde.Report;

/**
 * @author lucien
 *
 */
public abstract class SavedObjectReader
{
	protected static Context _context ;
	protected long	_depuis ;
	protected Cursor _cursor ;

	protected SavedObjectReader( Context context, long depuis, Cursor cursor)
	{
		_context = context;
		_depuis = depuis ;
		_cursor = cursor ;
	}
	
	/***
	 * Retourne true s'il y a des nouveaux objets depuis la derniere sauvegarde
	 * @return
	 */
	public boolean nouveauxDepuisDerniereSauvegarde()
	{
		// La requete query qui a retourne ce curseur doit comporter une clause de selection sur la date
		return count() > 0 ;
	}
	
	public long derniereSauvegarde()
	{
		return _depuis ;
	}
	
	public abstract SavedObject currentObject() ;
	public abstract void remplitReport( Report r, int NbSauvegardes) ;
	public abstract void remplitReportPasDeNouveau(Report report) ;
	public abstract void setDerniereSauvegarde( long now ) ;
	public abstract String getNotificationMessage( int no, int total );
	public abstract String getNotificationMessageFin( int total ) ;
	
	public int count()
	{
		if ( _cursor == null) 
			return 0 ;
		
		return _cursor.getCount() ;				
	}
	public void close()
	{
		if ( _cursor != null)
			_cursor.close();
		
		_cursor = null ;
	}
	public boolean moveToNext()
	{
		if (_cursor == null)
			return false ;
		
		return _cursor.moveToNext() ;
	}


	public static String getResourceString(int resId, Object... args)
	{
		String format = _context.getResources().getString(resId);
		return String.format(format, args);
	}
}
