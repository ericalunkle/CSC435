/*--------------------------------------------------------

1. Name / Date: Erica Unkle/ 10-8-2017

2. Java version used, if not the official version for the class:

 java 1.8.0_144

3. Precise command-line compilation examples / instructions:

> javac MyWebServer.java


4. Precise examples / instructions to run this program:

In the firefox browser type: http://localhost:2540
This will bring up the directory. From there you can click on the different files

You can also try: http://localhost:dog.txt


5. List of files needed for running the program.

 a. MyWebServer.java

5. Notes:

Getting some error when I include a sub-directory.
Also getting some errors with the favicon.ico. I don't quite understand what this is doing

----------------------------------------------------------*/


import java.io.*; //Import necessary libraries
import java.net.*; 

class Worker extends Thread {
	Socket sock;	//Create a Socket variable sock
	Worker (Socket s1) {sock = s1;} //Create a contructor for Worker
	
	public void run() { //must have run program to start thread
		PrintStream outToClient = null; //create outstream in order to send data to the client
		BufferedReader inFromClient = null; //create buffered reader in order to recieve from the client
		try {
			outToClient = new PrintStream(sock.getOutputStream());
      		inFromClient = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			try{
				String request;
				request = inFromClient.readLine(); //read the line of text from the input stream
				//System.out.println("Looking up " + request); //Looks up name typed in by Client
				printRemoteAddress(request, outToClient); //This will send the 'name' to the Client 
				String fileName;
				String contentType = ""; //declare the content type to nothing to start
  
		      //get the filename
		      if(!request.startsWith("GET")){ //We only want to work with GET requests
		        System.out.println("Must be a GET request"); //Send a message to the server to let them know it must be a GET request
		    	}
		      else if(request.startsWith("GET")){ //if we have a GET request     	
		      	//System.out.println("Request started with GET");
		        fileName = request.substring(4, request.length()-9).trim(); //Remove the GET and http/1.1 from the request
		        System.out.println("The filename is : "+ fileName); //for debugging 

            //Using if else to assign correct MIME types for each header
		        if(fileName.endsWith(".txt")){ //if the file ends in .txt, set the content type to text/plain
		          contentType = "text/plain"; //setting the contect type of the header
		          displayFileToBrower(fileName, outToClient, contentType); //send info in order for Header to be sent to the Browser
		        }
		        else if(fileName.endsWith(".html")){ //if the file ends in .html, set the contenr type to text/html
		          contentType = "text/html"; //setting the contect type of the header
		          displayFileToBrower(fileName, outToClient, contentType); //send info in order to display to browers
		        }
		        else if(fileName.endsWith(".java")){ //if file ends in .java then we want to treat it as a .txt
		          contentType = "text/plain";//setting the contect type of the header
		          displayFileToBrower(fileName, outToClient, contentType); //send info in order to display to browser
		        }
		        else if(fileName.endsWith("/")){ //if file ends in /, then its a directory/folder, not a file
		          System.out.println("This is a directory");//used for debugging
		          contentType = "text/html";
		          displayDirectoryToBrower(fileName, outToClient, contentType); //send the file, printstream, and content type
		        }
		        else if(fileName.contains(".fake-cgi")){ //if the file is a fake-cgi file, treat it as text/html and run the addnums
		        	System.out.println("Addnums:");
		        	contentType = "text/html";
		        	addNums(fileName, outToClient, contentType); //send the file, printstream, and content type to addNums
		        }
            
		        else{ //all other files should be treated as text/plain
		        	contentType = "text/plain";  
		        	displayFileToBrower(fileName, outToClient, contentType);
		        }
		      } //end of if-else block 


			} catch (IOException x) {
				System.out.println("Server read error");
				x.printStackTrace();
			}
			sock.close(); //connection closes, server stays open
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
	
	static void printRemoteAddress (String name, PrintStream out) {//utility function left over from InetServer
		try{
			//out.println("Looking up " + name + "..."); //Sent to Client
			InetAddress machine = InetAddress.getByName (name);
			out.println("Host name : " + machine.getHostName()); //Sent to Client
			out.println("Host IP : " + toText(machine.getAddress())); //Sent to Client
		} catch(UnknownHostException ex) {
			//out.println("Failed attempt to loop up " + name);
		}
	}
	
	static String toText (byte ip[]) { //utility function left over from InetServer
		StringBuffer result = new StringBuffer();
		for(int i = 0; i < ip.length; i++) {
			if (i>0) result.append(".");
			result.append(0xff & ip[i]);
		}
		return result.toString();
	}

	//used to send the header to the browser for Files
	  public void displayFileToBrower(String fileName, PrintStream out, String contentType) throws IOException{

	   //get rid of leading / if there is one
	    if(fileName.startsWith("/")) { // if there is a leading slash, 
	      fileName = fileName.substring(1); // remove leading slash from file
	      }
    
     //Sent requested file to client
      InputStream in = new FileInputStream(fileName); //opens the file using instream
      File file = new File(fileName); //creating a new file

      //Send header to printstream
      //header should include the content length and content type (MIME type)
      out.print("HTTP/1.1 200 OK\r\n" + "Content Length: " + file.length() + "\r\n" + "Content Type: "  + contentType +"\r\n" + "\r\n\r\n");
      
      //Sent header out to the browser by bytes
      byte[] Bytes = new byte[10000];
      int numberOfBytes = in.read(Bytes); //count the number of bytes 
      out.write(Bytes, 0, numberOfBytes); //write bytes to the outStream
      out.flush(); //flush the PrintStream
      in.close(); //close the inputstream for the file
  }//end of displayfiletobrowser

//used to send headers to browser for directories
    public void displayDirectoryToBrower(String folderName, PrintStream out, String contentType) throws IOException{
    	System.out.println("Sending directory to browser"); //for debugging

    	StringBuilder stuffToDisplay = new StringBuilder(); //create a string for the HTML code that will be used to display the hot-links
    	StringBuilder headerToSend = new StringBuilder();

		File file = new File("."); //creating file for the current directory
		String directory = null; //declaure the directory string to start

		//if the foldername is empty, then we set the directory to /
		if(folderName.equals("")){
	    	directory = "/"; //set directory to /
	    }
	    else{
	    	//otherwise the file is a file, and so we set the file to a ./ + the name of the file
	    	file = new File("./" + folderName); 
	    	folderName = "/" + folderName;
	    	directory = folderName.substring(0, folderName.lastIndexOf("/")); //the directory is the name up until the last /

	    	//if the directory is empty, then we set it to /
	    	if(directory.equals("")){
	    		directory = "/";
	    	}
	    }

	    File[] listOfFiles = file.listFiles(); //create a list of files in the directory in order to loop through

	    stuffToDisplay.append("<h1> Index of " + folderName + "</h1>"); //Start creating html to be displayed
	    stuffToDisplay.append("<pre>");
	    stuffToDisplay.append("<a href='" + directory + "'>Parent Directory</a><br><br>"); //display the parent directory

	    for(int i =0; i<listOfFiles.length; i++){//create a loop to go through all the files or folders in the directory
	    	if(listOfFiles[i].isDirectory()){ //if the file is actually a directory, print the directory name
	    		//use .substring(1) to get the /filename, use .substring(2) to get just filename for display purposes
	    		stuffToDisplay.append("<a href=" + listOfFiles[i].toString().substring(1) + ">" + listOfFiles[i].toString().substring(2) + "</a><p></p>");
	    		System.out.println("This is folder: " + listOfFiles[i]); //send to server for debugging
	    	}
	    	if(listOfFiles[i].isFile()){ //if the file is indeed a file, then print the file name
	    		//use .substring(1) to get the /filename, use .substring(2) to get just filename for display purposes
	    		stuffToDisplay.append("<a href=" + listOfFiles[i].toString().substring(1) + ">" + listOfFiles[i].toString().substring(2) + "</a><p></p>");
	    		 System.out.println("This is file: " + listOfFiles[i]); //send to server for debugging
	    	}

	    }

	    stuffToDisplay.append("</pre>");
	    String stuffToDisplayString = stuffToDisplay.toString(); //create string from stringbuilder
	    
	    //create header to send out to printstream
	    headerToSend.append("HTTP/1.1 200 OK\r\n" + "Content Length: " + stuffToDisplayString.getBytes().length + "\r\n"  + "Content Type: "  + contentType +"\r\n" + "\r\n\r\n");
	    //send to server for debugging
	    System.out.println("HTTP/1.1 200 OK\r\n" + "Content Length: " + stuffToDisplayString.getBytes().length + "\r\n"  + "Content Type: "  + contentType +"\r\n" + "\r\n\r\n");
	    

	    //Sent header out to the browser
	    out.println(headerToSend.toString());
	    //Send html info to be displayed in the browser
		out.println(stuffToDisplayString);
  }//end of displacydirectory


  //addNums is to hand the .cgi html file to add two numbers together
  	public void addNums(String folderName, PrintStream out, String contentType){
	  	System.out.println("The foldername is :" + folderName); //sent to server for debugging
	  	out.println("HTTP/1.1 200 OK\r\n" + "Content Type: " + contentType + "\r\n\r\n"); //send header to browser
	  	out.println("<html><head></head><body>"); //start of html body

	  	//System.out.println("Header = HTTP/1.1 200 OK\r\n" + "Content Type: " + contentType + "\r\n\r\n");

	  	String[] parsename = folderName.split("[?]"); //parse the fname of the file into two pieces

	  	String info = parsename[1]; //this should inclue the person, num1, and num2 info
	  	//System.out.println("Info = " + info);

	  	String[] parseInfo = info.split("="); // this creates 4 pieces that will be used to parse further 
	  	String personpart1 = parseInfo[1]; //name&num1
	  	//System.out.println("personpart1 = " + personpart1); //name&num1
	  	String parseInfo2 =  parseInfo[2];
	  	//System.out.println("second part = " +  parseInfo2); //#&num2
	  	//System.out.println("thirdpart = " + parseInfo[3]); //#


	  	String[] personpart2 = personpart1.split("&"); //split the name&num1 up into two parts
	  	String person = personpart2[0]; //This should give us the name of the person 
	  	System.out.println("Person = " + person); //sent to server for debugging
	  	
	  
	  	String[] num1info = parseInfo2.split("&"); //split the #&num2
	  	String number1 = num1info[0]; //take the first part pf #&num2
	    //System.out.println("Number 1= " +number1);
	  	String number2 = parseInfo[3]; //the second #
	  	//System.out.println("Number2= "+ number2);
	 
	  	Integer number1int = Integer.parseInt(number1); //convert the string to an integer in order to add them together
	  	Integer number2int = Integer.parseInt(number2); //convert the string to an integer in order to add them together
	  	Integer result = number1int + number2int; //add the two numbers from the browser together
	  	String result1 = String.valueOf(result); //get the result and cast it as a string

	  	//Send the result to the web browser
	  	out.println("<font size = 50> The request of " + person + " is the sum of " + number1 + " + " + number2 + "=" + result1 +"</font><b></b>\n");
	  	//Also send the result to the server for debugging
	  	System.out.println("The sum of " + number1 + " + " + number2 + "=" + result1); 
	  	out.println("</body></html>");
	  	out.flush(); //flush the printstream


	  }//end of addnums


}


public class MyWebServer {
	public static void main(String a[]) throws IOException {
		int queue_len = 6; //queue length
		int port = 2540; //set Port to communicate with Client
		Socket sock;
		
		ServerSocket servsock = new ServerSocket(port, queue_len); //create server socket at specified port and queue length
		
		System.out.println("Erica Unkle's Web server starting up, listening at localhost, port 2540.\n");
		while (true){ //server is always open
			sock = servsock.accept(); //server is wating for a new client connection
			new Worker(sock).start(); //Worker thread is created to handle the new client connection
		}
	}
}