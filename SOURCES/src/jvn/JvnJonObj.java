package jvn;

import java.io.Serializable;

public class JvnJonObj implements Serializable {

	private Serializable objet;

	private JvnObject jo;

	public JvnJonObj(Serializable objet, JvnObject jobjet) {
		this.objet = objet;
		jo = jobjet;
	}

	public Serializable getObjet() {
		return objet;
	}
	public void setObjet(Serializable objet) {
		this.objet=objet;
	}

	public JvnObject getJo() {
		return jo;
	}

	@Override
	public int hashCode() {
		return objet.hashCode()+jo.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)return true;
        if (obj == null)return false;
        if (!(obj instanceof JvnJonObj)) return false;
        JvnJonObj key = (JvnJonObj) obj;
        return objet == key.getObjet() && jo.equals(key.getJo());
        
	}
}
