package jvn;

import irc.Sentence;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class JvnInvocationHandler implements InvocationHandler, Serializable {

	Class<Sentence> o = Sentence.class;
	Class<JvnObjectImpl> j = JvnObjectImpl.class;

	private Object obj;
	private boolean block;

	public JvnInvocationHandler(Object obj, boolean b) {
		this.obj = obj;
		this.block = b;
	}

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		Object result = null;
		try {

			if (method.getName().equals("write")) {

				// verrou en ecriture
				Method lockwrite = j.getDeclaredMethod("jvnLockWrite", null);
				Object oWrite = lockwrite.invoke(obj, null);

				System.out.println(" result:" + oWrite);

				// getObjectState
				Method ObjState = j
						.getDeclaredMethod("jvnGetObjectState", null);
				Object test = ObjState.invoke(obj, null);

				System.out.println(" result:" + test);

				// ecriture
				Method write = o.getDeclaredMethod("write",
						java.lang.String.class);
				Object e = write.invoke(test, args);

				System.out.println(" result:" + e);

				// verrou en unlock
				Method unlock = j.getDeclaredMethod("jvnUnLock", null);
				Object o = unlock.invoke(obj, null);

				result = "ecriture";

			} else if (method.getName().equalsIgnoreCase("read")) {

				// verrou en lecture
				Method lockread = j.getDeclaredMethod("jvnLockRead", null);
				Object oRead = lockread.invoke(obj, null);

				System.out.println(" result:" + oRead);

				// getObjectState
				Method ObjState = j
						.getDeclaredMethod("jvnGetObjectState", null);
				Object test = ObjState.invoke(obj, null);

				System.out.println(" result:" + test);

				// ecriture
				Method read = o.getDeclaredMethod("read", null);
				Object l = read.invoke(test, null);

				System.out.println(" result:" + l);

				// verrou en unlock
				Method unlock = j.getDeclaredMethod("jvnUnLock", null);
				Object o = unlock.invoke(obj, null);

				result = l;
			} else {
				result = method.invoke(obj, args);
			}
			System.out.println(" result final:" + result);
		} catch (Exception e) {

		}
		return result;
	}

}
