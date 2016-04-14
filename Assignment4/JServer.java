/**
 * JServer.java
 *
 *Authors: Tim Kristedja & Austin Ayers
 *@version: 2
 *
 *
 *
 * This program implements a simple multithreaded chat server.  Every client that
 * connects to the server can broadcast data to all other clients.
 * The server stores an ArrayList of sockets to perform the broadcast.
 *
 * The JServer uses a JClientHandler whose code is in a separate file.
 * When a client connects, the JServer starts a JClientHandler in a separate thread
 * to receive messages from the client.
 *
 * To test, start the server first, then start multiple clients and type messages
 * in the client windows.
 *
 * This program also tells clients their number in which they connect and stores their names
 *
 *Also asks a question
 */
import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;

public class JServer
{
	// Maintain list of all client sockets for broadcast
	private ArrayList<socketAndName> socksAndNames; //will also store the names of all the clients
	private int clientNum;
	private ArrayList<JClientHandler> handlers;
	private static int coundownTime = 5;
    public int state = 0;
    public boolean question_asked = true; // used to facilitate a new question
	//private ArrayList<Thread> threads;

	public JServer()
	{
		socksAndNames = new ArrayList<socketAndName>();
		handlers= new ArrayList<JClientHandler>();
		//threads= new ArrayList<Thread>();
	}

	private void getConnection()
	{
		// Wait for a connection from the client
		clientNum = 0;
		try
		{
			System.out.println("Waiting for client connections on port 7654.");
			ServerSocket serverSock = new ServerSocket(7654);
			// This is an infinite loop, the user will have to shut it down
			// using control-c
			while (clientNum<3)
			{
				Socket connectionSock = serverSock.accept();

				//replies to the new connection their client number
				clientNum++;
				if (clientNum >= 3){serverSock.close();}
				DataOutputStream firstReplyToClient = new DataOutputStream(connectionSock.getOutputStream());
				String reply = "You are the "+clientNum;
				switch(clientNum) {
					case 1: reply +="st "; break;
					case 2: reply +="nd "; break;
					case 3: reply +="rd "; break;
					default: reply +="th "; break;
				}
				reply+="client to connect to the server.";
				firstReplyToClient.writeBytes(reply + "\n");

				//gets name from client
				BufferedReader firstReplyFromClient = new BufferedReader(
					new InputStreamReader(connectionSock.getInputStream()));

				String name = firstReplyFromClient.readLine().substring(5); //gets name from client program(client will send "HELO example name");


				// Add this socketAndName to the list
				socksAndNames.add(new socketAndName(connectionSock,name));

				JClientHandler handler = new JClientHandler(connectionSock, this.socksAndNames);
				handler.server = this;
				handlers.add(handler);
				Thread theThread = new Thread(handler);
				//threads.add(theThread);
				theThread.start();
			}



		}
		catch (IOException e)
		{
			System.out.println(e.getMessage());
		}
	}

	//prints the ArrayList after all 3 clients connect to the server
	private void printConnections(){
		System.out.println("Max clients reached ("+clientNum+"). No more connections accepted. Sockets and Names will print below.");
		for (socketAndName s : socksAndNames){
			System.out.println("\tName: "+s.name+" was at socket: "+s.socket);
		}
	}
	//prints the ArrayList after all 3 clients connect to the server
	private void printHandlers(){
		System.out.println("Handlers will print below.");
		for (JClientHandler h : handlers){
			System.out.println(h);
		}
	}
	private synchronized void askQustion(String question, String value){
		for (JClientHandler h : handlers){
			h.setHandlers(handlers); //makes sure all handlers have up to date handler lists
            h.setbuzz_num(-1);
            h.questionPhase = false;
			h.message = value+question; //asks question
			h.state = 1;

			//wait for response
			//ClientHandler will print out who buzzes in first
		}
	}
    private synchronized void nextQuestion(){
//
    }
    private synchronized void setAnswer(String answer){
        for (JClientHandler h : handlers){
			h.answer = answer;
		}
  }


	public static void main(String[] args)
	{
		JServer server = new JServer();
		server.getConnection();
		server.printConnections();
        while(true){
            switch(server.state){
                case 0: // question 1
                    if(server.question_asked){
                        server.question_asked = false;
                        server.setAnswer("What is the Three Way Handshake?");
                        server.askQustion("The process for establishing a TCP connection.","0100"); //a question and its value 100 in this case **make sure it is 4 characters wide
                    }
                    server.nextQuestion();
                    break;
                case 1: // question 2
									System.out.println("nextQuestion has been reached TMIMMMMMMMMMMMM");
                    if(server.question_asked){
											System.out.println("HEREREEEEEEE");
                        server.question_asked = false;
                        server.setAnswer("What is 128?");
                        server.askQustion("An IPv6 address is this many bits long.","0200"); //a question and its value 100 in this case **make sure it is 4 characters wide
                    }
                    server.nextQuestion();
                    break;
                case 2: // question 3
                    if(server.question_asked){
                        server.question_asked = false;
                        server.setAnswer("What is 16?");
                        server.askQustion("A port number is this many bits long.","0300"); //a question and its value 100 in this case **make sure it is 4 characters wide
                    }
                    server.nextQuestion();
                    break;
                case 3: // question 4
                    if(server.question_asked){
                        server.question_asked = false;
                        server.setAnswer("What is propogation delay?");
                        server.askQustion("Distance to propagation speed.","0400"); //a question and its value 100 in this case **make sure it is 4 characters wide
                    }
                    server.nextQuestion();
                    break;
                case 4: // question 5
                    if(server.question_asked){
                        server.question_asked = false;
                        server.setAnswer("What is UDP?");
                        server.askQustion("RIP uses the service of this protocol.","0500"); //a question and its value 100 in this case **make sure it is 4 characters wide
                    }
                    server.nextQuestion();
                    break;
                default:
                    break;
            }
        }
	}
} // JServer
