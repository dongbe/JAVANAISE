/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import irc.Sentence;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import jvn.JvnCoordImpl.LockState;

public class JvnServerImpl extends UnicastRemoteObject implements
		JvnLocalServer, JvnRemoteServer, Serializable{

	// A JVN server is managed as a singleton
	private static JvnServerImpl js = null;

	private JvnRemoteCoord jvnCoordImpl, jvnCoordImpl2;
	
	private HashMap<Integer, Serializable> cacheObj;
	
	String jonS=null;

	/**
	 * Default constructor
	 * 
	 * @throws JvnException
	 **/
	private JvnServerImpl() throws Exception {
		cacheObj= new HashMap<Integer, Serializable>();
		System.out.println("toto 2");
		jvnCoordImpl = (JvnRemoteCoord) Naming.lookup("rmi://localhost:1099/Coordinator");
         if (jvnCoordImpl== null)
			 jvnCoordImpl2 = (JvnRemoteCoord) Naming.lookup("rmi://localhost:1099/Coordinator2");

		System.out.println("serveur ready :"+jvnCoordImpl);
	}

	/**
	 * Static method allowing an application to get a reference to a JVN server
	 * instance
	 * 
	 * @throws JvnException
	 **/
	public static JvnServerImpl jvnGetServer() {
		if (js == null) {
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
	 * 
	 * @throws JvnException
	 **/
	public void jvnTerminate() throws jvn.JvnException {
		// to be completed
	}

	/**
	 * creation of a JVN object
	 * 
	 * @param o
	 *            : the JVN object state
	 * @throws JvnException
	 **/
	public JvnObject jvnCreateObject(Serializable o) throws jvn.JvnException {
		JvnObjectImpl objet = new JvnObjectImpl(o);
		InvocationHandler invocationHandler = new JvnInvocationHandler(objet,true);
		//InvocationHandler invocationPHandler = new JvnProxyInvocationHandler(p);
		ClassLoader loader = objet.getClass().getClassLoader();
		Class<?>[] m = objet.getClass().getInterfaces();
		Object proxy = Proxy.newProxyInstance(loader, m, invocationHandler);
        
		return (JvnObject) proxy;

	}

	/**
	 * Associate a symbolic name with a JVN object
	 * 
	 * @param jon
	 *            : the JVN object name
	 * @param jo
	 *            : the JVN object
	 * @throws JvnException
	 **/
	public void jvnRegisterObject(String jon, JvnObject jo)
			throws jvn.JvnException {
		try {
			jonS = jon;

			if (jvnCoordImpl != null)
			jvnCoordImpl.jvnRegisterObject(jon, jo, (JvnRemoteServer) js);
			else 
				jvnCoordImpl2.jvnRegisterObject(jon, jo, (JvnRemoteServer) js);
			
		} catch (RemoteException e) {
			System.out.println("erreur lors de l'appel de la methode register"+e.getMessage());
		}
	}

	/**
	 * Provide the reference of a JVN object being given its symbolic name
	 * 
	 * @param jon
	 *            : the JVN object name
	 * @return the JVN object
	 * @throws JvnException
	 **/
	public JvnObject jvnLookupObject(String jon) throws jvn.JvnException {
		JvnObject jvnObject = null;
		try {
			if (jvnCoordImpl != null)
				jvnObject=jvnCoordImpl.jvnLookupObject(jon, this);
			else 
				jvnObject=jvnCoordImpl2.jvnLookupObject(jon, this);
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jvnObject;
	}

	/**
	 * Get a Read lock on a JVN object
	 * 
	 * @param joi
	 *            : the JVN object identification
	 * @return the current JVN object state
	 * @throws JvnException
	 **/
	public Serializable jvnLockRead(int joi) throws JvnException {
		
		Serializable stateObj=null;
		try {
	if (jvnCoordImpl != null)
				stateObj= jvnCoordImpl.jvnLockRead(joi, js);
			else 
				stateObj= jvnCoordImpl2.jvnLockRead(joi, js);
		
		} catch (RemoteException e) {
			System.out.println("erreur au niveau du lock read serveur : "+e.getMessage());
		}
		
		return stateObj;
	}

	/**
	 * Get a Write lock on a JVN object
	 * 
	 * @param joi
	 *            : the JVN object identification
	 * @return the current JVN object state
	 * @throws JvnException
	 **/
	public Serializable jvnLockWrite(int joi) throws JvnException {
		
		Serializable stateObj=null;
		try {
			
			if (jvnCoordImpl != null)
				stateObj= jvnCoordImpl.jvnLockWrite(joi, js);
			else 
				stateObj= jvnCoordImpl2.jvnLockWrite(joi, js);

		} catch (RemoteException e) {
			System.out.println("erreur au niveau du lock write serveur : "+e.getMessage());
		}
		
		return stateObj;
	}

	/**
	 * Invalidate the Read lock of the JVN object identified by id called by the
	 * JvnCoord
	 * 
	 * @param joi
	 *            : the JVN object id
	 * @return void
	 * @throws java.rmi.RemoteException
	 *             ,JvnException
	 **/
	public JvnObject getObject(int joi) {

		return null;
	}

	public void jvnInvalidateReader(int joi) throws java.rmi.RemoteException,
			jvn.JvnException {
		
		cacheObj.remove(joi);
	}

	/**
	 * Invalidate the Write lock of the JVN object identified by id
	 * 
	 * @param joi
	 *            : the JVN object id
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException
	 *             ,JvnException
	 **/
	public Serializable jvnInvalidateWriter(int joi)
			throws java.rmi.RemoteException, jvn.JvnException {

		return cacheObj.get(joi);
	}

	/**
	 * Reduce the Write lock of the JVN object identified by id
	 * 
	 * @param joi
	 *            : the JVN object id
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException
	 *             ,JvnException
	 **/
	public Serializable jvnInvalidateWriterForReader(int joi)
			throws java.rmi.RemoteException, jvn.JvnException {
		
		return cacheObj.get(joi);
	};

	public JvnRemoteCoord getJvnCoordImpl() {
		if (jvnCoordImpl != null)
			return jvnCoordImpl ;
		else 
			
		return jvnCoordImpl2;
	}

	public void setJvnCoordImpl(JvnCoordImpl jvnCoordImpl) {
		if (jvnCoordImpl != null)
			this.jvnCoordImpl = jvnCoordImpl;
			else 
				
		this.jvnCoordImpl2 = jvnCoordImpl;
	}

	public HashMap<Integer, Serializable> getCacheObj() {
		return cacheObj;
	}

	public void setCacheObj(HashMap<Integer, Serializable> cacheObj) {
		this.cacheObj = cacheObj;
	}

}
