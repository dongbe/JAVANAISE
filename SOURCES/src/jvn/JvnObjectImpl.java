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
		mode = LockState.W;
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
		if ( mode.equals(LockState.RC)){
			
				System.out.println("objet read reuse par le client : "+ objet); 
				mode = LockState.R;
		}
		else 
			{	setObjet(js.jvnLockRead(jvnGetObjectId()));
				System.out.println("objet read recu par le client : "+ objet); 
				mode = LockState.R;
				}	
		}

	public  void jvnLockWrite() throws JvnException {
		/*
		 *  si l'objet a toujours le verrou ou le verrou en etat cached reutilisation
		 *  sinon demande de verrou au serveur
		 */
		if(js==null){
			js= JvnServerImpl.jvnGetServer();
			}
		if (mode.equals(LockState.WC)){
			System.out.println("objet write reuse par le client : "+ objet); 
			mode = LockState.W;	
		}
		else 
			{					
				setObjet(js.jvnLockWrite(jvnGetObjectId()));
				System.out.println("objet write recu par le client : "+ objet); 
				mode = LockState.W;
			}		
	}

	public  synchronized void jvnUnLock() throws JvnException {
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
		JvnServerImpl.jvnGetServer().getCacheObj().put(id, this);
	}

	public int jvnGetObjectId() throws JvnException {
		
		return id;
	}

	public  Serializable jvnGetObjectState() throws JvnException {
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

	public  synchronized void jvnInvalidateReader() throws JvnException {
		if( mode == LockState.R ) {
			try {
				while( mode == LockState.R  ) {					
					this.wait();
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mode = LockState.NL;
			
		} else if( mode == LockState.RC ) {
			mode = LockState.NL;
		}		 
	}

	public synchronized Serializable jvnInvalidateWriter() throws JvnException {
		if( mode == LockState.W ) {
			try {
				while( mode == LockState.W  ) {					
					this.wait();
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mode = LockState.NL;
			
		} else if( mode == LockState.WC ) {
			mode = LockState.NL;
		}		 
		return objet;
	}

	public synchronized Serializable jvnInvalidateWriterForReader() throws JvnException {
		
		if( mode == LockState.W ) {
			try {
				while( mode == LockState.W  ) {					
					this.wait();
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mode = LockState.RWC;
			
		} else if( mode == LockState.WC ) {
			mode = LockState.RWC;
		}		 
		return objet;
	
	}
}
