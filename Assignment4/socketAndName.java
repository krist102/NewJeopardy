/*Authors: Tim Kristedja & Austin Ayers 
*@version: 1
*
*This class is used to store the socket with the client's name
*/

import java.net.Socket;

public class socketAndName{
  public String name;
  public Socket socket;

  public socketAndName(Socket s, String n){
      socket = s;
      name = n;
  }
}
