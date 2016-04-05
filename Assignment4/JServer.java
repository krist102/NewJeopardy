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
	private synchronized void askQustion(){
		for (JClientHandler h : handlers){
			h.setState(1);
			h.setHandlers(handlers);
			h.setMessage("The process for establishing a TCP connection.");
			//wait for response
			//ClientHandler will print out who buzzes in first
		}
	}

	public static void main(String[] args)
	{
		JServer server = new JServer();
		server.getConnection();
		server.printConnections();
		server.askQustion();
	}
} // JServer
