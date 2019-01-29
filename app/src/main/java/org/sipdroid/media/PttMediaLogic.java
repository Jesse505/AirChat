package org.sipdroid.media;

public interface PttMediaLogic {

	/** Pause the thread of ptt sending**/
	public boolean pausePttSend();
	/** Pause the thread of ptt receiving**/
	public boolean pausePttRece();
	/** stop play the ptt audio **/
	public boolean stopPlay();
}
