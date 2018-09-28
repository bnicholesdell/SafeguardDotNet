package com.oneidentity.safeguard.safeguardclient.restclient;

import com.oneidentity.safeguard.safeguardclient.data.SafeguardClientMap;
import com.oneidentity.safeguard.safeguardclient.data.SafeguardClientMapEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class SafeguardClientMapAdapter extends XmlAdapter<SafeguardClientMap, Map<String, String>> {
	@Override
	public SafeguardClientMap marshal(Map<String, String> arg0) throws Exception {
		SafeguardClientMap myMapType = new SafeguardClientMap();
		for (Map.Entry<String, String> entry : arg0.entrySet()) {
			SafeguardClientMapEntry myMapEntryType = new SafeguardClientMapEntry();
			myMapEntryType.key = entry.getKey();
			myMapEntryType.value = entry.getValue();
                        List<SafeguardClientMapEntry> a = myMapType.entry;
			myMapType.entry.add(myMapEntryType);
		}
		return myMapType;
	}

	@Override
	public Map<String, String> unmarshal(SafeguardClientMap arg0) throws Exception {
		Map<String, String> hashMap = new HashMap<>();
		for (SafeguardClientMapEntry myEntryType : arg0.entry) {
			hashMap.put(myEntryType.key, myEntryType.value);
		}
		return hashMap;
	}
}