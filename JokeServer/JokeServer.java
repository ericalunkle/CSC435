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

I have not implemented multiple servers

----------------------------------------------------------*/


import java.io.*; //Import necessary libraries
import java.net.*; 
import java.util.*;

public class JokeServer {

	static boolean jokeMode = true;  //joke mode is true initially
	static boolean proverbMode = false; //proverb mode is false initially
	public static boolean controlSwitch = true;

	public static void main(String args[]) throws IOException {
		int queue_len = 6;
		int clientport1 = 0;
		int clientport2 = 0;
		
		if(args.length < 1) {
			clientport1 = 4545; //first client port
			clientport2 = 0;//if no server is entered, default to localhose
			System.out.println("Erica Unkle's Joke primary server starting up, listening at port 4545.\n");
		}
		else if (args[0] == "secondary") {
			clientport2 = 4646; //second client port
			clientport1 = 4545;//first client port always exists
			System.out.println("Erica Unkle's Joke secondary server starting up, listening at port 4546.\n");
		}
	
		Socket sockToClient1; //create a new socket to connect to client;
		Socket sockToClient2; //create a second client to connect to
		
		AdminLooper AL = new AdminLooper();
		Thread t = new Thread(AL); //create a thread for admin to start
		t.start(); //starts the admin thread at time of connection. Now it is waiting for admin input
		
		ServerSocket servsock = new ServerSocket(clientport1, queue_len);
		ServerSocket servsock2 = new ServerSocket(clientport2, queue_len);
		
		while (controlSwitch){ //server is always open
			
			sockToClient1 = servsock.accept(); //server is wating for a new client connection
			new Worker(sockToClient1).start(); //Worker thread is created to handle the new client connection
			if(clientport2 == 4546) { //if there is a second port then create a new connection to the client on port 4546
				sockToClient2 = servsock2.accept();
				new Worker(sockToClient2).start();
			}
		}
	}
}


class Worker extends Thread { //worker thread handles what to do when a client is connected to the server
	String jokeAndProverbState ; //need to keep track of the mode or Joke State
	Socket socket;	//Create a Socket variable sock
	
	Worker (Socket s1) 
		{socket =  s1;} //Create a contructor for Worker
	
	public void run() { //must have run program to start thread
		PrintStream outToClient = null;
		BufferedReader inFromClient = null;
		try {
			//System.out.println("Starting Worker Thread");
			inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream())); //open an Input Stream from the Client
			outToClient = new PrintStream(socket.getOutputStream()); //open an output stream to the Client
			
			//Start by getting variables in from the Client
			String clientResponse1 = inFromClient.readLine(); //did they press Enter?
			setJokeState(inFromClient.readLine()); //get the joke and proverb state from client
			String userName = inFromClient.readLine(); //get the username from the client

