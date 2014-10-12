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
		mode = LockState.NL;
		
	}
	
	public synchronized void jvnLockRead() throws JvnException {
		/*
		 *  si l'objet a toujours le verrou ou le verrou en etat cached reutilisation
		 *  sinon demande de verrou au serveur
		 */
		if(js==null){
			js= JvnServerImpl.jvnGetServer();
			}
		if (mode.equals(LockState.W)|| mode.equals(LockState.R))/*pour read c'est pas très claire (es ce qu'il faut attendre ou non)*/
			try {
				wait();
				setObjet(js.jvnLockRead(jvnGetObjectId()));
				System.out.println("objet write recu par le client : "+ objet); 
				mode = LockState.R;
				
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
		
	
		else 
			{	setObjet(js.jvnLockRead(jvnGetObjectId()));
				System.out.println("objet write recu par le client : "+ objet); 
				mode = LockState.R;}	
		
	/*	if(mode.equals(LockState.RC)
				|| mode.equals(LockState.NL) 
				|| mode.equals(LockState.RC)
				||mode.equals(LockState.RWC)){
			setObjet(js.jvnLockRead(jvnGetObjectId()));
			System.out.println("objet read recu par le client : "+ objet);
			mode = LockState.R;
		}else{
			mode = LockState.R;
		}
*/	}

	public synchronized void jvnLockWrite() throws JvnException {
		/*
		 *  si l'objet a toujours le verrou ou le verrou en etat cached reutilisation
		 *  sinon demande de verrou au serveur
		 */
		if(js==null){
			js= JvnServerImpl.jvnGetServer();
			}
		if (mode.equals(LockState.W))/*pour read c'est pas très claire (es ce qu'il faut attendre ou non)*/
			try {
				wait();
				setObjet(js.jvnLockWrite(jvnGetObjectId()));
				System.out.println("objet write recu par le client : "+ objet); 
				mode = LockState.W;
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
		else 
			{	setObjet(js.jvnLockWrite(jvnGetObjectId()));
				System.out.println("objet write recu par le client : "+ objet); 
				mode = LockState.W;	
		}
			
		
	}

	public synchronized void jvnUnLock() throws JvnException {
		switch(mode){
		case R:
			mode=LockState.RC;
			notifyAll(); break;	
		case W:
			mode=LockState.WC;
			notifyAll(); break;
		default:
			mode=LockState.NL;
			notifyAll(); break;
		}
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

	public synchronized void jvnInvalidateReader() throws JvnException {
		mode = LockState.NL;
		notifyAll();
	}

	public synchronized Serializable jvnInvalidateWriter() throws JvnException {
		mode = LockState.NL;
		notifyAll();
		return (Serializable) objet;
	}

	public synchronized Serializable jvnInvalidateWriterForReader() throws JvnException {
		mode = LockState.R;
		notifyAll();
		return (Serializable) objet;
	}

}
