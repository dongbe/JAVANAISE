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
		/*if (state.equals(LockState.W)){
		data = text;
		}else{
			System.out.println("vous n'avez pas suffisament de droits");
		}*/
		data = text;
	}
	
	public String read() {
		/*String lecture=null;
		if(state.equals(LockState.R) || state.equals(LockState.RWC)){
		 lecture=data;
		}else{
			System.out.println("vous n'avez pas suffisament de droits");
		}*/
		return data;
	}	
}