package jvn;

import java.io.Serializable;

import jvn.JvnCoordImpl.LockState;

public class JvnObjectImpl implements JvnObject{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LockState state;
	private int id;
	private JvnServerImpl js=null;
	private JvnObject objet;
	
	
	public JvnObjectImpl ()
	{   
		//creation de l'objet en mode unlock 
		id = hashCode();
		state = LockState.NL;
		
	}
	
	public void jvnLockRead() throws JvnException {
		/*
		 *  si l'objet a toujours le verrou ou le verrou en etat cached reutilisation
		 *  sinon demande de verrou au serveur
		 */
		if(js==null){
		js= JvnServerImpl.jvnGetServer();
		}
		if(state.equals(LockState.RC) || state.equals(LockState.R)){
			state=LockState.R;
		} else if(state.equals(LockState.RWC)){
			
		}
		else{
			state=(LockState) js.jvnLockRead(jvnGetObjectId());
		}	 
	}

	public void jvnLockWrite() throws JvnException {
		/*
		 *  si l'objet a toujours le verrou ou le verrou en etat cached reutilisation
		 *  sinon demande de verrou au serveur
		 */
		if(js==null){
			js= JvnServerImpl.jvnGetServer();
			}
		if(state.equals(LockState.WC) || state.equals(LockState.W)){
			state=LockState.W;
		}else{
			state=(LockState) js.jvnLockWrite(jvnGetObjectId());
		}	 
	}

	public synchronized void jvnUnLock() throws JvnException {
		/*
		 * les verrous write et read sont en etat de cache 
		 * les communications avec le serveur ne sont pas necessaires
		 */
		
		if(state.equals(LockState.R)){
			state= LockState.RC;
		}else if(state.equals(LockState.W)){
			state= LockState.WC;
		}
		notifyAll();
	}

	public int jvnGetObjectId() throws JvnException {
		
		return id;
	}

	public Serializable jvnGetObjectState() throws JvnException {
		
		return objet;
	}
	public Serializable jvnGetState() throws JvnException {
		
		return state;
	}

	public void jvnInvalidateReader() throws JvnException {
		state = LockState.NL;		
	}

	public Serializable jvnInvalidateWriter() throws JvnException {

		state = LockState.WC;
		return this; //TODO pourquoi un return ??
	}

	public Serializable jvnInvalidateWriterForReader() throws JvnException {
		state = LockState.RC;
		return this;//TODO pourquoi un return ??
	}

}
