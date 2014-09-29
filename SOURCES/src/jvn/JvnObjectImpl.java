package jvn;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum lockState { NL, RC, WC, R, W, RWC;};
	public lockState state;
	private int id;
	public JvnObjectImpl ()
	{
		id = hashCode();
		state = lockState.NL;
	}
	public void jvnLockRead() throws JvnException {
		
		state = lockState.R; 
	}

	public void jvnLockWrite() throws JvnException {
		state = lockState.W;
	}

	public void jvnUnLock() throws JvnException {
		state = lockState.NL;
	}

	public int jvnGetObjectId() throws JvnException {
		
		return id;
	}

	public Serializable jvnGetObjectState() throws JvnException {
		
		return state;
	}

	public void jvnInvalidateReader() throws JvnException {
		state = lockState.RC;
		
	}

	public Serializable jvnInvalidateWriter() throws JvnException {

		state = lockState.WC;
		return state; //TODO pourquoi un return ??
	}

	public Serializable jvnInvalidateWriterForReader() throws JvnException {
		state = lockState.RC;
		return state;//TODO pourquoi un return ??
	}

}
