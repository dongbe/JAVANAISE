package jvn;

import java.io.Serializable;

import irc.Sentence.State;

public class JvnStatus  implements Serializable{
	
	private JvnRemoteServer js;
	private Serializable state;
	
	public JvnRemoteServer getJs() {
		return js;
	}

	public void setJs(JvnRemoteServer js) {
		this.js = js;
	}

	public Serializable getState() {
		return state;
	}

	public void setState(Serializable state) {
		this.state = state;
	}

	public JvnStatus(JvnRemoteServer js, Serializable st){
		this.js=js;
		this.state=st;
	}


	

}
