package com.change_vision.astah.extension.plugin.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public abstract class AbstractApplicationUtil {
	private static final String PROPERTIES_NAME = "app.xml";

	protected Properties properties = new Properties();

	protected abstract String getAppFileDir();

	public AbstractApplicationUtil() throws IOException {
		super();
		this.load();
	}

	public void save() throws IOException {
		OutputStream os = null;
		try {
			os = new FileOutputStream(this.getApplicationProperties());
			this.properties.storeToXML(os, null, "UTF-8");
		} finally {
			if (os != null) {
				os.close();
			}
		}
	}

	public void load() throws IOException {
		File applicationProperties = this.getApplicationProperties();
		if (!applicationProperties.exists()) {
			return;
		}
		InputStream is = null;
		try {
			is = new FileInputStream(applicationProperties);
			this.properties.loadFromXML(is);
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	protected File getApplicationProperties() throws IOException {
		String home = System.getProperty("user.home");
		File appDir = new File(String.format("%s/%s", home, this.getAppFileDir()));
		if (!appDir.exists()) {
			appDir.mkdirs();
		}
		return new File(String.format("%s/%s", appDir.getCanonicalPath(), PROPERTIES_NAME));
	}
}
