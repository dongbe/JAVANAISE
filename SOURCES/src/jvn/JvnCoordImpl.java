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
	private HashMap<Integer, JvnStatus> locktable;

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
		locktable = new HashMap<Integer, JvnStatus>();
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
		//JvnCodeOS code = new JvnCodeOS(, js);
		JvnStatus status = new JvnStatus(jon, LockState.NL);
		locktable.put(jo.jvnGetObjectId(),status);
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
			System.out.println(" lookup " + jvnObject.jvnGetObjectId());
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
		
		//JvnCodeOS code = new JvnCodeOS(joi, js);
		//JvnObject objet = null;
		/*
		
		if(locktable.get(code).getState().equals(LockState.W) || locktable.get(code).getState().equals(LockState.WC)){
		     objet=(JvnObject) js.jvnInvalidateWriterForReader(joi);
		     naming.put(jon, objet);
		}else{
			locktable.get(code).setState(LockState.R);
		}
		
		System.out.println("table des apres etats : "+locktable.get(code).getState());*/
		
		locktable.get(joi).setState(LockState.R);
		String jon= locktable.get(joi).getJon();
		return naming.get(jon);
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
		
		//JvnCodeOS code = new JvnCodeOS(joi, js);
		//JvnObject objet = null;
		locktable.get(joi).setState(LockState.W);
		String jon= locktable.get(joi).getJon();
		return naming.get(jon);
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

	public HashMap<Integer, JvnStatus> getLocktable() {
		return locktable;
	}

	public void setLocktable(HashMap<Integer, JvnStatus> locktable) {
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
