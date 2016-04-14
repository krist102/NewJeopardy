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
    public int buzz_num, global_buzz; // number that the client buzzed in as
	public int state;
	public int score, questionVal;
	private String message, answer, right_answer;
	public boolean questionAttempted, questionPhase; //will be true when the user tries to answer a question

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
      return this.answer.equalsIgnoreCase(this.right_answer);
  }
    public int getbuzz_num(){return buzz_num;}
    public void setbuzz_num(int num){this.buzz_num = num;}
	public int getState(){return state;}

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

			while (state<=11)
			{
				switch (state){
					case 0:
						clientOutput.writeBytes("Please wait for other users to connect." + "\n");
						clientOutput.writeBytes("00ENTERSTATE"+"\n");
						wait();
						break;
					case 1:
						clientOutput.writeBytes("01ENTERSTATE"+"\n");
						clientOutput.writeBytes("Press [enter] to buzz in: "+"\n");
						questionVal = Integer.parseInt(message.substring(0,4)); //message will = "0123Question will be here?"
						clientOutput.writeBytes(message.substring(4) + "\n"); 	//Question will be here?
						clientInput.readLine();
						questionAttempted = true;
						this.state = 10;
						break;
					case 2:
						for (socketAndName s : socksAndNames){ //change other client states
							if (s.socket != connectionSock){
                                DataOutputStream otherClientStream = new DataOutputStream(s.socket.getOutputStream());
                                otherClientStream.writeBytes(this._senderName + " is typing an answer, please wait... (And hope that they're wrong!)"+"\n");
							}
						}
						clientOutput.writeBytes("01ENTERSTATE"+"\n"); //tells my client to enter a typing allowed state
						clientOutput.writeBytes("Do you still want to answer the question? (y/n): "+"\n");
						this.answer = clientInput.readLine();
                        System.out.println(this.answer);
                        if(this.answer.equals("n")){
                            for (socketAndName s : socksAndNames){ //change other client states
								if (s.socket != connectionSock){
									DataOutputStream otherClientStream = new DataOutputStream(s.socket.getOutputStream());
									otherClientStream.writeBytes(this._senderName+" passed!"+"\n");
									otherClientStream.writeBytes("01ENTERSTATE"+"\n"); //tells client to go to a typing state
								}
							}
                            state = 3; //change my handler state
                                for (JClientHandler h : handlers){
                                    h.global_buzz++; // allows next client to answer the question
                                    if(h.global_buzz == 4){
                                        state = 4;
                                    }
                                }
                        }
                        else{
                            clientOutput.writeBytes("Please enter the question: "+"\n");
                            this.answer = clientInput.readLine();
                                if(this.checkAnswer()){ //my client answered correctly
                                for (socketAndName s : socksAndNames){ //change other client states
                                    if (s.socket != connectionSock){
                                        DataOutputStream otherClientStream = new DataOutputStream(s.socket.getOutputStream());
                                        otherClientStream.writeBytes(this._senderName + " answered correctly!" + "\n");
                                        otherClientStream.writeBytes("00ENTERSTATE" + "\n"); //tells client to go to a nontyping state
                                    }
                                    else{
                                        clientOutput.writeBytes("Congratulations, you answered correctly!"+"\n");
                                        System.out.println(_senderName+" answered correctly.");
                                        clientOutput.writeBytes("00ENTERSTATE"+"\n"); //tells client to go to nontyping state
                                    }
                                }
                                this.state = 4;
                            }
                            else{//my client answered incorrectly
                                for (socketAndName s : socksAndNames){ //change other client states
                                    if (s.socket != connectionSock){
                                        DataOutputStream otherClientStream = new DataOutputStream(s.socket.getOutputStream());
                                        otherClientStream.writeBytes(this._senderName+" answered incorrectly!"+"\n");
                                        otherClientStream.writeBytes("Their answer was: "+answer+"\n");
                                        otherClientStream.writeBytes("01ENTERSTATE"+"\n"); //tells client to go to a typing state
                                    }
                                    else{
                                        clientOutput.writeBytes("WRONGGGGGG you answered incorrectly!"+"\n");
                                        System.out.println(_senderName+" answered incorrectly. They answered with: "+answer);
                                        clientOutput.writeBytes("00ENTERSTATE"+"\n"); //tells my client to go to nontyping state
                                    }
                                }
                                state = 3; //change my handler state
                                for (JClientHandler h : handlers){
                                    h.global_buzz++; // allows next client to answer the question
                                    if(h.global_buzz == 4){
                                        state = 4;
                                    }
                                }
                            }
                        }
						break; //end of state 2

					case 3: //waiting state
						break;
					case 4: //resetting questionAttempted / new question
						break;
                    case 9: // first
                        if(this.questionPhase){
                            if(this.buzz_num == this.global_buzz){ // go to question answering phase in order
                                this.state = 2;
                            }
                        }
                        break;
                    case 10: // handles the number in which people buzz in
                        int n = 3;
                        for (JClientHandler h : handlers){
							if (h!=this){
								if(h.buzz_num == -1){
                                    n--;
                                }
							}
						}
                        clientOutput.writeBytes("You buzzed in! You are #"+n+"! (Please wait until the others buzz in)\n");
                        this.buzz_num = n;
                        if(this.buzz_num == 3){
                            for (JClientHandler h : handlers){
							    h.questionPhase = true;
						}
                        }
                        this.global_buzz = 1;
                        this.state = 9;
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
