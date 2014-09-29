/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import jvn.JvnObjectImpl.lockState;

public class JvnServerImpl extends UnicastRemoteObject implements
		JvnLocalServer, JvnRemoteServer {

	// A JVN server is managed as a singleton
	private static JvnServerImpl js = null;

	private JvnCoordImpl jvnCoordImpl;

	/**
	 * Default constructor
	 * 
	 * @throws JvnException
	 **/
	private JvnServerImpl() throws Exception {
		super();
		Registry register = LocateRegistry.getRegistry("/localhost/irc/");
		jvnCoordImpl = (JvnCoordImpl) register.lookup("coordinator");
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
		JvnObject proxy = (JvnObject) Proxy.newProxyInstance(loader, m,
				invocationHandler);

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
			jvnCoordImpl.jvnRegisterObject(jon, jo, this);
		} catch (RemoteException e) {
			System.out.println("erreur lors de l'appel de la methode register");
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
		JvnObject jvnObj = getObject(joi);

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
			}
		return jvnObj;
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
		JvnObject jvnObj = getObject(joi);

		if (jvnObj.jvnGetObjectState().equals(lockState.NL)
				|| jvnObj.jvnGetObjectState().equals(lockState.RC)
				|| jvnObj.jvnGetObjectState().equals(lockState.WC)
				|| jvnObj.jvnGetObjectState().equals(lockState.RWC))
			jvnObj.jvnLockWrite();
		else
			try {
				wait();
			} catch (InterruptedException e) {
				jvnObj.jvnLockWrite();
				notify();
			}
		return jvnObj;
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

		return null;//jvnCoordImpl.getLocktable().get(joi);
	}

	public void jvnInvalidateReader(int joi) throws java.rmi.RemoteException,
			jvn.JvnException {
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
	 * 
	 * @param joi
	 *            : the JVN object id
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException
	 *             ,JvnException
	 **/
	public Serializable jvnInvalidateWriterForReader(int joi)
			throws java.rmi.RemoteException, jvn.JvnException {

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

	public JvnCoordImpl getJvnCoordImpl() {
		return jvnCoordImpl;
	}

	public void setJvnCoordImpl(JvnCoordImpl jvnCoordImpl) {
		this.jvnCoordImpl = jvnCoordImpl;
	}

}
