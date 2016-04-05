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
    private int state;

	JClientHandler(Socket sock, ArrayList<socketAndName> socksAndNames)
	{
		this.connectionSock = sock;
		this.socksAndNames = socksAndNames;	// Keep reference to master list
        state = 1;
	}

	public void run()
	{
        		// Get data from a client and send it to everyone else
		try
		{
			System.out.println("Connection made with socket " + connectionSock);

			BufferedReader clientInput = new BufferedReader(
				new InputStreamReader(connectionSock.getInputStream()));



			while (true)
			{
                DataOutputStream question_output;
                switch(state){
                    case 1:
                        question_output = new DataOutputStream(connectionSock.getOutputStream());
                        question_output.writeBytes("Question 1: What is the air speed of a unladen swallow?");
                        break;
                    default:
                        break;
                }

                String clientText = clientInput.readLine();
                if (clientText.equals("_answered" + this.state.toString())){
                    state++;
                }
                else if (clientText != null)
                {
                    System.out.println("Received: " + clientText);
                  //gets name of client at that connectionSock
                  for (socketAndName s: socksAndNames){
                    if (s.socket == connectionSock)
                      clientText  = s.name + ": "+ clientText; //appends name to front of message
                  }

                    // Turn around and output this data
                    // to all other clients except the one
                    // that sent us this information
                    for (socketAndName s : socksAndNames)
                    {
                        if (s.socket != connectionSock)
                        {
                            DataOutputStream clientOutput = new DataOutputStream(s.socket.getOutputStream());
                            clientOutput.writeBytes("_answered" + this.state.toString());
                        }
                    }
                }
                else
                {
                  // Connection was lost
                  System.out.println("Closing connection for socket " + connectionSock);
                   // Remove from arraylist
                   socksAndNames.remove(connectionSock);
                   connectionSock.close();
                   break;
                }
			}
		}
		catch (Exception e)
		{
			System.out.println("Error: " + e.toString());
			// Remove from arraylist
			socksAndNames.remove(connectionSock);
		}
	}
} // ClientHandler for MTServer.java