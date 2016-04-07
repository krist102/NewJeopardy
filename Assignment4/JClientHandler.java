/*
 * JClientHandler.java
 *
 *Authors: Tim Kristedja & Austin Ayers
 *@version: 2
 *
 * This class handles communication between the client
 * and the server.  It runs in a separate thread but has a
 * link to a common list of sockets to handle broadcast.
 *
 * it keeps track of its name and who buzzed in
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
	private String message, answer, right_answer;

	JClientHandler(Socket sock, ArrayList<socketAndName> socksAndNames)
	{
		this.connectionSock = sock;
		this.socksAndNames = socksAndNames;	// Keep reference to master list
        this.state = 0;
	}

	public synchronized void setState(int s){
		state = s;
		notify();//wakes up the thread
	}
    public synchronized void setAnswer(String answer){
		this.right_answer = answer;
		notify();//wakes up the thread
	}
	public void setMessage(String msg){
		message = msg;
	}
    public boolean checkAnswer(){
        return this.answer.equals(this.right_answer);
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
            DataOutputStream clientOutput = new DataOutputStream(connectionSock.getOutputStream());
			while (state<=10)
			{
					if (state == 0){
						clientOutput.writeBytes("Please wait for other users to connect." + "\n");
						wait();
					}
					else if (state == 1){

						clientOutput.writeBytes("Press [enter] to buzz in: "+"\n");
						clientOutput.writeBytes(message + "\n");
						clientInput.readLine();
						if (this.getState() == 1)
							state = 2;
					}
					else if (state == 2){
						for (JClientHandler h : handlers){
							if (h!=this){
                                clientOutput.writeBytes("Someone else buzzed in first.");
                                System.out.println("Someone else buzzed in first.");
                                clientOutput.writeBytes(this._senderName + " is typing an answer, please wait... (And hope that they're wrong)");
                                System.out.println(this._senderName + " is typing an answer, please wait... (And hope that they're wrong)");
								h.state = 3;
							}
						}
                        clientOutput.writeBytes("Please enter a correct question: ");
                        System.out.println("Please enter a correct question: ");
                        this.answer = clientInput.readLine();
                        if(this.checkAnswer()){
                            for (JClientHandler h : handlers){
							    clientOutput.writeBytes("The question was answered correctly!");
                                System.out.println("The question was answered correctly!");
                            }
						}

                        else{
                            for (JClientHandler h : handlers){
							    clientOutput.writeBytes("The question was NOT answered correctly!");
                                System.out.println("The question was NOT answered correctly!");
						    }
                        }
                        wait();
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
