/**
 * ClientListener.java
 *
 *Authors: Tim Kristedja & Austin Ayers
 *@version: 2
 *
 * This class runs on the client end and just
 * displays any text received from the server.
 *
 */
import java.net.Socket;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;

public class JClientListener implements Runnable
{
	int c_state;

	private Socket connectionSock = null;

	JClientListener(Socket sock)
	{
		this.connectionSock = sock;
	}

	public void run()
	{
       		 // Wait for data from the server.  If received, output it.
		try
		{
			BufferedReader serverInput = new BufferedReader(new InputStreamReader(connectionSock.getInputStream()));
			while (true)
			{
				// Get data sent from the server
				String serverText = serverInput.readLine();
				if (serverText.substring(2).equals("ENTERSTATE")){ //enter state msg will look like '01ENTERSTATE' to enter state 1
					String c_state_str = serverText.substring(0,2);
					c_state = Integer.parseInt(c_state_str);
					System.out.println("I entered state: "+c_state_str);
				}
				else if (serverInput != null)
				{
					System.out.println(serverText);
				}
				else
				{
					// Connection was lost
					System.out.println("Closing connection for socket " + connectionSock);
					connectionSock.close();
					break;
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("Error: " + e.toString());
		}
	}
} // JClientListener for JClient
