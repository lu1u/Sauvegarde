/**
 * 
 */
package com.lpi.sauvegarde.Sauvegarde;

/**
 * @author lucien
 *
 */
public interface ProgressDlg
{
	public void setStage( String title ) ;
	public void setProgress( String format, int step, int Max ) ;
	public boolean isCanceled() ;
	public void notification( int i, Object... args) ;
	public void notification( String s) ;
}
