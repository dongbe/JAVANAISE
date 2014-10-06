/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.io.Serializable;

public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord {

	private HashMap<String, JvnObject> naming;
	private HashMap<JvnCodeOS, JvnStatus> locktable;

	public enum LockState {
		NL, RC, WC, R, W, RWC;
	}

	private int id;

	/**
	 * Default constructor
	 * 
	 * @throws JvnException
	 **/
	public JvnCoordImpl() throws Exception {
		id = hashCode();

		naming = new HashMap<String, JvnObject>();
		locktable = new HashMap<JvnCodeOS, JvnStatus>();
		System.out.println("id :" + id);
	}

	/**
	 * Allocate a NEW JVN object id (usually allocated to a newly created JVN
	 * object)
	 * 
	 * @throws java.rmi.RemoteException
	 *             ,JvnException
	 **/
	public int jvnGetObjectId() throws java.rmi.RemoteException,
			jvn.JvnException {
		return id;
	}

	/**
	 * Associate a symbolic name with a JVN object
	 * 
	 * @param jon
	 *            : the JVN object name
	 * @param jo
	 *            : the JVN object
	 * @param joi
	 *            : the JVN object identification
	 * @param js
	 *            : the remote reference of the JVNServer
	 * @throws java.rmi.RemoteException
	 *             ,JvnException
	 **/
	public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
			throws java.rmi.RemoteException, jvn.JvnException {

		naming.put(jon, jo);
		JvnCodeOS code = new JvnCodeOS(jo.jvnGetObjectId(), js);
		JvnStatus status = new JvnStatus(jon, LockState.NL);
		locktable.put(code,status);
	}

	/**
	 * Get the reference of a JVN object managed by a given JVN server
	 * 
	 * @param jon
	 *            : the JVN object name
	 * @param js
	 *            : the remote reference of the JVNServer
	 * @throws java.rmi.RemoteException
	 *             ,JvnException
	 **/
	public JvnObject jvnLookupObject(String jon, JvnRemoteServer js)
			throws java.rmi.RemoteException, jvn.JvnException {
		JvnObject jvnObject = null;
		if (naming.containsKey(jon)) {
			jvnObject = naming.get(jon);
		}

		return jvnObject;
	}

	/**
	 * Get a Read lock on a JVN object managed by a given JVN server
	 * 
	 * @param joi
	 *            : the JVN object identification
	 * @param js
	 *            : the remote reference of the server
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException
	 *             , JvnException
	 **/
	public synchronized Serializable jvnLockRead(int joi, JvnRemoteServer js)
			throws java.rmi.RemoteException, JvnException {
		
		JvnCodeOS code = new JvnCodeOS(joi, js);
		System.out.println("table des avant etats : "+locktable.get(code).getJon());
		if(locktable.get(code).getState().equals(LockState.W) || locktable.get(code).getState().equals(LockState.WC)){
			js.jvnInvalidateWriterForReader(joi);
		}else{
			locktable.get(code).setState(LockState.R);
		}
		
		System.out.println("table des apres etats : "+locktable.get(code).getState());
		return locktable.get(code).getState();
	}

	/**
	 * Get a Write lock on a JVN object managed by a given JVN server
	 * 
	 * @param joi
	 *            : the JVN object identification
	 * @param js
	 *            : the remote reference of the server
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException
	 *             , JvnException
	 **/
	public synchronized Serializable jvnLockWrite(int joi, JvnRemoteServer js)
			throws java.rmi.RemoteException, JvnException {
		
		JvnCodeOS code = new JvnCodeOS(joi, js);
		System.out.println("table des avant etats : "+locktable.get(code).getJon());
		for( Entry<JvnCodeOS,JvnStatus> ent : locktable.entrySet() ){
			int obj=ent.getKey().getJoi();
			JvnRemoteServer server=ent.getKey().getJs();
			/*
			 * si l'objet est detenu par tous les serveurs en lecture
			 * on accorde le verrou en ecriture et on invalide les verrous en lecture
			 */
			if(joi==obj && ent.getValue().getState().equals(LockState.R) || ent.getValue().getState().equals(LockState.RC)){
				server.jvnInvalidateReader(joi);
				locktable.get(code).setState(LockState.W);
			}
			/*
			 * si l'objet est detenu par un serveur en ecriture 
			 * on attend qu'il libere le verrou
			 */
			if(joi==obj && ent.getValue().getState().equals(LockState.W)){
				JvnObject test =jvnLookupObject(ent.getValue().getJon(),js);
				try {
					test.wait();
				} catch (InterruptedException e) {
					System.out.println("coord write :"+e.getMessage());
				}
			}
			/*
			 * si l'objet est detenu par un serveur en ecriture cache ou inutilise
			 * on invalide le verrou et on l'attribue
			 */
			if(joi==obj && ent.getValue().getState().equals(LockState.WC) || ent.getValue().getState().equals(LockState.RWC)){
				server.jvnInvalidateWriter(joi);
			}
		}
		
		System.out.println("table des apres etats : "+locktable.get(code).getState());
		return locktable.get(code).getState();
	}

	/**
	 * A JVN server terminates
	 * 
	 * @param js
	 *            : the remote reference of the server
	 * @throws java.rmi.RemoteException
	 *             , JvnException
	 **/
	public void jvnTerminate(JvnRemoteServer js)
			throws java.rmi.RemoteException, JvnException {

		for (int i = 0; i < locktable.size(); i++) {
			
				
			
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public HashMap<String, JvnObject> getTable() {
		return naming;
	}

	public void setTable(HashMap<String, JvnObject> table) {
		this.naming = table;
	}

	public HashMap<JvnCodeOS, JvnStatus> getLocktable() {
		return locktable;
	}

	public void setLocktable(HashMap<JvnCodeOS, JvnStatus> locktable) {
		this.locktable = locktable;
	}
	
	public static void main(String argv[]){
		  JvnCoordImpl jvnCoordImpl;
		try {
			jvnCoordImpl = new JvnCoordImpl();
	
			LocateRegistry.createRegistry(1099);
			String url="rmi://localhost:1099/Coordinator";
			Naming.rebind(url, jvnCoordImpl);
			System.out.println("Coordinator ready");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
