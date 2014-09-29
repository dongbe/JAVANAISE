/***
 * Sentence class : used for representing the text exchanged between users
 * during a chat application
 * Contact: 
 *
 * Authors: 
 */

package irc;

import java.io.Serializable;

import jvn.JvnException;
import jvn.JvnObject;

public class Sentence implements JvnObject, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String 		data;
	
	public enum State {
		unlock, reading, writing, waiting;
	}
	
	private State state=null;
  
	public Sentence() {
		data = new String("");
		state = State.waiting;
	}
	
	public void write(String text) {
		
		data = text;
	}
	public String read() {
		return data;	
	}

	public void jvnLockRead() throws JvnException {
		while(state!=State.unlock){
			state=State.waiting;
		}
		state= State.reading;
	}

	public void jvnLockWrite() throws JvnException {
		while(state!=State.unlock){
			state=State.waiting;
		}
		state= State.writing;
	}

	public void jvnUnLock() throws JvnException {
		state= State.unlock;
		
	}

	public int jvnGetObjectId() throws JvnException {
		// TODO Auto-generated method stub
		return 0;
	}

	public Serializable jvnGetObjectState() throws JvnException {
			
		return this;
	}

	public void jvnInvalidateReader() throws JvnException {
		// TODO Auto-generated method stub
		
	}

	public Serializable jvnInvalidateWriter() throws JvnException {
		// TODO Auto-generated method stub
		return null;
	}

	public Serializable jvnInvalidateWriterForReader() throws JvnException {
		// TODO Auto-generated method stub
		return null;
	}
	
}