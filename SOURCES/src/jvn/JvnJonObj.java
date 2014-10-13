package jvn;

import java.io.Serializable;

public class JvnJonObj implements Serializable {

	private Serializable objet;

	private JvnObject js;

	public JvnJonObj(Serializable objet, JvnObject jserv) {
		this.objet = objet;
		js = jserv;
	}

	public Serializable getObjet() {
		return objet;
	}
	public void setObjet(Serializable objet) {
		this.objet=objet;
	}

	public JvnObject getJs() {
		return js;
	}

	@Override
	public int hashCode() {
		return objet.hashCode()+js.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)return true;
        if (obj == null)return false;
        if (!(obj instanceof JvnJonObj)) return false;
        JvnJonObj key = (JvnJonObj) obj;
        return objet == key.getObjet() && js.equals(key.getJs());
        
	}
}
