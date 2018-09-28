package com.oneidentity.safeguard.safeguardclient.data;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SafeguardClientListItem {

    private List<SafeguardClientItem> items;
//    private SCBNavMetadata meta = null;

    public SafeguardClientListItem() {
    }

    public List<SafeguardClientItem> getItems() {
        return items;
    }

    public void setItems(List<SafeguardClientItem> items) {
        this.items = items;
    }

//    public SCBNavMetadata getMeta() {
//        if (meta == null) {
//            meta = new SCBNavMetadata();
//        }
//        return meta;
//    }
//
//    public void setMeta(SCBNavMetadata meta) {
//        this.meta = meta;
//    }
}
