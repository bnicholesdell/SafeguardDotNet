package com.oneidentity.safeguard.safeguardclient.data;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SafeguardClientItem {

    private String key;
//    private SCBNavHRef meta;
    
    public SafeguardClientItem() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

//    public SCBNavHRef getMeta() {
//        return meta;
//    }
//
//    public void setMeta(SCBNavHRef meta) {
//        this.meta = meta;
//    }

}
