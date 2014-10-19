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
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;

import com.sun.org.apache.bcel.internal.generic.INSTANCEOF;

public class JvnServerImpl extends UnicastRemoteObject implements
		JvnLocalServer, JvnRemoteServer, Serializable {

	// A JVN server is managed as a singleton
	private static JvnServerImpl js = null;

	private JvnRemoteCoord jvnCoordImpl, jvnCoordImpl2;

	private HashMap<Integer, JvnObject> cacheObj;

	/**
	 * Default constructor
	 * 
	 * @throws JvnException
	 **/
	private JvnServerImpl() throws Exception {
		cacheObj = new HashMap<Integer, JvnObject>();
		jvnCoordImpl = (JvnRemoteCoord) Naming
				.lookup("rmi://localhost:1099/Coordinator");// init coordinator
			jvnCoordImpl2 = (JvnRemoteCoord) Naming
					.lookup("rmi://localhost:1099/slave");// init slave

		System.out.println("serveur ready :" + jvnCoordImpl);
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
				System.out.println("new serveur");
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
		JvnObject proxy = null;
		System.out.println("ok :"+o.getClass().getName());
		if (o.getClass().getName().equalsIgnoreCase("irc.sentence")) {
			System.out.println("test");
			proxy = new JvnObjectImpl(o);
		} else {

			InvocationHandler invocationHandler = new JvnInvocationHandler(o,
					true);
			ClassLoader loader = o.getClass().getClassLoader();
			Class<?>[] m = o.getClass().getInterfaces();
			proxy = (JvnObject) Proxy.newProxyInstance(loader, m,
					invocationHandler);
		}
		return proxy;

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
			String[] list = Naming.list("rmi://localhost:1099");
			System.out.println("list"+list.length);
			jvnCoordImpl.jvnRegisterObject(jon, jo, (JvnRemoteServer)js);
		} catch (RemoteException e) {
			try {
				jvnCoordImpl2.jvnRegisterObject(jon, jo, (JvnRemoteServer)js);
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
		} catch (MalformedURLException e) {
			
				e.printStackTrace();
			
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

			String[] list = Naming.list("rmi://localhost:1099");
			System.out.println("list"+list.length);
						
			jvnObject = jvnCoordImpl.jvnLookupObject(jon, (JvnRemoteServer)js);
			if(jvnObject!=null){
				cacheObj.put(jvnObject.jvnGetObjectId(), jvnObject);
			}

		} catch (RemoteException e) {
			try {
				jvnObject = jvnCoordImpl2.jvnLookupObject(jon, (JvnRemoteServer)js); // slave
			} catch (RemoteException e1) {
			
				e1.printStackTrace();
			}
		} catch (MalformedURLException e1) {
				
				e1.printStackTrace();
			
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

		Serializable stateObj = null;
		try {
			String[] list = Naming.list("rmi://localhost:1099");
			System.out.println("list"+list.length);
			stateObj = jvnCoordImpl.jvnLockRead(joi, (JvnRemoteServer)js);
		} catch (RemoteException e) {
			try {
				stateObj = jvnCoordImpl2.jvnLockRead(joi, (JvnRemoteServer)js);
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		
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

		Serializable stateObj = null;
		try {
			String[] list = Naming.list("rmi://localhost:1099");
			System.out.println("list"+list.length);
						
			stateObj = jvnCoordImpl.jvnLockWrite(joi, (JvnRemoteServer)js);
				

		} catch (RemoteException e) {
			try {
				stateObj = jvnCoordImpl2.jvnLockWrite(joi, (JvnRemoteServer)js); 
			} catch (RemoteException e1) {
				
				e1.printStackTrace();
			}
		
		} catch (MalformedURLException e) {
			try {
				stateObj = jvnCoordImpl2.jvnLockWrite(joi, (JvnRemoteServer)js);
			} catch (RemoteException e1) {
				
				e1.printStackTrace();
			}
		
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
		 JvnObject jo=cacheObj.get(joi);
		 jo.jvnInvalidateReader();
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
		
        JvnObject jo=cacheObj.get(joi);
		return jo.jvnInvalidateWriter();
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
		 JvnObject jo=cacheObj.get(joi);
			return jo.jvnInvalidateWriterForReader();
	};

	public JvnRemoteCoord getJvnCoordImpl() {
		try {
			String[] list = Naming.list("rmi://localhost:1099");
			System.out.println("list"+list.length);

		} catch (RemoteException e) {
			return jvnCoordImpl2; // retourner le slave s'il n'y pas de coordinateur 
		} catch (MalformedURLException e) {
			return jvnCoordImpl2;	
		}
	
		return jvnCoordImpl;
	}


	public HashMap<Integer, JvnObject> getCacheObj() {
		return cacheObj;
	}

	public void setCacheObj(HashMap<Integer, JvnObject> cacheObj) {
		this.cacheObj = cacheObj;
	}

}
