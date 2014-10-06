package jvn;

import java.io.Serializable;

public interface JvnSentenceItf extends Serializable{
	String 		data="";
 public void write(String s);
 public String read();
}
