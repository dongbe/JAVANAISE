package jvn;

import irc.Sentence;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class JvnInvocationHandler implements InvocationHandler , Serializable{

	Class<Sentence> o = Sentence.class;
	Class<JvnObjectImpl> j = JvnObjectImpl.class;
	
	private Object obj;
	private boolean block;
	public JvnInvocationHandler(Object obj, boolean b){
		this.obj= obj;
		this.block = b;
	}


	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object result=null;	
		try {
			
				
				result = method.invoke(obj, args);
			
			//System.out.println(" result:" +result);
		} catch (Exception e) {

		}
		return result;
	}

}
