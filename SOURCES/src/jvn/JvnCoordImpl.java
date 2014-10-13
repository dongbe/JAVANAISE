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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.io.Serializable;

public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord {

	private HashMap<String, JvnJonObj> naming;
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

		naming = new HashMap<String, JvnJonObj>();
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

		naming.put(jon, (new JvnJonObj(jo.jvnGetObjectState(),jo)));
		JvnCodeOS code = new JvnCodeOS(jo.jvnGetObjectId(), js);
		JvnStatus status = new JvnStatus(jon, LockState.NL, js);
		locktable.put(code, status);
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
			jvnObject = naming.get(jon).getJs();
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

		// JvnCodeOS code = new JvnCodeOS(joi, js);
		String jon = null;
		JvnObject jvnObject = null;
		Serializable objet=null;
		
		for (Map.Entry<JvnCodeOS, JvnStatus> entry : locktable.entrySet()) {
			jon = entry.getValue().getJon();
			JvnCodeOS code = new JvnCodeOS(joi, js);
			JvnRemoteServer ts = entry.getKey().getJs();
			
			
			if (entry.getKey().getJoi()==joi && !entry.getKey().getJs().equals(js)) {
				
				if (entry.getValue().getState().equals(LockState.R)) {
					JvnStatus statut = new JvnStatus(jon, LockState.R, js);
					locktable.put(code, statut);
				} 
				else if (entry.getValue().getState().equals(LockState.W) || entry.getValue().getState().equals(LockState.RWC)) {	
					System.out.println("ici test 2 : " );
					objet=ts.jvnInvalidateWriter(joi);
					naming.get(jon).setObjet(objet);
					System.out.println(" objet recu par le coordinateur :"
							+ objet);
					locktable.put(code, new JvnStatus(jon, LockState.R, js));
				}

			}else if (entry.getKey().getJoi() == joi && entry.getKey().getJs().equals(js)){
				System.out.println("ici");
				if (entry.getValue().getState().equals(LockState.W)) {
					System.out.println("ici 2");
					objet=ts.jvnInvalidateWriterForReader(joi);
					System.out.println("ici 3 :"+objet);
					naming.get(jon).setObjet(objet);
					locktable.put(code, new JvnStatus(jon, LockState.RWC, js));
				}else{
					
					locktable.put(code, new JvnStatus(jon, LockState.R, js));
					//jvnObject=naming.get(jon);
				}				
			}
		}
		//naming.put(jon, jvnObject);

		return naming.get(jon).getObjet();
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
        ArrayList<JvnCodeOS> list = new ArrayList<JvnCodeOS>();
		String jon = null;
		//JvnObject jvnObject = null;
		JvnCodeOS code=null;
		Serializable objet=null;
		for (Map.Entry<JvnCodeOS, JvnStatus> entry : locktable.entrySet()) {
			System.out.println(entry.getKey().getJoi()+" : "+entry.getValue().getState());
		}
		for (Map.Entry<JvnCodeOS, JvnStatus> entry : locktable.entrySet()) {
			jon = entry.getValue().getJon();
			code = new JvnCodeOS(joi, js);
			JvnRemoteServer ts = entry.getKey().getJs();
			
			if (entry.getKey().getJoi() == joi) {
				
				if (entry.getValue().getState().equals(LockState.R)) {
					ts.jvnInvalidateReader(joi);
					list.add(code);
					//locktable.remove(code);
					//locktable.put(code, new JvnStatus(jon, LockState.W, js));
					//jvnObject=naming.get(jon);
				} 
				else if (entry.getValue().getState().equals(LockState.W) || entry.getValue().getState().equals(LockState.RWC)) {	
					System.out.println(" objet 1");
					objet =  ts.jvnInvalidateWriter(joi);
					System.out.println(" objet recu par le coordinateur :"
							+ objet);
					naming.get(jon).setObjet(objet);
					list.add(code);
					//locktable.remove(code);
					//locktable.put(code, new JvnStatus(jon, LockState.W, js));
				}else{
					System.out.println("Objet");
					//locktable.put(code, new JvnStatus(jon, LockState.W, js));
					//jvnObject=naming.get(jon);
				}

			}else {
				System.out.println("Objet non referencie dans la base retour NULL");
			}
		}
		for(int i=0;i<list.size();i++){
			locktable.remove(list.get(i));
		}
		locktable.put(code, new JvnStatus(jon, LockState.W, js));
		//naming.put(jon, jvnObject);
		System.out.println(" objet retourne par le coordinateur :"
				+ naming.get(jon).getObjet());
		return naming.get(jon).getObjet();
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
		int joi ;

		for (Map.Entry<JvnCodeOS, JvnStatus> entry : locktable.entrySet()) {
			
			if (entry.getKey().getJs().equals(js)) {
				joi= entry.getKey().getJoi();
				naming.remove(joi);
				locktable.remove(entry.getKey());	
				} }
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public HashMap<String, JvnJonObj> getTable() {
		return naming;
	}

	public void setTable(HashMap<String, JvnJonObj> table) {
		this.naming = table;
	}

	public HashMap<JvnCodeOS, JvnStatus> getLocktable() {
		return locktable;
	}

	public void setLocktable(HashMap<JvnCodeOS, JvnStatus> locktable) {
		this.locktable = locktable;
	}

	public static void main(String argv[]) {
		JvnCoordImpl jvnCoordImpl;
		try {
			jvnCoordImpl = new JvnCoordImpl();

			LocateRegistry.createRegistry(1099);
			String url = "rmi://localhost:1099/Coordinator";
			Naming.rebind(url, jvnCoordImpl);
			System.out.println("Coordinator ready");
			
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
