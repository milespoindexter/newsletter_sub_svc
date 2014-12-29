package com.cn.dsa.newsletter;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.sql.Timestamp;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.*;

import com.cn.dsa.common.*;

@XmlRootElement(name="newsletterEvent")
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder={"email", "newsletters", "name", "timestamp", "props"})
public class NewsletterEvent {

    //REQUIRED
    private String email;
    private List<NewsletterItem> newsletters;

    //OPTIONAL
    private String name;
    private String timestamp;

    private Map props;

    //constructor
    public NewsletterEvent() {
    }

    public void setEmail(String email) {
        this.email = email;
    }
    @XmlElement(name="email", required=true)
    public String getEmail() {
        return email;
    }   

    //getters / setters
    public void setNewsletters(List<NewsletterItem> newsletters) {
        this.newsletters = newsletters;
    }
    //@XmlJavaTypeAdapter(IdListAdapter.class)
    @XmlElement(name="newsletter", required=true)
    //@XmlElementWrapper( name="newsletters" )
    public List<NewsletterItem> getNewsletters() {
        return newsletters;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setTimestamp(String ts) {
        this.timestamp = ts;
    }
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setProps(Map props) {
        this.props = props;
    }
    @XmlJavaTypeAdapter(PropMapAdapter.class)
    @XmlElement(nillable=true, name="props")
    public Map getProps() {
        return props;
    }
    
}