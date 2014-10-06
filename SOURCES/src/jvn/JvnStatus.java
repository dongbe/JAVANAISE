package jvn;

import java.io.Serializable;

import jvn.JvnCoordImpl.LockState;


public class JvnStatus  implements Serializable{
	
	private String jon;
	private LockState state;
	
	public String getJon() {
		return jon;
	}

	public void setJon(String jon) {
		this.jon = jon;
	}

	public LockState getState() {
		return state;
	}

	public void setState(LockState state) {
		this.state = state;
	}

	public JvnStatus(String js, LockState st){
		this.jon=js;
		this.state=st;
	}


	

}
