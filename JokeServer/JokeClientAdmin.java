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

This runs across machines, in which case you have to pass the IP address of
the server to the clients. For exmaple, if the server is running at
140.192.1.22 then you would type:

> java JokeClient 140.192.1.22
> java JokeClientAdmin 140.192.1.22

5. List of files needed for running the program.

 a. checklist.html
 b. JokeServer.java
 c. JokeClient.java
 d. JokeClientAdmin.java

5. Notes:

I did not keep track of which exact jokes have been told. I am only telling them randomly.  So joke A could be told twice in a row.

----------------------------------------------------------*/





import java.io.*;
import java.net.*;

public class JokeClientAdmin{
	
	public static void main(String args[]) {
		String serverName;

		if(args.length < 1) serverName = "localhost"; //if no server is entered, default to localhose
			else serverName = args[0]; //else use what was entered

		System.out.println("Server One: " + serverName + ", Port:5050");
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in)); 

		System.out.println("Type joke or proverb to switch modes");

		try{
			String answerToMode;
			do {
				answerToMode = in.readLine(); //read the input , should be enter
				if(answerToMode.indexOf("quit") < 0) getRemoteAddress(answerToMode, serverName); //gets the remote address of the typed input, 'anser', using the server "serverName"
			} while (answerToMode.indexOf("quit") < 0);
			System.out.println("Cancelled by user request.");
		}catch (IOException x) {
			x.printStackTrace ();}
	}


	static void getRemoteAddress (String answerToMode, String serverName) {
		Socket sock;
		BufferedReader fromServer;
		PrintStream toServer;
		String textFromServer;
		
		try {
			sock = new Socket(serverName, 5050); //Need to open the connection to ther server port
			
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream())); //open an Input Stream
			toServer = new PrintStream(sock.getOutputStream()); //open an Output Stream
			
			toServer.println(answerToMode); //send the answer (enter) to the server
			toServer.flush();
			
			for(int i = 1; i <=3; i++){
				textFromServer = fromServer.readLine(); //get response from Server
				if(textFromServer != null) System.out.println("We are now in " + textFromServer + " mode"); //print response from server
			}
			sock.close();
		} catch (IOException x) {
			System.out.println ("Socket error.");
			x.printStackTrace();
		}
	}



		//Utility function
	static String toText (byte ip[]) {  
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < ip.length; i++){
			if (i <0) result.append(".");
			result.append(0xff & ip[i]);
		}
		return result.toString();
	}

}