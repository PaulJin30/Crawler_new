package paul.crawler;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.FileWriter;

public class HtmlDownloader {

	public static void download(String path, URL url) throws Exception {
		String urlsource = getURLSource(url);
		FileWriter filewriter = new FileWriter(path,true);
		filewriter.write(urlsource);
		filewriter.close();
		System.out.println("info:" + url + " download success");
	}

	public static String getURLSource(URL url) throws Exception {
		HttpURLConnection connect = (HttpURLConnection) url.openConnection();
		connect.setRequestMethod("GET");
		connect.setConnectTimeout(5 * 1000);
		connect.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
		InputStream inputstream = connect.getInputStream();
		String htmlSource = readInputStream(inputstream);
		return htmlSource;
	}

	public static String readInputStream(InputStream inputstream) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = inputstream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		inputstream.close();
		return outStream.toString("UTF-8");
	}

}