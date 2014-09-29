package jvn;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class JvnInvocationHandler implements InvocationHandler , Serializable{

	private Object obj;
	public JvnInvocationHandler(Object obj){
		this.obj= obj;
	}


	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object result=null;	
		try {
			result = method.invoke(obj, args);
		} catch (Exception e) {

		}
		return result;
	}

}
