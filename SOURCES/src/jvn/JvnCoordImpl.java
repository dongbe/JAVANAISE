/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;

public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord {

	private HashMap<String, JvnJonObj> naming;
	private HashMap<JvnCodeOS, JvnStatus> locktable;
	JvnCoordImpl coordinateur2 = null ;
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
	public JvnCoordImpl(JvnCoordImpl coordinateur) throws Exception {
		id = hashCode();
		this.coordinateur2=coordinateur;
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
	public synchronized void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
			throws java.rmi.RemoteException, jvn.JvnException {

		naming.put(jon, (new JvnJonObj(jo.jvnGetObjectState(),jo)));
		JvnCodeOS code = new JvnCodeOS(jo.jvnGetObjectId(), js);
		JvnStatus status = new JvnStatus(jon, LockState.W, js);
		locktable.put(code, status);
		updateSndCord();
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
		ArrayList<JvnCodeOS> list = new ArrayList<JvnCodeOS>();
		if (naming.containsKey(jon)) {
			jvnObject = naming.get(jon).getJo();
			JvnCodeOS code = new JvnCodeOS(jvnObject.jvnGetObjectId(), js);
			for (Map.Entry<JvnCodeOS, JvnStatus> entry : locktable.entrySet()) {
				if(entry.getKey().getJoi()==jvnObject.jvnGetObjectId() 
						&& entry.getValue().getState().equals(LockState.W)){
					System.out.println("check");
					JvnRemoteServer ts = entry.getKey().getJs();
					System.out.println("check 1");
					Serializable objet=ts.jvnInvalidateWriter(jvnObject.jvnGetObjectId());
					System.out.println("check 2");
					naming.get(jon).setObjet(objet);
					list.add(entry.getKey());
				}
			}
			for(int i=0;i<list.size();i++){
				locktable.remove(list.get(i));
			}
			locktable.put(code, new JvnStatus(jon, LockState.W, js));
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
		//JvnObject jvnObject = null;
		 ArrayList<JvnCodeOS> list = new ArrayList<JvnCodeOS>();
		Serializable objet=null;
		JvnCodeOS code=new JvnCodeOS(joi, js);
		
		for (Map.Entry<JvnCodeOS, JvnStatus> entry : locktable.entrySet()) {
			jon = entry.getValue().getJon();
			JvnCodeOS key= entry.getKey();
			JvnRemoteServer ts = entry.getKey().getJs();
			
			
			if (entry.getKey().getJoi()==joi && !entry.getKey().getJs().equals(js)) {
				// si l'objet est detenu par un autre serveur en lecture
				if (entry.getValue().getState().equals(LockState.R)) {
					
				}
				// si l'objet est detenu par un autre serveur en ecriture
				else if (entry.getValue().getState().equals(LockState.W) || entry.getValue().getState().equals(LockState.RWC)) {	
					System.out.println("ici test 2 : " );
					objet=ts.jvnInvalidateWriter(joi);
					naming.get(jon).setObjet(objet);
					System.out.println(" objet recu par le coordinateur :"
							+ objet);
					//locktable.put(code, new JvnStatus(jon, LockState.R, js));
					list.add(key);
				}

			}else if (entry.getKey().getJoi() == joi && entry.getKey().getJs().equals(js)){
				//System.out.println("ici");
				if (entry.getValue().getState().equals(LockState.W)) {
					//System.out.println("ici 2");
					objet=ts.jvnInvalidateWriterForReader(joi);
					System.out.println("ici 3 :"+objet);
					naming.get(jon).setObjet(objet);
					list.add(key);
					//locktable.put(code, new JvnStatus(jon, LockState.RWC, js));
				}			
			}
		}
		for(int i=0;i<list.size();i++){
			locktable.remove(list.get(i));
		}
		locktable.put(code, new JvnStatus(jon, LockState.R, js));
		updateSndCord();
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
		JvnCodeOS code = new JvnCodeOS(joi, js);
		Serializable objet=null;
		
		
		for (Map.Entry<JvnCodeOS, JvnStatus> entry : locktable.entrySet()) {
			jon = entry.getValue().getJon();
			JvnCodeOS key = entry.getKey();
			JvnRemoteServer ts = entry.getKey().getJs();
			
			if (entry.getKey().getJoi() == joi) {
				
				if (entry.getValue().getState().equals(LockState.R)) {
					ts.jvnInvalidateReader(joi);
					list.add(key);
				} 
				else if (entry.getValue().getState().equals(LockState.W) || entry.getValue().getState().equals(LockState.RWC)) {	
					System.out.println(" objet 1");
					objet =  ts.jvnInvalidateWriter(joi);
					System.out.println(" objet recu par le coordinateur :"
							+ objet);
					naming.get(jon).setObjet(objet);
					list.add(key);
				}else{
					System.out.println("Objet");
				}

			}else {
				System.out.println("Objet non referencie dans la base retour NULL");
			}
		}
		
		for(int i=0;i<list.size();i++){
			locktable.remove(list.get(i));
		}
		
		locktable.put(code, new JvnStatus(jon, LockState.W, js));
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
				locktable.remove(entry.getKey());	// � verifier si cette instruction engendre une 
													// suppression multiple de serveur 
				}
			}
		updateSndCord();
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
	public void updateSndCord(){
		JvnRemoteCoord jvnCoordImpl;
		try {
			jvnCoordImpl = (JvnRemoteCoord) Naming.lookup("rmi://localhost:1099/Coordinator2");
			try {
				jvnCoordImpl.jvnUpdateCache(naming, locktable);
			} catch (JvnException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		
	}
	

	public HashMap<JvnCodeOS, JvnStatus> getLocktable() {
		return locktable;
	}

	public void setLocktable(HashMap<JvnCodeOS, JvnStatus> locktable) {
		this.locktable = locktable;
	}

	public static void main(String argv[]) {
		JvnCoordImpl jvnCoordImpl;
		String url;
		try {
			jvnCoordImpl = new JvnCoordImpl();
			LocateRegistry.createRegistry(1099);
			url = "rmi://localhost:1099/Coordinator";
			Naming.rebind(url, jvnCoordImpl);
			System.out.println("Coordinator ready");
			jvnCoordImpl = new JvnCoordImpl();
			url = "rmi://localhost:1099/Coordinator2";
			Naming.rebind(url, jvnCoordImpl);
			
		} catch (Exception e) {
				//cordinateur 2 
			try {
				
				
			} catch (Exception e1) {
			}

			
		
			System.out.println("Coordinator2 ready");
		}
	}
	public void jvnUpdateCache(HashMap<String, JvnJonObj> naming,
	HashMap<JvnCodeOS, JvnStatus> locktable) throws RemoteException, JvnException {
		this.locktable=locktable;
		this.naming=naming;
	
	}

}
