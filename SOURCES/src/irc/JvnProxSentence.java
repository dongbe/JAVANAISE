/***
 * Sentence class : used for representing the text exchanged between users
 * during a chat application
 * Contact: 
 *
 * Authors: 
 */

package irc;


import java.io.Serializable;

import jvn.JvnObject;
import jvn.JvnObjectImpl;
import jvn.JvnSentenceItf;


public class JvnProxSentence
					extends JvnObjectImpl
			         implements  JvnSentenceItf, Serializable, JvnObject{
	/**
	 * 
	 */
  
	public JvnProxSentence() {
		super(new Sentence());
	}
	
	public void write(String text) {
	}
	
	public String read() {
		return null;
	}	
}