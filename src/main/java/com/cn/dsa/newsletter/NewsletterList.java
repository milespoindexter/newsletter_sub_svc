package com.cn.dsa.newsletter;

import java.util.List;
import java.util.ArrayList;

import javax.xml.bind.annotation.*;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "NewsletterList")
public class NewsletterList {
    @XmlElement(name = "newsletter", required = true)
    private final List<NewsletterItem> newsltrs = new ArrayList<NewsletterItem>();
    public List<NewsletterItem> getNewsletters() {
        return this.newsltrs;
    }

    public int size() {
    	return newsltrs.size();
    }
}