/***
 * Sentence class : used for representing the text exchanged between users
 * during a chat application
 * Contact: 
 *
 * Authors: 
 */

package irc;


import java.io.Serializable;

import jvn.JvnCoordImpl.LockState;
import jvn.JvnObject;
import jvn.JvnObjectImpl;
import jvn.JvnSentenceItf;


public class Sentence  
			         implements  JvnSentenceItf,Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String 		data;
	
	//private State state=null;
  
	public Sentence() {
		data = new String("");
	}
	
	public void write(String text) {
		
		data = text;
		System.out.println(" ecriture sentence : "+data);
	}
	
	public String read() {
		return data;
	}	
}