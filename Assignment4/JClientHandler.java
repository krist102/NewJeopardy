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
	private ArrayList<JClientHandler> handlers;
	private String _senderName;
	public int state;
	private String message;

	JClientHandler(Socket sock, ArrayList<socketAndName> socksAndNames)
	{
		this.connectionSock = sock;
		this.socksAndNames = socksAndNames;	// Keep reference to master list
	}

	public synchronized void setState(int s){
		state = s;
		notify();//wakes up the thread
	}
	public void setMessage(String msg){
		message = msg;
	}
	private int getState(){return state;}

	public String getName(){return _senderName;}

	public void setHandlers(ArrayList<JClientHandler> h){
		handlers = h;
	}



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


			while (state<=3)
			{
				DataOutputStream clientOutput = new DataOutputStream(connectionSock.getOutputStream());
					if (state == 0){
								clientOutput.writeBytes("Please wait for other users to connect." + "\n");

						wait();
					}
					else if (state == 1){

						clientOutput.writeBytes("Press [enter] to buzz in: "+"\n");
						clientOutput.writeBytes(message+"\n");
						clientInput.readLine();
						if (this.getState() ==1)
							state = 2;
					}
					else if (state ==2){ //this handler buzzed in

						for (JClientHandler h : handlers){
							if (h!=this){
								h.state = 3;
							}
						}
						System.out.println(_senderName + " buzzed in first.");
						wait(); //quit program
					}
					else if (state == 3){
						wait();
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
