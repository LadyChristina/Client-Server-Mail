package mailserver;


import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MailServer extends Thread{

    private List <Account> accounts; //a list of all stored accounts
    private Account thisAccount; //the current user
    private DataInputStream in;
    private DataOutputStream out;
    private Socket clientSocket;
    
    public MailServer(Socket aClientSocket)
    {
        try 
        {            
            accounts = new ArrayList<>();
            thisAccount = null;
            clientSocket = aClientSocket;
            in = new DataInputStream( clientSocket.getInputStream());
            out =new DataOutputStream( clientSocket.getOutputStream());
            this.start();
        } catch(IOException e) {System.out.println("Connection:"+e.getMessage());}
    }
    
    private synchronized void register() throws IOException //this method is synchronized to make sure that no two users will register at the same time with the same username
    {
        String username, password; 
        boolean flag;
        do
        {
            flag = false;
            out.writeUTF("Enter a username or 'back' to go back");
            out.writeUTF("ENDMSG");
            username = in.readUTF();
            while (username.equals("back"))
            {
                return;
            }
            for (Account acc : accounts)
            {
                if (acc.getUsername().equals(username))
                {
                    out.writeUTF("This username is taken! Please choose another one!");
                    out.writeUTF("ENDMSG");
                    flag = true;
                    break;
                }
            }      
        }while (flag == true);
        do
        {
            flag = false;
            out.writeUTF("Enter password or 'back' to go back:");
            out.writeUTF("ENDMSG");
            password = in.readUTF();
            if (password.equals("back"))
            {
                return;
            }
            if (password.isEmpty())
            {
                out.writeUTF("Please enter a valid password!");
                flag = true;            
            }            
        }while (flag == true);
        Account acc = new Account (username, password);
        accounts.add(acc);
        out.writeUTF("Account created!");
        return;
    }
    
    private void logIn() throws IOException
    {
        String username,password;
        boolean flag;
        do
        {
            flag = true;            
            out.writeUTF("Enter your username or 'back' to go back:");      
            out.writeUTF("ENDMSG");
            username = in.readUTF();
            if (username.equals("back"))
            {
                return;
            }
            for (Account acc : accounts)
            {
                if (acc.getUsername().equals(username))
                {
                    //we found the user
                    this.thisAccount = acc; //we reference the user
                    flag = false;
                    break;
                }
            }
            if (flag==true)
            {
                out.writeUTF("This username does not exist!");
            }
        }while (flag==true);
        do
        {
            flag = false;
            out.writeUTF("Enter your password or 'back' to go back: ");
            out.writeUTF("ENDMSG");
            password = in.readUTF();
            if (password.equals("back"))
            {
                this.thisAccount=null; //user did not log in after all
                return;
            }
            if (!(this.thisAccount.getPassword().equals(password))) //we check if the password is correct
            {
                //the password is not correct
                flag = true;
                out.writeUTF("Wrong password!");
            }            
        }while (flag==true);   
        //the password is correct
        out.writeUTF("Successfull login \nWelcome " + this.thisAccount.getUsername() + "!");
        return;
    }
    
    private void newEmail() throws IOException
    {
        if (this.thisAccount==null)
        {
            //no user is logged in
            out.writeUTF("Please enter a valid command! ");
            return;            
        }
        Email email = new Email();   
        Account receiver = new Account();
        email.setSender(this.thisAccount.getUsername());
        boolean flag;
        do
        {
            flag=true;
            out.writeUTF("Enter the username of the recipient or 'back' to go back: "); //back is a reserved word
            out.writeUTF("ENDMSG");
            String username;
            username = in.readUTF();
            if (username.equals("back"))
            {
                return;
            }
            for (Account acc : accounts)
            {
                if (acc.getUsername().equals(username)) //we check if given username exists
                {
                    //we found the recipient's username in our stored accounts
                    flag = false;
                    email.setReceiver(username);
                    receiver = acc; //a reference to the receiver in order to put the new mail in his mailbox
                    break;
                }
            }
            if (flag==true)
            {
                out.writeUTF("There is no such user in our network!");
            }
        }while (flag==true);
        out.writeUTF("Enter the subject of the email or 'back' to abort: ");
        out.writeUTF("ENDMSG");
        String subject;
        subject = in.readUTF();
        if (subject.equals("back"))
        {
            return;
        }
        if (subject.isEmpty()) //we make sure that the user entered a subject
        {
            out.writeUTF("Are you sure you want to send an email without a subject?");
            out.writeUTF("Press 'n' to re-enter the subject or any other key to proceed.");
            out.writeUTF("ENDMSG");
            if (in.readUTF().charAt(0)=='n')
            {
                out.writeUTF("Enter the subject of the email: ");
                out.writeUTF("ENDMSG");
                subject=in.readUTF(); //this time we assume that the user entered a subject or doesn't want to enter one
            }            
        }
        email.setSubject(subject);
        out.writeUTF("Enter the main body of the email or 'back' to abort: ");
        out.writeUTF("ENDMSG");
        String mainbody;
        mainbody = in.readUTF(); 
        if (mainbody.equals("back"))
        {
            return;
        }
        if (mainbody.isEmpty()) //we make sure that the user entered a message
        {
            out.writeUTF("Are you sure you want to send an email without a main body?");
            out.writeUTF("Press 'n' to re-enter the main body or any other key to proceed.");
            out.writeUTF("ENDMSG");
            if (in.readUTF().charAt(0) == 'n');
            {
                out.writeUTF("Enter the main body of the email: ");
                out.writeUTF("ENDMSG");
                mainbody=in.readUTF();; //this time we assume that the user entered a message or doesn't care to enter one
            }            
        }
        email.setMainbody(mainbody);
        out.writeUTF("Do you want to send the email now?");
        out.writeUTF("Press 's' to send or any other key to abort.");
        out.writeUTF("ENDMSG");
        if (in.readUTF().charAt(0)=='s')
        {
            //send email
            receiver.getMailbox().add(email);
            out.writeUTF("Email sent!");
        }
        return;
    }
    
    private void showEmails() throws IOException
    {
        if (this.thisAccount==null)
        {
            //no user is logged in
            out.writeUTF("Please enter a valid command! ");
            return;            
        }
        ArrayList<Email> mailbox = new ArrayList<>(this.thisAccount.getMailbox());
        if (this.thisAccount.getMailbox().isEmpty())
        {
            out.writeUTF("No emails to show!");         
        }
        else
        {
         out.writeUTF("Id\t\tFrom\tSubject");
            int id = 1;
            String isNew = null;
            for (Email email : mailbox )
            {
                if (email.getIsNew())
                {
                    isNew = "[New]";
                }
                else
                {
                    isNew = "";
                }
                out.writeUTF(id + "\t" + isNew + "\t" + email.getSender() + "\t" + email.getSubject());
                id++;   
            }
        }
        return;
    }
    
    private void readEmail() throws IOException
    {
        if (this.thisAccount==null)
        {
            //no user is logged in
            out.writeUTF("Please enter a valid command! ");
            return;            
        }
        if (this.thisAccount.getMailbox().isEmpty())
        {
            out.writeUTF("No emails to be read!");
            return;
        }
        out.writeUTF("Enter the Id of the email you wish to read or 0 to go back: ");
        out.writeUTF("ENDMSG");
        //int id = in.readInt();
        String input = in.readUTF();
        int id = Integer.parseInt(input);
        if (id==0)
        {
            return;
        }
        while (id > this.thisAccount.getMailbox().size()) 
        {
            out.writeUTF("There is no email with such an Id!");
            out.writeUTF("Enter the Id of the email you wish to read or 0 to go back: ");
            out.writeUTF("ENDMSG");
            id = in.readInt();
            if (id==0)
            {
                return;
            }
        }
        int count=1;
        //we search for the email with the given id
        for (Email email : this.thisAccount.getMailbox())
        {
            if (count == id)
            {
                //we found the requested email
                out.writeUTF("From: " + email.getSender() + "\nSubject: " + email.getSubject() + "\nMain Body: " + email.getMainBody() );
                email.setIsNew(false);//the read email is no longer qualified as new
                break;
            }
            else
            {
                count++;
            }
        }
        return;
    }
    
    private void deleteEmail() throws IOException
    {
        if (this.thisAccount==null)
        {
            //no user is logged in
            out.writeUTF("Please enter a valid command! ");
            return;            
        }
        if (this.thisAccount.getMailbox().isEmpty())
        {
            out.writeUTF("No emails to be deleted !");
            return;
        }
        out.writeUTF("Enter the Id of the email you wish to delete or 0 to go back: ");
        out.writeUTF("ENDMSG");
        String input = in.readUTF();
        int id = Integer.parseInt(input);
        //int id = in.readInt(); 
        if (id==0)
        {
            return;
        }
        while (id > this.thisAccount.getMailbox().size() || id<0) 
        {
            out.writeUTF("There is no email with such an Id!");
            out.writeUTF("Enter the Id of the email you wish to delete or 0 to go back: ");
            out.writeUTF("ENDMSG");
            id = in.readInt();
            if (id==0)
            {
                return;
            }
        }
        int count=1;
        //we search for the email with the given id
        for (Email email : this.thisAccount.getMailbox())
        {
            if (count == id)
            {
                //we found the requested email
                this.thisAccount.getMailbox().remove(count-1); 
                out.writeUTF("Email has been deleted!");
                break;
            }
            else
            {
                count++;
            }
        }
        return;
    }
    
    private void logOut() throws IOException
    {
        if (this.thisAccount==null)
        {
            //no user is logged in
            out.writeUTF("Please enter a valid command! ");
            return;            
        }        
        out.writeUTF("Bye bye " + this.thisAccount.getUsername() + "!");
        this.thisAccount = null;
    }
    
    private void exit() throws IOException
    {
        out.writeUTF("Goodbye!");
        //closing socket
        in.close();
        out.close();
        this.clientSocket.close();
    }
    
    public void menu() throws IOException
    {
        out.writeUTF("=============");
        if (this.thisAccount == null) // user is not logged in
        {
            out.writeUTF("> Login");
            out.writeUTF("> Sign up");
            out.writeUTF("> Exit");
        }
        else //user is logged in
        {
            out.writeUTF("> New email");
            out.writeUTF("> Show emails");
            out.writeUTF("> Read email");
            out.writeUTF("> Delete email");
            out.writeUTF("> Log out");
            out.writeUTF("> Exit");
        }
        out.writeUTF("=============");
    }
    
    public void loadAccounts()
    {
        Account acc1 = new Account("Christina", "123");
        Account acc2 = new Account ("Maria", "456");
        acc1.getMailbox().add(new Email("Maria","Christina","Hey","Hello there!"));
        acc1.getMailbox().add(new Email("Maria","Christina","Miss ya","Long time no see!"));
        acc1.getMailbox().add(new Email("Maria","Christina","sup?","How u doin?"));
        acc2.getMailbox().add(new Email("Christina","Maria","yo","yo man"));
        acc2.getMailbox().add(new Email("Christina","Maria","Dang!","Check this out!"));
        acc2.getMailbox().add(new Email("Christina","Maria","Hey","Hello to you too!"));
        this.accounts.add(acc1);
        this.accounts.add(acc2);
    }
    
    /**
     * @param args the command line argument = the server's port
     */
    public static void main(String[] args) throws IOException
    {
       if (args.length < 1) 
       {
            System.out.println("Please provide a port number for the server!");
            return;
       }
       try
       {
            int port = Integer.parseInt(args[0]);
            ServerSocket listenSocket  = new ServerSocket(port);
             while (true) 
             {
                 Socket clientSocket = listenSocket.accept();
                 System.out.println("Request from client " + clientSocket.getInetAddress()+" at port "+ clientSocket.getPort());				
                 MailServer server = new MailServer(clientSocket);
                 server.loadAccounts();
             }
       }catch(IOException e) {System.out.println("Listen socket:"+e.getMessage());}
    }

    @Override
    public void run() 
    {
        System.out.println("Hey, I am the server!");
        try {
            out.writeUTF("Hey client,this is your server speaking!");
        } catch (IOException ex) {
            System.out.println("An error occured!");
        }
        try 
        {
            while (true) 
            {
                menu();
                out.writeUTF("Enter command: ");   
                out.writeUTF("ENDMSG");
                String command = in.readUTF(); //we read the user's command 
                if (command.toLowerCase().equals("login")) //if the user types "Login" or "login" or "LogIn" etc (case insensitive)
                {
                    logIn();
                }
                else if (command.toLowerCase().equals("sign up") || command.toLowerCase().equals("signup")) //we check for all possibilities of user input
                {
                    register();
                }
                else if (command.toLowerCase().equals("new email") || command.toLowerCase().equals("newemail"))
                {
                    newEmail();
                }
                else if (command.toLowerCase().equals("show emails") || command.toLowerCase().equals("showemails"))
                {
                    showEmails();
                }
                else if (command.toLowerCase().equals("read email") || command.toLowerCase().equals("reademail"))
                {
                    readEmail();
                }
                else if (command.toLowerCase().equals("delete email") || command.toLowerCase().equals("deleteemail"))
                {
                    deleteEmail();
                }
                else if (command.toLowerCase().equals("log out") || command.toLowerCase().equals("logout"))
                {
                    logOut();
                }
                else if (command.toLowerCase().equals("exit"))
                {
                    exit();
                }
                else
                {
                    out.writeUTF("Please enter a valid command! ");
                }                    
            }

        } catch(IOException e) 
        {
            System.out.println("Connection Terminated");
            try {
                this.in.close();
            } catch (IOException ex) {
                System.out.println("An error occured!");;
                try {
                    this.out.close();
                } catch (IOException ex1) {
                    System.out.println("An error occured!");;
                    try {
                        this.clientSocket.close();
                    } catch (IOException ex2) {
                        System.out.println("An error occured!");;
                    }
                }
            }

        } finally{ try {clientSocket.close();}catch (IOException e){/*close failed*/}}


    }
    
}