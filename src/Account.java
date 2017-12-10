package mailserver;

import java.util.ArrayList;
import java.util.List;

public class Account {
    
    private String username;
    private String password;
    private List <Email> mailbox;
    
    public Account(String name, String word)
    {
        username = name;
        password = word;
        mailbox = new ArrayList<>();
    }
    
    public Account()
    {
        username=null;
        password=null;
        //mailbox?
    }
    
    public String getUsername()
    {
        return username;
    }
    
    public String getPassword()
    {
        return password;
    }
    
    public List<Email> getMailbox() 
    {
        return mailbox;
    }
    
    public void setUsername(String username)
    {
        this.username=username;
    }
    
    public void setPassword(String password)
    {
        this.password=password;
    }
}

