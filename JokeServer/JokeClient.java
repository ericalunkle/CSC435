/*--------------------------------------------------------

1. Name / Date: Erica Unkle/ 9-24-2017

2. Java version used, if not the official version for the class:

 java 1.8.0_144

3. Precise command-line compilation examples / instructions:

> javac JokeServer.java
> javac JokeClient.java
> javac JokeClientAdmin.java


4. Precise examples / instructions to run this program:

In separate shell windows run each of the following:

> java JokeServer
> java JokeClient
> java JokeClientAdmin

Joke Server will start and there is nothing you must do to keep this running.

Joke Client will have instructions displayed on what to do in the console.

JokeClientAdmin will have instructsion dispayed on what to ddo in the console


5. List of files needed for running the program.

 a. checklist.html
 b. JokeServer.java
 c. JokeClient.java
 d. JokeClientAdmin.java

5. Notes:

I have not implemented multiple servers.

----------------------------------------------------------*/


import java.util.*;
import java.io.*;
import java.net.*;


public class JokeClient 	{
	static String modeTracker;
	
	 public static String getModeTrackerState() 
	 	{return modeTracker;}
	 public static void setModeTrackerState (String state)
	 		{JokeClient.modeTracker = state;}
	 
public static void main(String args[]) {
	String serverName;
	JokeClient.setModeTrackerState("ffffffff");
	
	if(args.length < 1) {
		serverName = "localhost"; //if no server is entered, default to localhost
		System.out.println("Server One: " + serverName + ", Port:4545");
		
	}
	else serverName = args[0]; //else use what was entered
	
	System.out.println("Please enter your username");
	BufferedReader intoClient = new BufferedReader(new InputStreamReader(System.in)); //Created to get users input into the buffer reader
	
	try{
		String userName;
		userName = intoClient.readLine();//read the username from the client
		System.out.print("Press Enter for a joke or proverb:");
		String response; //answer to if they want a joke or proverb...should be enter
		do {
			System.out.flush();
			response = intoClient.readLine(); //read the input text from the client
			if (response.equals("")) {getConnection(userName, serverName, getModeTrackerState(), response);}; //if they did not input quit, then get the connection from the server
			
		} while (response.indexOf("quit") < 0);
		System.out.println("Cancelled by user request.");
	} catch (IOException x) {
		x.printStackTrace ();
	}
}



static void getConnection (String userName, String serverName, String modeTracker, String response) {
	Socket sock;
	BufferedReader fromServer;
	PrintStream toServer;
	String textFromServer;
	String textFromServer2;
	
	
	try {
		sock = new Socket(serverName, 4545); //Need to open the connection to ther server port
		System.out.println("Socket connecting");
		fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream())); //open an Input Stream to get in from server
		toServer = new PrintStream(sock.getOutputStream()); //open an Output Stream to send out to server
		
		//send username to the server, so they can keep track|
		toServer.println(response); //send over resposne\
		
		toServer.println(getModeTrackerState()); //send each part of the array one at a time
		
		toServer.println(userName);//send server username
		
		toServer.flush();
		

		//now you want to get the Text from the server and print it out on the client system
		
		textFromServer = fromServer.readLine(); //read in the joke from the server
		textFromServer2 = fromServer.readLine();

		if(textFromServer != null) System.out.println(textFromServer); //print the joke from the server
		setModeTrackerState(textFromServer2); //update state of mode tracker
		//System.out.println("The tracker recieved is: " + modeTracker);
		sock.close();
		
	} catch (IOException x) {
		System.out.println ("Socket error.");
		x.printStackTrace();
	}
}


}
