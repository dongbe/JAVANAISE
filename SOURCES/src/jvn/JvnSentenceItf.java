package jvn;

import java.io.Serializable;

public interface JvnSentenceItf extends Serializable{

 public void write(String s);
 public String read();
}
