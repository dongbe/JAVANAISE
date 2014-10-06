package jvn;

import irc.Sentence;

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
	private Object objet=null;
	
	
	public Object getObjet() {
		return objet;
	}

	public void setObjet(Object serializable) {
		this.objet = serializable;
	}

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
			
			setObjet(js.jvnLockRead(jvnGetObjectId()));
			System.out.println("object 4:"+objet);
		}	 
	}

	public synchronized void jvnLockWrite() throws JvnException {
		/*
		 *  si l'objet a toujours le verrou ou le verrou en etat cached reutilisation
		 *  sinon demande de verrou au serveur
		 */
		if(js==null){
			js= JvnServerImpl.jvnGetServer();
			System.out.println("ici objet: js -> "+js);
			}
		if(state.equals(LockState.WC) || state.equals(LockState.W)){
			state=LockState.W;
		}else{
							
			setObjet(js.jvnLockWrite(jvnGetObjectId()));
			System.out.println("ici objet: begin -> "+objet);
			
		}	 
	}

	public synchronized void jvnUnLock() throws JvnException {
		/*
		 * les verrous write et read sont en etat de cache 
		 * les communications avec le serveur ne sont pas necessaires
		 */
		
		/*if(state.equals(LockState.R)){
			state= LockState.RC;
		}else if(state.equals(LockState.W)){
			state= LockState.WC;
		}
		notifyAll();*/
	}

	public int jvnGetObjectId() throws JvnException {
		
		return id;
	}

	public synchronized Serializable jvnGetObjectState() throws JvnException {
		
		return (Serializable) objet;
	}
	public Serializable jvnGetState() throws JvnException {
		
		return state;
	}

	public void jvnInvalidateReader() throws JvnException {
		state = LockState.NL;		
	}

	public Serializable jvnInvalidateWriter() throws JvnException {

		state = LockState.WC;
		return (Serializable) objet;
	}

	public Serializable jvnInvalidateWriterForReader() throws JvnException {
		state = LockState.RC;
		return (Serializable) objet;
	}

}
