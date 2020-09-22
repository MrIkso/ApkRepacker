package com.mrikso.apkrepacker.autotranslator.browser;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class WebBrowser {
	private MyCookieStore cookieStore = new MyCookieStore();
	HttpHost proxy = new HttpHost("xxx", 8080, "http");
	private boolean useProxy = false;
	private boolean useSSL = false;

	private String userAgent = null;

	public WebBrowser(String userAgent) {
		this.userAgent = userAgent;
	}

	public WebBrowser(boolean paramBoolean) {
		this.useSSL = paramBoolean;
	}

	private DefaultHttpClient getHttpClient() {
		if (this.useSSL)
			return SSLSocketFactoryEx.getNewHttpClient();
		return new DefaultHttpClient();
	}

	private String getString(InputStream input) {
		int bufferSize = 256 * 1024;
		int readSize = 0;

		try {
			byte[] buffer = new byte[bufferSize];
			int maxStrSize = bufferSize - 1;
			while (readSize < maxStrSize) {
				int ret = input.read(buffer, readSize, maxStrSize - readSize);
				if (ret <= 0) {
					break;
				}
				readSize += ret;
			}

			if (readSize > 0) {
				return new String(buffer, 0, readSize, StandardCharsets.UTF_8);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "";
	}

	public String get(String strUrl, String referUrl) {
		String str = null;
		try {
			HttpGet httpGet = new HttpGet(strUrl);
			httpGet.getParams().setParameter("http.protocol.cookie-policy", "compatibility");
			DefaultHttpClient httpClient = getHttpClient();
			if (userAgent != null)
				httpGet.addHeader("User-Agent", userAgent);
			if (referUrl != null) {
				httpGet.addHeader("Referer", referUrl);
			}
			MyCookieStore cookieStore = this.cookieStore;
			if (cookieStore != null) {
				httpClient.setCookieStore(this.cookieStore);
			}
			if (this.useProxy) {
				httpClient.getParams().setParameter("http.route.default-proxy", this.proxy);
			}
			httpClient.getParams().setParameter("http.connection.timeout", 15000);
			httpClient.getParams().setParameter("http.socket.timeout", 15000);
			str = getString(httpClient.execute(httpGet).getEntity().getContent());
			// Debug.dump(tag + ".html", str);
			List<Cookie> newCookies = httpClient.getCookieStore().getCookies();
			this.cookieStore.addCookies(newCookies);
			return str;
		} catch (Exception e) {
			e.printStackTrace();
			//Debug.dump(tag + ".error", e.getMessage());
		}
		return null;
	}

}
