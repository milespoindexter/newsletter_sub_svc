package com.cn.dsa.newsletter;

import java.util.List;
import java.util.ArrayList;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public final class NewsletterListAdapter extends XmlAdapter<NewsletterList, List<NewsletterItem>> {

    @Override
    public NewsletterList marshal(List<NewsletterItem> newsltrs) {
        NewsletterList newsltrList = new NewsletterList();
        List<NewsletterItem> nList = newsltrList.getNewsletters();
        if(newsltrs != null && newsltrs.size() > 0) {
            for ( NewsletterItem newsltr : newsltrs ) {
                nList.add(newsltr);
            }
        }
        return newsltrList;
    }

    @Override
    public List<NewsletterItem> unmarshal(NewsletterList newsltrList) {
        List<NewsletterItem> newsltrs = new ArrayList<NewsletterItem>();
        if(newsltrList != null && newsltrList.size() > 0) {
            for ( NewsletterItem newsltr : newsltrList.getNewsletters() ) {
                newsltrs.add(newsltr);
            }
        }
        return newsltrs;
    }
    
}