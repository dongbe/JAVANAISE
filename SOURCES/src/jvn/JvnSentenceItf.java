package jvn;

import java.io.Serializable;

public interface JvnSentenceItf extends Serializable{

 public void write(String s) throws JvnException;
 public String read() throws JvnException;
}
