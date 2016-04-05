/*
 * JClientHandler.java
 *
 *Authors: Tim Kristedja & Austin Ayers
 *@version: 1
 *
 * This class handles communication between the client
 * and the server.  It runs in a separate thread but has a
 * link to a common list of sockets to handle broadcast.
 */

import java.net.Socket;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;

public class JClientHandler implements Runnable
{
	private Socket connectionSock = null;
	private ArrayList<socketAndName> socksAndNames;
	private String _senderName;
	public int state;
	private String message;

	JClientHandler(Socket sock, ArrayList<socketAndName> socksAndNames)
	{
		this.connectionSock = sock;
		this.socksAndNames = socksAndNames;	// Keep reference to master list
	}

	public synchronized void setState(int fromServer){
		state = fromServer;
		notify();
	}
	public void setMessage(String msg){
		message = msg;
	}

	public String getName(){return _senderName;}



	public synchronized void run()
	{
        		// Get data from a client and send it to everyone else
		try
		{
			System.out.println("Connection made with socket " + connectionSock);

			BufferedReader clientInput = new BufferedReader(
				new InputStreamReader(connectionSock.getInputStream()));

			//gets name from socket
			for (socketAndName s: socksAndNames){
				if (s.socket == connectionSock){
					_senderName = s.name;
				}
			}


			while (state<3)
			{
				System.out.println(state);
				switch (state){
					case 0:
						System.out.println("BBBBB"+state);
								DataOutputStream clientOutput = new DataOutputStream(connectionSock.getOutputStream());
								clientOutput.writeBytes("Please wait for other users to connect." + "\n");

						wait();
						break;
					case 1:
						System.out.println("AAAA" +state);

						clientOutput = new DataOutputStream(connectionSock.getOutputStream());
						clientOutput.writeBytes("Press [enter] to buzz in: "+"\n");
						clientOutput.writeBytes(message+"\n");
						clientInput.readLine();
						System.out.println(_senderName + " buzzed in first.");
						state = 2;
						notifyAll();
						break;
					case 2: //this handler buzzed in
						wait(); //add stuff here later
						break;
				}
			}

				  // Connection was lost
				  System.out.println("Closing connection for socket " + connectionSock);
				   // Remove from arraylist
				   socksAndNames.remove(connectionSock);
				   connectionSock.close();
		}
		catch (Exception e)
		{
			System.out.println("Error: " + e.toString());
			// Remove from arraylist
			socksAndNames.remove(connectionSock);
		}
	}
} // ClientHandler for MTServer.java
