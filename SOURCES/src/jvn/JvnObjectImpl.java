package jvn;

import java.io.Serializable;

import jvn.JvnCoordImpl.LockState;

public class JvnObjectImpl implements JvnObject, Serializable{
	/**
	 * 
	 */
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
	
	public synchronized void jvnLockRead() throws JvnException {
		/*
		 *  si l'objet a toujours le verrou ou le verrou en etat cached reutilisation
		 *  sinon demande de verrou au serveur
		 */
		if(js==null){
			js= JvnServerImpl.jvnGetServer();
			}
		if (mode.equals(LockState.W)|| mode.equals(LockState.R))/*pour read c'est pas tr�s claire (es ce qu'il faut attendre ou non)*/
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
		if (mode.equals(LockState.W))/*pour read c'est pas tr�s claire (es ce qu'il faut attendre ou non)*/
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
		
		JvnServerImpl.jvnGetServer().getCacheObj().put(id,objet);
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
		case RC:
			System.out.println("Mode Lecture cached");break;	
		case WC:
			System.out.println("Mode Ecriture cached");break;
		default:
			System.out.println("Mode Inconnu");break;
		}
		return objet;
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
		return objet;
	}

	public synchronized Serializable jvnInvalidateWriterForReader() throws JvnException {
		mode = LockState.R;
		notifyAll();
		return objet;
	}

}
