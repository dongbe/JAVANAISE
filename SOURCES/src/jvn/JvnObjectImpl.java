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
	private Sentence objet=null;
	
	
	public Sentence getObjet() {
		return objet;
	}

	public void setObjet(Sentence objet) {
		this.objet = objet;
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
			Sentence test = (Sentence) js.jvnLockRead(jvnGetObjectId());
			this.objet= test.getObjet();
			
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
			
			System.out.println("ici objet: begin -> "+objet);
			Sentence test = (Sentence) js.jvnLockWrite(jvnGetObjectId());
			this.objet= test.getObjet();
			System.out.println("ici objet"+ test.getObjet());
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
		System.out.println("ici objet: begin -> "+objet);
		
		this.objet= (Sentence) js.jvnLockWrite(jvnGetObjectId());
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
		return objet;
	}

	public Serializable jvnInvalidateWriterForReader() throws JvnException {
		state = LockState.RC;
		return objet;
	}

}
