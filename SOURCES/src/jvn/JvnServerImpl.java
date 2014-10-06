/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import jvn.JvnCoordImpl.LockState;

public class JvnServerImpl extends UnicastRemoteObject implements
		JvnLocalServer, JvnRemoteServer {

	// A JVN server is managed as a singleton
	private static JvnServerImpl js = null;

	private JvnRemoteCoord jvnCoordImpl;

	/**
	 * Default constructor
	 * 
	 * @throws JvnException
	 **/
	private JvnServerImpl() throws Exception {
		System.out.println("toto 2");
		jvnCoordImpl = (JvnRemoteCoord) Naming.lookup("rmi://localhost:1099/Coordinator");
		System.out.println("toto 3"+jvnCoordImpl);
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
		
		InvocationHandler invocationHandler = new JvnInvocationHandler(o);
		ClassLoader loader = o.getClass().getClassLoader();
		Class<?>[] m = o.getClass().getInterfaces();
		System.out.println("ici");
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
			jvnCoordImpl.jvnRegisterObject(jon, jo, (JvnRemoteServer) js);
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
			jvnCoordImpl.jvnLookupObject(jon, this);
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
		/*JvnObject jvnObj = getObject(joi);

		if (jvnObj.jvnGetObjectState().equals(lockState.NL)
				|| jvnObj.jvnGetObjectState().equals(lockState.RC)
				|| jvnObj.jvnGetObjectState().equals(lockState.WC)
				|| jvnObj.jvnGetObjectState().equals(lockState.RWC))
			jvnObj.jvnLockRead();
		else
			try {
				wait();
			} catch (InterruptedException e) {
				jvnObj.jvnLockRead();
				notify();
			}*/
		LockState state=null;
		try {
			state = (LockState) jvnCoordImpl.jvnLockRead(joi, js);
		} catch (RemoteException e) {
			System.out.println("erreur au niveau du lock read serveur : "+e.getMessage());
		}
		return state;
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
		
		LockState state=null;
		try {
			state = (LockState) jvnCoordImpl.jvnLockWrite(joi, js);
		} catch (RemoteException e) {
			System.out.println("erreur au niveau du lock write serveur : "+e.getMessage());
		}
		return state;
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

		String obj=((JvnCoordImpl) jvnCoordImpl).getLocktable().get(new JvnCodeOS(joi, (JvnRemoteServer)this)).getJon();
		JvnObject object = null;
		try {
			object = jvnLookupObject(obj);
		} catch (JvnException e) {
			System.out.println("get object erreur"+e.getMessage());
		}
		return object;
	}

	public void jvnInvalidateReader(int joi) throws java.rmi.RemoteException,
			jvn.JvnException {
		JvnObject jvnObj = getObject(joi);
		jvnObj.jvnInvalidateReader();
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
		JvnObject jvnObj = getObject(joi);
		jvnObj=(JvnObject) jvnObj.jvnInvalidateWriter();
		return jvnObj;
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

		
		return null;
	};

	public JvnRemoteCoord getJvnCoordImpl() {
		return jvnCoordImpl;
	}

	public void setJvnCoordImpl(JvnCoordImpl jvnCoordImpl) {
		this.jvnCoordImpl = jvnCoordImpl;
	}

}
