package mailclient;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class MailClient {
 
    private Socket s;
        
    public MailClient(String ip,int port) throws IOException
    {
        s=null;
        try
        {
            s = new Socket(ip,port);
        }
        catch (IOException e) 
        {
            System.out.println("Failed to connect!");
            System.out.println("Make sure to enter correctly your server's IP address and port!");
        }
    }
    
    public Socket getSocket()
    {
        return s;
    }
    
    public MailClient()
    {
        s = null;
    }
    
    /**
     *
     * @param args the command line arguments = the server's ip address and port
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException 
    {
        try  
        {
            if (args.length < 2) 
            {
            System.out.println("Invalid arguments! Please provide IP address and port number!");
            return;
            }
            String ip = args[0];
            int port = Integer.parseInt(args[1]);
            Scanner scanner = new Scanner(System.in);
            MailClient client = new MailClient(ip,port);
            if (client.getSocket()==null)
            {
                //an exception has been caught in the constructor
                return; 
            }
            DataInputStream in = new DataInputStream(client.s.getInputStream());
            DataOutputStream out = new DataOutputStream (client.s.getOutputStream());
            while (true) 
            {
                if (client.s.isClosed()) 
                {
                    System.out.println("An error occured!");
                    System.exit(0);
                }        
                String data = in.readUTF();
                while (data != null  )
                {
                    if (data.equals("ENDMSG")) //the signal from the server that there is nothing more to be read for now
                    {
                        break;
                    }
                    System.out.println(data);
                    data = in.readUTF();
                }
                data = scanner.nextLine();
                out.writeUTF(data); 
            }
        } catch (IOException e) {
            System.out.println("Connection terminated!");
        }
    }
}
