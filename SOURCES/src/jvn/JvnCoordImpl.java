/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;


public class JvnCoordImpl 	
              extends UnicastRemoteObject 
							implements JvnRemoteCoord{
	
	private HashMap<String, JvnObject> naming;
	private Map <Integer, JvnStatus> locktable;
	private int id;
	

 /**
  * Default constructor
  * @throws JvnException
  **/
	public JvnCoordImpl() throws Exception {
		id=hashCode();
		
		naming = new HashMap<String, JvnObject>();
		locktable = new HashMap<Integer, JvnStatus>();
		System.out.println("id :"+id);
	}

  /**
  *  Allocate a NEW JVN object id (usually allocated to a 
  *  newly created JVN object)
  * @throws java.rmi.RemoteException,JvnException
  **/
  public int jvnGetObjectId()
  throws java.rmi.RemoteException,jvn.JvnException {
    return id;
  }
  
  /**
  * Associate a symbolic name with a JVN object
  * @param jon : the JVN object name
  * @param jo  : the JVN object 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the JVNServer
  * @throws java.rmi.RemoteException,JvnException
  **/
  public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
  throws java.rmi.RemoteException,jvn.JvnException{
	  /*System.out.println("yakoi");
	  naming.put(jon, jo);
	  JvnStatus jvnStatus= new JvnStatus(js, jo.jvnGetObjectState());
	  locktable.put(jo.jvnGetObjectId(), jvnStatus);*/
  }
  
  /**
  * Get the reference of a JVN object managed by a given JVN server 
  * @param jon : the JVN object name
  * @param js : the remote reference of the JVNServer
  * @throws java.rmi.RemoteException,JvnException
  **/
  public JvnObject jvnLookupObject(String jon, JvnRemoteServer js)
  throws java.rmi.RemoteException,jvn.JvnException{
	  JvnObject jvnObject=null;
      if(naming.containsKey(jon)){
      	jvnObject=naming.get(jon);
      }
		
		return jvnObject;
  }
  
  /**
  * Get a Read lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
   public Serializable jvnLockRead(int joi, JvnRemoteServer js)
   throws java.rmi.RemoteException, JvnException{
    // to be completed
    return null;
   }

  /**
  * Get a Write lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
   public Serializable jvnLockWrite(int joi, JvnRemoteServer js)
   throws java.rmi.RemoteException, JvnException{
    // to be completed
    return null;
   }

	/**
	* A JVN server terminates
	* @param js  : the remote reference of the server
	* @throws java.rmi.RemoteException, JvnException
	**/
    public void jvnTerminate(JvnRemoteServer js)
	 throws java.rmi.RemoteException, JvnException {
	 // to be completed
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
    
    public Map<Integer, JvnStatus> getLocktable() {
		return locktable;
	}

	public void setLocktable(Map<Integer, JvnStatus> locktable) {
		this.locktable = locktable;
	}

}

 
