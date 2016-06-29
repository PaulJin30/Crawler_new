package paul.crawler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApkDownloader {

	public static void download(String path, String filename, URL url) throws Exception {
		System.out.println("info:" + url + " downloading");
		byte[] urlsource = getURLSource(url);
		File file = new File(path, filename);
		FileOutputStream filewriter = new FileOutputStream(file);
		filewriter.write(urlsource);
		filewriter.close();
		System.out.println("info:" + url + " download success");
	}

	public static byte[] getURLSource(URL url) throws Exception {
		HttpURLConnection connect = (HttpURLConnection) url.openConnection();
		connect.setRequestMethod("GET");
		connect.setConnectTimeout(5 * 1000);
		connect.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
		InputStream inputstream = connect.getInputStream();
		byte[] htmlSource = readInputStream(inputstream);
		return htmlSource;
	}

	public static byte[] readInputStream(InputStream inputstream) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = inputstream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		inputstream.close();
		return outStream.toByteArray();
	}

}