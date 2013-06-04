package com.change_vision.astah.extension.plugin.common;

public class NamespaceClass {
	public String namespace = "";
	public String clazz = "";

	public String getFullName() {
		if ("".equals(namespace)) {
			return clazz;
		}
		return String.format("%s::%s", namespace, clazz);
	}

	@Override
	public String toString() {
		return String.format("%s, %s", namespace, clazz);
	}
}
