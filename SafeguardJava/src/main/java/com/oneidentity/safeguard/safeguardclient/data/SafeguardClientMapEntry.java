package com.oneidentity.safeguard.safeguardclient.data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

public class SafeguardClientMapEntry {
	@XmlAttribute
	public String key;

	@XmlValue
	public String value;
}