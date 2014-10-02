/***
 * Sentence class : used for representing the text exchanged between users
 * during a chat application
 * Contact: 
 *
 * Authors: 
 */

package irc;

import java.beans.Transient;
import java.io.Serializable;

import jvn.JvnObject;
import jvn.JvnObjectImpl;
import jvn.JvnServerImpl;

public class Sentence  
			 extends JvnObjectImpl 
			         implements  Serializable, JvnObject{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String 		data;
	
	//private State state=null;
  
	public Sentence() {
		super();
		data = new String("");
	}
	
	public void write(String text) {
		
		data = text;
	}
	
	public String read() {
		return data;	
	}	
}