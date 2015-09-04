package com.eebbk.amaptmp;

import java.io.Serializable;
import java.util.Map;

public class SerializableMap implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8258340268765780555L;
	
	private Map<String,String> maps;
	
	public SerializableMap(Map<String, String> maps){
		this.maps = maps;
	}
	
	public Map<String, String> getMaps() {
		return maps;
	}
	
}
