/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
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