/**
 * ClientListener.java
 *
 *Authors: Tim Kristedja & Austin Ayers
 *@version: 1
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
            InputStreamReader reader = new InputStreamReader(System.in);
            BufferedReader in = new BufferedReader(reader);
			BufferedReader serverInput = new BufferedReader(new InputStreamReader(connectionSock.getInputStream()));
            DataOutputStream clientOutput = new DataOutputStream(this.connectionSock.getOutputStream());
			while (true)
			{
				// Get data sent from the server
				String serverText = serverInput.readLine();
                if(serverText.substring(0,5).equals("_answ")){
                    clientOutput.writeBytes(serverText);
                }
                String name = in.readLine();
				clientOutput.writeBytes(name + "\n");
			}
		}
		catch (Exception e)
		{
			System.out.println("Error: " + e.toString());
		}
	}
} // JClientListener for JClient
