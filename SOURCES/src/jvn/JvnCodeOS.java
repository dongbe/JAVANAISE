package jvn;

import java.io.Serializable;

public class JvnCodeOS implements Serializable {

	private int joi;

	private JvnRemoteServer js;

	public JvnCodeOS(int joid, JvnRemoteServer jserv) {
		joi = joid;
		js = jserv;
	}

	public int getJoi() {
		return joi;
	}

	public JvnRemoteServer getJs() {
		return js;
	}

	@Override
	public int hashCode() {
		return joi+js.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)return true;
        if (obj == null)return false;
        if (!(obj instanceof JvnCodeOS)) return false;
        JvnCodeOS key = (JvnCodeOS) obj;
        return joi == key.getJoi() && js.equals(key.getJs());
        
	}
}