			//System.out.println("Client Repsonse is: " + clientResponse1); checking to see the reponse
			//System.out.println("Client username is:" + userName); //checking for confirmation
			//System.out.println("Client mode is:" + jokeAndProverbState); //checking for confirmation

		    
			if (clientResponse1.equals("") ){ //if the client pressed Enter
				int jokeTotal = 0; //find how many jokes have been told so far
				for( int i=0; i<= 4; i++ ) {
				    if( getJokeState().charAt(i) == 't' ) { //we are counting how many t's there are in the joke and proverb state to see how many jokes have been told
				        jokeTotal++;
				    } 
				}
				
				int proverbTotal = 0; //find how many proverbs have been sent so far

				for( int i=4; i <= 7; i++ ) {
				    if(getJokeState().charAt(i) == 't' ) { //we are counting how many t's there are in the joke and proverb state to see how many jokes have been told
				        proverbTotal++;
				    } 
				}

				
				if(jokeTotal == 4 && JokeServer.jokeMode == true){ //all jokes untold
					System.out.println("Getting a joke"); //State that you are now getting a joke
					setJokeState("ffff" + getJokeState().substring(4)); //reset the state of all jokes to f, false
					//System.out.println("Initialize all jokes back to f: " + getJokeState());
					outToClient.println(getJoke(userName)); //get a joke and send it back to the client
					outToClient.println(getJokeState()); //send the state back to the client
				}
			
				else if (jokeTotal <= 3 && JokeServer.jokeMode == true) //There are still some jokes to tell
				{
					System.out.println("Getting a joke");
					outToClient.println(getJoke(userName));  //get a joke and send to client
					outToClient.println(getJokeState()); //send state back to client
				}
				
				else if(proverbTotal == 4 && JokeServer.proverbMode == true){ //all proverbs have been told
					System.out.println("Getting a proverb");
					setJokeState(getJokeState().substring(0,3) + "ffff") ; //initialize all proverbs back to f
					//System.out.println("Initialize all proverbs back to f: " + getJokeState());
					outToClient.println(getProverb(userName));
					outToClient.println(getJokeState());	//send current state back to client		
				}

				else if(proverbTotal <= 3 && JokeServer.proverbMode == true) {//still some proverbs to tell and in proverb mode
					System.out.println("Getting a proverb");
					outToClient.println(getProverb(userName));//Send proverb
					outToClient.print(getJokeState()); //send state
				}			 
			}
		socket.close(); //connection closes, server stays open
		}catch (IOException ioe) {
			System.out.println(ioe);} 	
	}

	//created to maintain the state
	 public String getJokeState() 
	 	{return jokeAndProverbState;}
	 public void setJokeState (String state)
	 		{this.jokeAndProverbState = state;}
	
	
	//helper functions
	public String getJoke(String username){
		boolean jokeSwitch = true;
		
		while(jokeSwitch) {
			Random rand = new Random();
			int jokeId = rand.nextInt(4); //get a random number from 0-3
			
			//System.out.println("joke Id is " + jokeId); //Used as a check
			//System.out.println("Joke state before " + getJokeState());// used as a check
			
			if(jokeId == 0 && getJokeState().charAt(0) == 'f') { //if you get the first joke and it is untold
				//System.out.println(jokeAndProverbState);
				setJokeState("t" + getJokeState().substring(1));  //update jokeAndProverbState so joke A has been told, set as t (true)
				jokeSwitch = false; //exit the loop
				return("JA " + username + ": Time flies like an arrow, fruit flies like a banana.");
			}
			else if(jokeId == 1 && getJokeState().charAt(1) == 'f') { //if you get the second joke and it is unload
				//System.out.println(jokeAndProverbState);
				setJokeState(getJokeState().substring(0,1) + "t" + getJokeState().substring(2,8)); //update jokeAndProverbState so joke B has been told, set as t (true)
				jokeSwitch = false;
				return("JB " + username + " A plateau is the highest form of flattery.");
			}
			else if(jokeId == 2 && getJokeState().charAt(2) == 'f') {
				//System.out.println(jokeAndProverbState);
				setJokeState(getJokeState().substring(0,2) + "t" + getJokeState().substring(3,8)); //update jokeAndProverbState so joke B has been told, set as t (true)
				jokeSwitch = false;
				return("JC " + username + "  I'm addicted to brake fluid, but I can stop whenever I want.");
			}
			else if(jokeId == 3 && getJokeState().charAt(3) == 'f') {
				//System.out.println(jokeAndProverbState);
				setJokeState(getJokeState().substring(0,3) + "t" + getJokeState().substring(4,8)); //update jokeAndProverbState so joke B has been told, set as t (true)
				jokeSwitch = false;
				return("JD " + username + "  What's orange and sounds like a parrot? A carrot.");
			}
		}
		return("Error");
	}

	public String getProverb(String username){
		boolean proverbSwitch = true;

		while(proverbSwitch){
			Random rand = new Random();
			int proverbId = rand.nextInt(7-4+1)+4;
			
			if(proverbId == 4 && getJokeState().charAt(4) == 'f') {
				//System.out.println("PRoverb State before change " + getJokeState());
				setJokeState(getJokeState().substring(0,4) + "t" + getJokeState().substring(5));//update jokeAndProverbState so proverb A has been told, set as t (true)
				proverbSwitch = false;
				//System.out.println("PRoverb State after change " + getJokeState());
				return("PA " + username + ": Two wrongs don't make a right.");
			}
			else if(proverbId == 5 && getJokeState().charAt(5) == 'f') {
				//System.out.println("PRoverb State before change " + getJokeState());
				setJokeState(getJokeState().substring(0,5) + "t" + getJokeState().substring(6)); //update jokeAndProverbState so proverb B has been told, set as t (true)
				proverbSwitch = false;
				//System.out.println("PRoverb State after change " + getJokeState());
				return("PB " + username + ": The pen is mightier than the sword.");
			}
			else if(proverbId == 6 && getJokeState().charAt(6) == 'f') {
				//System.out.println("PRoverb State before change " + getJokeState());
				setJokeState(getJokeState().substring(0,6) + "t" + getJokeState().substring(7));//update jokeAndProverbState so proverb C has been told, set as t (true)
				proverbSwitch = false;
				//System.out.println("PRoverb State after change " + getJokeState());
				return("PC " + username + ": When in Rome, do as the Romans.");
			}
			else if(proverbId == 7 && getJokeState().charAt(7) == 'f') {
				//System.out.println("PRoverb State before change " + getJokeState());
				setJokeState(getJokeState().substring(0,7) + "t") ;//update jokeAndProverbState so proverb D has been told, set as t (true)
				proverbSwitch = false;
				//System.out.println("PRoverb State after change " + getJokeState());
				return("PD " + username + ": The squeaky wheel gets the grease.");
			}

		}
		return("Error");
	}

} //End of worker thread



class AdminLooper implements Runnable{  
	public static boolean adminControlSwitch = true;
	
	public void run(){
		//System.out.println("In the admin looper thread");
		
		int queue_len = 6;
		int adminport = 5050; //listening for Admin clients
		Socket sockToAdmin;
		
		try{
			ServerSocket servsock = new ServerSocket(adminport, queue_len);
			while(adminControlSwitch){
				sockToAdmin = servsock.accept(); // server is waiting for a new admin connection
				new AdminWorker(sockToAdmin).start(); //admin worker thread is crated to handle the new admin connection
			}
		}catch (IOException ioe) {System.out.println(ioe);}
	}
}


class AdminWorker extends Thread {
	Socket sockToAdmin;	//Create a Socket variable sock
	AdminWorker (Socket s1) 
		{sockToAdmin = s1;} //Create a contructor for Worker
	
	public void run() { //must have run program to start thread
		PrintStream outToAdmin = null;
		BufferedReader inFromAdmin = null;
		String userInput;
		try {
			inFromAdmin = new BufferedReader(new InputStreamReader(sockToAdmin.getInputStream())); //open an Input Stream
			outToAdmin = new PrintStream(sockToAdmin.getOutputStream()); //open an output stream
			System.out.println("Admin connected");
			
			//Send state of server
			userInput = inFromAdmin.readLine(); //find out what the mode is

			//update state mode
			if(userInput.equals("joke")){
				System.out.println("Joke mode now active");
				JokeServer.jokeMode = true;
				JokeServer.proverbMode = false;
			}
			else if(userInput.equals("proverb")){
				System.out.println("Proverb mode now active");
				JokeServer.proverbMode = true;
				JokeServer.jokeMode = false;
			}
			
			sockToAdmin.close(); //connection closes, server stays open
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
}
