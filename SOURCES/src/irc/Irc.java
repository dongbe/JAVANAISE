/***
 * Irc class : simple implementation of a chat using JAVANAISE
 * Contact: 
 *
 * Authors: 
 */

package irc;

import java.awt.*;
import java.awt.event.*;

import jvn.*;

import java.io.*;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class Irc {
	public TextArea		text;
	public TextField	data;
	Frame 			frame;
	JvnObject       sentence;
	


  /**
  * main method
  * create a JVN object named IRC for representing the Chat application
  **/
	public static void main(String argv[]) {
			   
	
		try {
			
			
			
		// initialize JVN
		JvnServerImpl js = JvnServerImpl.jvnGetServer();
		//Sentence s = new Sentence();
		
		// look up the IRC object in the JVN server
		// if not found, create it, and register it in the JVN server
		System.out.println(" jo : ");
		JvnObject jo = js.jvnLookupObject("IRC");
		
		if (jo == null) {
			jo = js.jvnCreateObject((Serializable)new Sentence());
			// after creation, I have a write lock on the object
			jo.jvnUnLock();
			js.jvnRegisterObject("IRC", jo);
			
			
		}
		System.out.println(" jo : "+jo.jvnGetObjectId());
		// create the graphical part of the Chat application
		 new Irc(jo);
		// create the graphical part of the Chat application
		// new Irc(jo);
			  
	   } 
		catch (Exception e) {
		   System.out.println("IRC problem : " + e.getMessage());
	   }
	}

  /**
   * IRC Constructor
   @param jo the JVN object representing the Chat
   **/
	public Irc(JvnObject jo) {
		sentence = jo;
		frame=new Frame();
		frame.setLayout(new GridLayout(1,1));
		text=new TextArea(10,60);
		text.setEditable(false);
		text.setForeground(Color.red);
		frame.add(text);
		data=new TextField(40);
		frame.add(data);
		Button read_button = new Button("read");
		read_button.addActionListener(new readListener(this));
		frame.add(read_button);
		Button write_button = new Button("write");
		write_button.addActionListener(new writeListener(this));
		frame.add(write_button);
		frame.setSize(545,201);
		text.setBackground(Color.black); 
		frame.setVisible(true);
	}
}


 /**
  * Internal class to manage user events (read) on the CHAT application
  **/
 class readListener implements ActionListener {
	Irc irc;
  
	public readListener (Irc i) {
		irc = i;
	}
   
 /**
  * Management of user events
  **/
	public void actionPerformed (ActionEvent e) {
	 try {
		// lock the object in read mode
		irc.sentence.jvnLockRead();
		System.out.println("Object en mode lecture normalement: "+irc.sentence.jvnGetState());
		// invoke the method
		String s = ((JvnSentenceItf)(irc.sentence.jvnGetObjectState())).read();
		
		// unlock the object
		irc.sentence.jvnUnLock();
		
		// display the read value
		irc.data.setText(s);
		irc.text.append(s+"\n");
	   } catch (JvnException je) {
		   System.out.println("IRC problem : " + je.getMessage());
	   }
	}
}

 /**
  * Internal class to manage user events (write) on the CHAT application
  **/
 class writeListener implements ActionListener {
	Irc irc;
  
	public writeListener (Irc i) {
        	irc = i;
	}
  
  /**
    * Management of user events
   **/
	public void actionPerformed (ActionEvent e) {
	   try {	
		// get the value to be written from the buffer
    String s = irc.data.getText();
    System.out.println("Object en mode ecriture normalement: "+irc.sentence.jvnGetState());
    // lock the object in write mode
		irc.sentence.jvnLockWrite();
		 
		// invoke the method
		
		((JvnSentenceItf) irc.sentence.jvnGetObjectState()).write(s);
		
		
		// unlock the object
		irc.sentence.jvnUnLock();
	 } catch (JvnException je) {
		   System.out.println("IRC problem  : " + je.getMessage());
	 }
	}
}



