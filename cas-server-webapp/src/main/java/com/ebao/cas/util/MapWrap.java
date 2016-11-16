package com.ebao.cas.util;

import java.util.HashMap;
import java.util.Map;

public class MapWrap {
	private HashMap<String,Object> map=new HashMap<>();
	public static MapWrap newInstance(){
		return new MapWrap();
	}
	
	public MapWrap put(String key,Object value)
	{
		map.put(key, value);
		return this;
	}
	
	public Map<String,Object> toMap(){
		return map;
	}

	
	public HashMap<String,Object> toHashMap(){
		return map;
	}
	
}
