package org.constellation.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Stack;

public class JSonUtils {

	public static void main(String[] args) {

		Properties list = new Properties();
		list.put("a", "bcde");
		list.put("ab.c", "de");
		list.put("ab.d", "de");

		Map<String, Object> p2h = JSonUtils.toJSon(list);
		System.out.println(p2h);

		Properties properties = JSonUtils.toProperties(p2h);
		
		System.out.println(properties);
	}

	public static Properties toProperties(Map<String, Object> map) {

		return addValue(new Properties(), new Stack<String>(), map);

		
	}

	private static Properties addValue(Properties properties, Stack<String> path,
			Object o) {
		if (o instanceof Map) {
			Map<String, Object> map = (Map<String, Object>) o;
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				path.push(entry.getKey());

				addValue(properties, path, entry.getValue());

				path.pop();
			}
		} else {
			StringBuilder builder = new StringBuilder();
			for (String string : path) {
				if (builder.length() > 0)
					builder.append('.');
				builder.append(string);
			}
			properties.put(builder.toString(), String.valueOf(o));
		}
		return properties;
	}

	public static Map<String, Object> toJSon(Properties list) {
		Map<String, Object> map = new HashMap<String, Object>();
		for (Entry<Object, Object> entry : list.entrySet()) {
			String key = (String) entry.getKey();
			String[] tokens = key.split("\\.");
			addValue(map, tokens, 0, (String) entry.getValue());
		}
		return map;

	}

	public static void addValue(Map<String, Object> map, String[] tokens,
			int i, String value) {
		String token = tokens[i];
		if (i == tokens.length - 1)
			map.put(token, value);
		else {
			Map<String, Object> o = (Map<String, Object>) map.get(token);
			if (o == null) {
				o = new HashMap<String, Object>();
				map.put(token, o);
			}
			addValue(o, tokens, i + 1, value);
		}
	}
}