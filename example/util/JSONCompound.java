package util;

import java.util.HashMap;

public class JSONCompound {

	private HashMap<String, Object> data = new HashMap<>();
	
	public JSONCompound put(String key, Object value){
		data.put(key, value);
		return this;
	}
	
	public boolean isSet(String key){
		return data.containsKey(key);
	}
	
	public Object get(String key){
		return data.get(key);
	}
}
