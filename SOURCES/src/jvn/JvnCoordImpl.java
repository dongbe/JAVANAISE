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
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;

public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord {

	private HashMap<String, JvnJonObj> naming;
	private HashMap<JvnCodeOS, JvnStatus> locktable;
	JvnRemoteCoord coordinateur2 = null ;
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
		JvnStatus status = new JvnStatus(jon, LockState.W, js);
		locktable.put(code, status);
		updateSlave();
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
		updateSlave();
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
		//JvnObject jvnObject = naming.get(locktable.get(code).getJon()).getJs();
		Serializable objet=null;
		for (Map.Entry<JvnCodeOS, JvnStatus> entry : locktable.entrySet()) {
			System.out.println(entry.getKey().getJoi()+" : "+entry.getValue().getState());
		}
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
		for (Map.Entry<JvnCodeOS, JvnStatus> entry : locktable.entrySet()) {
			System.out.println(entry.getKey().getJoi()+" : "+entry.getValue().getState());
		}
		for(int i=0;i<list.size();i++){
			locktable.remove(list.get(i));
		}
		
		locktable.put(code, new JvnStatus(jon, LockState.W, js));
		System.out.println(" objet retourne par le coordinateur :"
				+ naming.get(jon).getObjet());
		for (Map.Entry<JvnCodeOS, JvnStatus> entry : locktable.entrySet()) {
			System.out.println(entry.getKey().getJoi()+" : "+entry.getValue().getState());
		}
		updateSlave();
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
				locktable.remove(entry.getKey());	// ï¿½ verifier si cette instruction engendre une 
													// suppression multiple de serveur 
				}
			}
		updateSlave();
	}

	public HashMap<String, JvnJonObj> getNaming(){return this.naming;}
	public HashMap<JvnCodeOS, JvnStatus> getLocktableser(){return this.locktable;}
	public void addSlave(JvnRemoteCoord coordinateur_P ){	
		coordinateur2 = coordinateur_P;
		updateSlave();
	}
	

	public  void updateSlave(){
		JvnRemoteCoord jvnCoordImpl;
		try {
			 String[] list = Naming.list("rmi://localhost:1099"); 
			if (list.length == 2){ // tester s'il y a un coordinateur et un slave dans le registry
				jvnCoordImpl = (JvnRemoteCoord) Naming.lookup("rmi://localhost:1099/slave");
				try {
					jvnCoordImpl.jvnUpdateCache(naming, locktable); // mettre à jour les données du slave 
				} catch (JvnException e) {
				e.printStackTrace();}
			}
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			System.out.println("No slave launched !");
			
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		
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
		String url;
		
		try {
			LocateRegistry.createRegistry(1099);
			jvnCoordImpl = new JvnCoordImpl();
			url = "rmi://localhost:1099/Coordinator"; 
			Naming.rebind(url, jvnCoordImpl);
			System.out.println("Coordinator ready");
			
		} catch (ExportException e0) {
				//cordinateur 2 
			try { // si le coordinateur est déjà créé 
				JvnRemoteCoord jvnCoord = (JvnRemoteCoord) Naming.lookup("rmi://localhost:1099/Coordinator");
				String[] list = Naming.list("rmi://localhost:1099");
				System.out.println("list"+list.length);
					if (list.length== 1){
						jvnCoordImpl = new JvnCoordImpl();
						url = "rmi://localhost:1099/slave";
						Naming.bind(url, jvnCoordImpl);
						jvnCoord.addSlave((JvnRemoteCoord)jvnCoordImpl);
						jvnCoordImpl.updateSlave();// mettre à jour les données du slave
						System.out.println("Slave ready");
				} else 
						System.out.println("Slave already exists !");}
				catch(Exception e1){
					e1.printStackTrace();
				}
			
			
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//Mettre à jour la cache du slave
	public void jvnUpdateCache(HashMap<String, JvnJonObj> naming,
	HashMap<JvnCodeOS, JvnStatus> locktable) throws RemoteException, JvnException {
		this.locktable=locktable;
		this.naming=naming;
	
	}

}
