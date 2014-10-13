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

	private HashMap<String, JvnObject> naming;
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

		naming = new HashMap<String, JvnObject>();
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
	public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
			throws java.rmi.RemoteException, jvn.JvnException {

		naming.put(jon, jo);
		JvnCodeOS code = new JvnCodeOS(jo.jvnGetObjectId(), js);
		JvnStatus status = new JvnStatus(jon, LockState.NL, js);
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

		// JvnCodeOS code = new JvnCodeOS(joi, js);
		String jon = null;
		JvnObject jvnObject = null;

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
					jvnObject = (JvnObject) ts.jvnInvalidateWriter(joi);
					naming.put(jon, jvnObject);
					System.out.println(" objet recu par le coordinateur :"
							+ jvnObject.jvnGetObjectId());
					locktable.put(code, new JvnStatus(jon, LockState.R, js));
				}

			}else if (entry.getKey().getJoi() == joi && entry.getKey().getJs().equals(js)){
				
				if (entry.getValue().getState().equals(LockState.W)) {
					jvnObject=(JvnObject)ts.jvnInvalidateWriterForReader(joi);
					naming.put(jon, jvnObject);
					locktable.put(code, new JvnStatus(jon, LockState.RWC, js));
				}else{
					
					locktable.put(code, new JvnStatus(jon, LockState.R, js));
					//jvnObject=naming.get(jon);
				}				
			}
		}
		//naming.put(jon, jvnObject);
		updateSndCord();
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
        ArrayList<JvnCodeOS> list = new ArrayList<JvnCodeOS>();
		String jon = null;
		JvnObject jvnObject = null;
		JvnCodeOS code=null;
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
					jvnObject = (JvnObject) ts.jvnInvalidateWriter(joi);
					System.out.println(" objet recu par le coordinateur :"
							+ jvnObject.jvnGetObjectId());
					naming.put(jon, jvnObject);
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
				+ naming.get(jon).jvnGetObjectId());
		updateSndCord();
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
		int joi ;
		for (Map.Entry<JvnCodeOS, JvnStatus> entry : locktable.entrySet()) {		
			if (entry.getKey().getJs().equals(js)) {
				joi= entry.getKey().getJoi();
				naming.remove(joi);
				locktable.remove(entry.getKey());	// à verifier si cette instruction engendre une 
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

	public HashMap<String, JvnObject> getTable() {
		return naming;
	}

	public void setTable(HashMap<String, JvnObject> table) {
		this.naming = table;
	}
	public void updateSndCord(){
		if(this.coordinateur2!=null){
		this.coordinateur2.locktable=this.locktable;
		this.coordinateur2.naming= this.naming;}
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
			
			//cordinateur 2 
			
			jvnCoordImpl = new JvnCoordImpl();

			
			url = "rmi://localhost:1099/Coordinator2";
			Naming.rebind(url, jvnCoordImpl);
			System.out.println("Coordinator2 ready");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
