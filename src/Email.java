package mailserver;

public class Email {
    
    private boolean isNew;
    private String sender;
    private String receiver;
    private String subject;
    private String mainbody;
    
    public Email(String s, String r,String sub, String mb)
    {
        sender = s;
        receiver = r;
        subject = sub;
        mainbody = mb;
        isNew=true;
        
    }
    
    public Email()
    {
        sender=null;
        receiver=null;
        subject=null;
        mainbody=null;
        isNew=true;
    }
    
    public boolean getIsNew ()
    {
        return isNew;
    }
    
    public String getSender()
    {
        return sender;
    }
    
    public String getReceiver()
    {
        return receiver;
    }
    
    public String getSubject()
    {
        return subject;
    }
    
    public String getMainBody()
    {
        return mainbody;
    }
    
    public void setIsNew(boolean isnew)
    {
        isNew = isnew;
    }
    
    public void setSender(String sender)
    {
        this.sender = sender;
    }
    
    public void setReceiver(String receiver)
    {
        this.receiver=receiver;
    }
    
    public void setSubject(String subject)
    {
        this.subject=subject;
    }
    
    public void setMainbody(String mainbody)
    {
        this.mainbody=mainbody;
    }
}

