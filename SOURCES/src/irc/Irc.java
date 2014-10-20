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

import javax.swing.JFrame;


public class Irc {
	public TextArea		text;
	public TextField	data;
	JFrame 			frame;
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
			jo = js.jvnCreateObject(new Sentence());
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
		   System.out.println("IRC problem creation: " + e.getMessage());
		   e.printStackTrace();
	   }
	}

  /**
   * IRC Constructor
   @param jo the JVN object representing the Chat
   **/
	public Irc(JvnObject jo) {
		sentence = jo;
		frame=new JFrame();
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
		Button exit_button = new Button("exit");
		exit_button.addActionListener(new exitPListenerirc(this));
		frame.add(exit_button);
		frame.setSize(545,201);
		text.setBackground(Color.black); 
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
class exitPListenerirc implements ActionListener {
	Irc irc;
  
	public exitPListenerirc (Irc i) {
		irc = i;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			JvnServerImpl.jvnGetServer().jvnTerminate();
		} catch (JvnException e1) {
			
			e1.printStackTrace();
		}
		System.exit(0);
		
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
		System.out.println("Objet : "+irc.sentence.jvnGetObjectId());
		// invoke the method
		String s = ((Sentence)(irc.sentence.jvnGetObjectState())).read();
		
		// unlock the object
		irc.sentence.jvnUnLock();
		
		// display the read value
		irc.data.setText(s);
		irc.text.append(s+"\n");
	   } catch (JvnException je) {
		   System.out.println("IRC problem read : " + je.getMessage());
	   }
	}
}

 /**
  * Internal class to manage user events (write) on the CHAT application
  **/
 class writeListener  implements ActionListener {
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
    System.out.println("Objet: "+irc.sentence.jvnGetObjectId());
    // lock the object in write mode
		irc.sentence.jvnLockWrite();
		 
		// invoke the method
		
		((Sentence) irc.sentence.jvnGetObjectState()).write(s);
		
		
		// unlock the object
		irc.sentence.jvnUnLock();
	 } catch (JvnException je) {
		   System.out.println("IRC problem write : " + je.getMessage());
		   
		   je.printStackTrace();
	 }
	}
}



