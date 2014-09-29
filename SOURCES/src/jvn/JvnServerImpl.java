/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import irc.Sentence;

import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import jvn.JvnObjectImpl.lockState;



public class JvnServerImpl 	
              extends UnicastRemoteObject 
							implements JvnLocalServer, JvnRemoteServer{
	
  // A JVN server is managed as a singleton 
	private static JvnServerImpl js = null;
	
	private HashMap<String, JvnObject> table;

  public HashMap<String, JvnObject> getTable() {
		return table;
	}

	public void setTable(HashMap<String, JvnObject> table) {
		this.table = table;
	}

/**
  * Default constructor
  * @throws JvnException
  **/
	private JvnServerImpl() throws Exception {
		super();
		table = new HashMap<String, JvnObject>();
		// to be completed
	}
	
  /**
    * Static method allowing an application to get a reference to 
    * a JVN server instance
    * @throws JvnException
    **/
	public static JvnServerImpl jvnGetServer() {
		if (js == null){
			try {
				js = new JvnServerImpl();
			} catch (Exception e) {
				return null;
			}
		}
		return js;
	}
	
	/**
	* The JVN service is not used anymore
	* @throws JvnException
	**/
	public  void jvnTerminate()
	throws jvn.JvnException {
    // to be completed 
	} 
	
	/**
	* creation of a JVN object
	* @param o : the JVN object state
	* @throws JvnException
	**/
	public  JvnObject jvnCreateObject(Serializable o)
	throws jvn.JvnException { 
		// to be completed 
		/*
		 *
		 * //initialize velocity engine
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		ve.init();
		Template t = ve.getTemplate("sentence.vm");
		// create a context & add data
		VelocityContext context = new VelocityContext();
		context.put("className", "Sentence");
		// perform generation
		StringWriter writer = new StringWriter();
		t.merge(context, writer);
		System.out.println(writer.toString());
		
		 */
		InvocationHandler invocationHandler = new JvnInvocationHandler(o);
		ClassLoader loader=o.getClass().getClassLoader();
		Class<?>[] m=o.getClass().getInterfaces();
		JvnObject proxy =  (JvnObject) Proxy.newProxyInstance(loader, m, invocationHandler);
		
		return proxy; 
		
	}
	
	/**
	*  Associate a symbolic name with a JVN object
	* @param jon : the JVN object name
	* @param jo : the JVN object 
	* @throws JvnException
	**/
	public  void jvnRegisterObject(String jon, JvnObject jo)
	throws jvn.JvnException {
		table.put(jon, jo);
	}
	
	/**
	* Provide the reference of a JVN object being given its symbolic name
	* @param jon : the JVN object name
	* @return the JVN object 
	* @throws JvnException
	**/
	public  JvnObject jvnLookupObject(String jon)
	throws jvn.JvnException {
		JvnObject jvnObject=null;
        if(table.containsKey(jon)){
        	jvnObject=table.get(jon);
        }
		
		return jvnObject;
	}	
	
	/**
	* Get a Read lock on a JVN object 
	* @param joi : the JVN object identification
	* @return the current JVN object state
	* @throws  JvnException
	**/
   public Serializable jvnLockRead(int joi)
	 throws JvnException {
	   JvnObject jvnObj = getObject(joi);
		  
		  if (jvnObj.jvnGetObjectState().equals(lockState.NL) || jvnObj.jvnGetObjectState().equals(lockState.RC)  || jvnObj.jvnGetObjectState().equals(lockState.WC) ||  jvnObj.jvnGetObjectState().equals(lockState.RWC)  )
				jvnObj.jvnLockRead();
			else
				try {
					wait();
				} catch (InterruptedException e) {
					jvnObj.jvnLockRead();
					notify();
				}	
		  return jvnObj;
	}	
	/**
	* Get a Write lock on a JVN object 
	* @param joi : the JVN object identification
	* @return the current JVN object state
	* @throws  JvnException
	**/
   public Serializable jvnLockWrite(int joi)
	 throws JvnException {
	   JvnObject jvnObj = getObject(joi);
		  
		  if (jvnObj.jvnGetObjectState().equals(lockState.NL) || jvnObj.jvnGetObjectState().equals(lockState.RC)  || jvnObj.jvnGetObjectState().equals(lockState.WC) ||  jvnObj.jvnGetObjectState().equals(lockState.RWC)  )
				jvnObj.jvnLockWrite();
			else
				try {
					wait();
				} catch (InterruptedException e) {
					jvnObj.jvnLockWrite();
					notify();
				}
		return jvnObj;		}	

	
  /**
	* Invalidate the Read lock of the JVN object identified by id 
	* called by the JvnCoord
	* @param joi : the JVN object id
	* @return void
	* @throws java.rmi.RemoteException,JvnException
	**/
  public JvnObject getObject (int joi){
	  
	  return null; }
   
  public void jvnInvalidateReader(int joi)
	throws java.rmi.RemoteException,jvn.JvnException {
	  JvnObject jvnObj = getObject(joi);
		if (jvnObj.jvnGetObjectState().equals(lockState.R))
			try {
				wait();
			} catch (InterruptedException e) {
				jvnObj.jvnUnLock();
				notify();
			}

} 
	  

	 

	    
	/**
	* Invalidate the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
  public Serializable jvnInvalidateWriter(int joi)
	throws java.rmi.RemoteException,jvn.JvnException { 
	  JvnObject jvnObj = getObject(joi);
			if (jvnObj.jvnGetObjectState().equals(lockState.W))
				try {
					wait();
				} catch (InterruptedException e) {
					jvnObj.jvnUnLock();
					notify();
				}
		return jvnObj;
	}
	
	/**
	* Reduce the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
   public Serializable jvnInvalidateWriterForReader(int joi)
	 throws java.rmi.RemoteException,jvn.JvnException { 
	
	   JvnObject jvnObj = getObject(joi);
		if (jvnObj.jvnGetObjectState().equals(lockState.W))
			try {
				wait();
			} catch (InterruptedException e) {
				jvnObj.jvnLockRead();
				notify();
			}
	return jvnObj;
	 };

}

 
