package jvn;

import irc.Sentence;

import java.io.Serializable;

import jvn.JvnCoordImpl.LockState;

public class JvnObjectImpl implements JvnObject{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LockState mode;
	private int id;
	private JvnServerImpl js=null;
	private Serializable objet=null;
	
	
	public Serializable getObjet() {
		return objet;
	}

	public void setObjet(Serializable serializable) {
		this.objet = serializable;
	}

	public JvnObjectImpl (Serializable sentence)
	{   
		//creation de l'objet en mode unlock 
		id = hashCode();
		mode = LockState.NL;
		objet=sentence;
		
	}
	
	public void jvnLockRead() throws JvnException {
		/*
		 *  si l'objet a toujours le verrou ou le verrou en etat cached reutilisation
		 *  sinon demande de verrou au serveur
		 */
		if(js==null){
		js= JvnServerImpl.jvnGetServer();
		}
		if(mode.equals(LockState.RC)){
			setObjet(js.jvnLockRead(jvnGetObjectId()));
			mode = LockState.R;
		}else{
			setObjet(js.jvnLockRead(jvnGetObjectId()));
			System.out.println("objet read recu par le client : "+ objet);
			mode = LockState.R;
		}
	}

	public synchronized void jvnLockWrite() throws JvnException {
		/*
		 *  si l'objet a toujours le verrou ou le verrou en etat cached reutilisation
		 *  sinon demande de verrou au serveur
		 */
		if(js==null){
			js= JvnServerImpl.jvnGetServer();
			}
		if(mode.equals(LockState.WC)){
			
			mode = LockState.W;
		}else{
			setObjet(js.jvnLockWrite(jvnGetObjectId()));
			System.out.println("objet write recu par le client : "+ objet); 
			mode = LockState.W;
		}
			
	}

	public synchronized void jvnUnLock() throws JvnException {
		switch(mode){
		case R:
			mode=LockState.RC;break;	
		case W:
			mode=LockState.WC;break;
		default:
			mode=LockState.NL;break;
		}
		System.out.println("Objet unlock: "+objet);
		js.getCacheObj().put(id, objet);
	}

	public int jvnGetObjectId() throws JvnException {
		
		return id;
	}

	public synchronized Serializable jvnGetObjectState() throws JvnException {
		switch(mode){
		case R:
			System.out.println("Mode Lecture");break;	
		case W:
			System.out.println("Mode Ecriture");break;
		default:
			System.out.println("Mode Inconnu");break;
		}
		return (Serializable) objet;
	}
	public Serializable jvnGetState() throws JvnException {
		
		return mode;
	}

	public void jvnInvalidateReader() throws JvnException {
		mode = LockState.NL;		
	}

	public Serializable jvnInvalidateWriter() throws JvnException {

		return (Serializable) objet;
	}

	public Serializable jvnInvalidateWriterForReader() throws JvnException {
		
		return (Serializable) objet;
	}

}
