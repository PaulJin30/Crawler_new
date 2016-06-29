package paul.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mobi {
	static String apkname = null, category = null, filename = null, uptime = null, language = null, heat = null,
			version = null, size = null, apkpage = null, apkaddress = null, oldversion = null, topic_id = null;
	static int line_no = 0, total_comments = 0;

	public static void main(String args[]) throws Exception, FileNotFoundException {
		
		FileOutputStream debugger = new FileOutputStream("E:\\Desktop\\Debugger.txt");
		MultiOutputStream multi = new MultiOutputStream(new PrintStream(debugger), System.out);
		System.setOut(new PrintStream(multi));
		
		String line;
		int i = 1;
		BufferedReader reader1 = new BufferedReader(
				new InputStreamReader(new FileInputStream("E:\\Desktop\\hiapk.csv"), "UTF-8"));

		while ((line = reader1.readLine()) != null) {
			URL url = new URL(line);
			try {
				HtmlDownloader.download("E:\\Desktop\\LAB\\games\\" + i + ".txt", url);
			} catch (SocketTimeoutException ex1) {
				System.out.println(ex1);
				FileWriter fw = new FileWriter("E:\\Desktop\\LAB\\games\\" + i + ".txt");
				fw.write("0");
				fw.close();
			}
			i++;
		}
		reader1.close();

		for (i = 1; i < 551; i++) {
			Mobi.createlist("E:\\Desktop\\LAB\\games\\" + i + ".txt", "E:\\Desktop\\LAB\\Mobilist_game.csv");
		}

		BufferedReader reader2 = new BufferedReader(
				new InputStreamReader(new FileInputStream("E:\\Desktop\\LAB\\Mobilist_game.csv")));
		Pattern p = Pattern.compile("([ \\S]+?),([ \\S]+)");
		FileWriter fw1 = new FileWriter("E:\\Desktop\\LAB\\APK\\list.csv");
		while ((line = reader2.readLine()) != null) {
			Matcher m = p.matcher(line);
			if (m.find()) {
				apkname = m.group(1);
				filename = m.group(2);
				apkpage = "http://apk.hiapk.com/appinfo/" + filename;
				apkaddress = "http://apk.hiapk.com/appdown/" + filename;
				Mobi.matching();
				// System.out.println(line_no);
			}

			if (line_no != 0) {
				fw1.write(line_no + "," + apkname + "," + filename + "\n");

				File apfile = new File("E:\\Desktop\\LAB\\Comments\\" + filename + ".csv");
				if (apfile.exists()) {
					continue;
				}
				apfile.mkdir();
				URL url = new URL(apkaddress);
				try {
					ApkDownloader.download("E:\\Desktop\\LAB\\APK\\" + category + "\\" + filename, filename + ".apk",
							url);
				} catch (Exception ex2) {
					System.out.println(ex2 + filename);
				} catch (OutOfMemoryError ex3) {
					System.out.println(ex3 + filename);
				}
				Mobi.collectinfo();
			}
		}
		Mobi.inforewrite("E:\\Desktop\\LAB\\info_mobi.csv");
		fw1.close();
		reader2.close();
	}

	private static void matching() throws IOException {
		System.out.println("Matching   " + apkname);
		@SuppressWarnings("resource")
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream("E:\\Desktop\\LAB\\info.csv")));
		String line;
		line_no = 0;
		Pattern p = Pattern.compile("^([ \\S]+?),(\\S+?),");
		while ((line = reader.readLine()) != null) {
			Matcher m = p.matcher(line);
			m.find();
			if (m.group(1).contains(apkname)) {
				category = m.group(2);
				return;
			}
			line_no++;
		}
		line_no = 0;
	}

	private static void collectinfo() throws Exception {
		FileWriter fw2 = new FileWriter("E:\\Desktop\\LAB\\info_mobigame.csv", true);
		uptime = null;
		language = null;
		heat = null;
		version = null;
		size = null;
		oldversion = null;
		total_comments = 0;
		topic_id = null;

		int[] flag = { 0, 0, 0, 0, 0 };
		int beginIndex = 0, endIndex = 0;

		URL url = new URL(apkpage);
		try {
			HtmlDownloader.download("E:\\Desktop\\LAB\\test\\" + apkname + ".txt", url);
		} catch (Exception ex1) {
			System.out.println(ex1);
			FileWriter erf = new FileWriter("E:\\Desktop\\LAB\\test\\" + apkname + ".txt");
			erf.write(0);
			erf.close();
			fw2.close();
			return;
		}

		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream("E:\\Desktop\\LAB\\test\\" + apkname + ".txt")));
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.contains("热度")) {
				flag[0] = 1;
			}
			if (line.contains("语言")) {
				flag[1] = 1;
				continue;
			}
			if (line.contains("上架时间")) {
				flag[2] = 1;
				continue;
			}
			if (line.contains("appSoftName")) {
				flag[3] = 1;
				continue;
			}
			if (line.contains("其他版本")) {
				flag[4] = 1;
				continue;
			}
			if (line.contains("<div class=\"footer\">")) {
				flag[4] = 0;
			}
			if ((line.contains("<span class=\"font14\">")) & (flag[0] == 1)) {
				beginIndex = line.indexOf(">");
				endIndex = line.indexOf("<", beginIndex);
				heat = line.substring(beginIndex + 1, endIndex);
				flag[0] = 0;
			}
			if ((line.contains("<span class=\"font14\">")) & (flag[1] == 1)) {
				beginIndex = line.indexOf(">");
				endIndex = line.indexOf("<", beginIndex);
				language = line.substring(beginIndex + 1, endIndex);
				flag[1] = 0;
			}
			if ((line.contains("<span class=\"font14\">")) & (flag[2] == 1)) {
				beginIndex = line.indexOf(">");
				endIndex = line.indexOf("<", beginIndex);
				uptime = line.substring(beginIndex + 1, endIndex);
				flag[2] = 0;
			}
			if (flag[3] == 1) {
				beginIndex = line.indexOf("(");
				endIndex = line.indexOf(")", beginIndex);
				version = line.substring(beginIndex + 1, endIndex);
				flag[3] = 0;
			}
			if ((line.contains("<a href=")) & (flag[4] == 1)) {
				Pattern p = Pattern.compile("<a href=\"(\\S+?)\" rel=\"nofollow\">V(\\S+?)</a> ");
				Matcher m = p.matcher(line);
				m.find();
				if (oldversion == null) {
					oldversion = m.group(2) + ":" + m.group(1) + "|";
				} else {
					oldversion = oldversion + m.group(2) + ":" + m.group(1) + "|";
				}
			}
			if (line.contains("appSize")) {
				beginIndex = line.indexOf(">");
				endIndex = line.indexOf("<", beginIndex);
				size = line.substring(beginIndex + 1, endIndex);
			}
			if (line.contains("hidAppId")) {
				beginIndex = line.indexOf("value");
				endIndex = line.indexOf("\"", beginIndex + 7);
				topic_id = line.substring(beginIndex + 7, endIndex);
			}
		}
		fw2.write(line_no + "," + apkname + "," + uptime + "," + language + "," + version + "," + size + "," + heat
				+ "," + filename + "," + topic_id + "," + oldversion + "," + "\n");
		fw2.close();
		// System.out.println(apkname+","+uptime+","+language+","+version+","+size+","+heat+","
		// +filename+","+topic_id+","+oldversion+","+"\n");
		reader.close();
		Mobi.get_comment();
		System.out.println(apkname + "  info created\n");
	}

	private static void get_comment() throws Exception {
		FileWriter fw = new FileWriter("E:\\Desktop\\LAB\\Comments\\" + filename + ".csv");
		int i = 1;
		int j;
		URL url = new URL("http://apk.hiapk.com/web/api.do?" + "qt=1701&id=" + topic_id + "&pi=" + i + "&ps=10");
		try {
			HtmlDownloader.download("E:\\Desktop\\LAB\\comtemp\\" + filename + i + ".txt", url);
		} catch (Exception ex1) {
			System.out.println(ex1);
			fw.close();
			return;
		}
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream("E:\\Desktop\\LAB\\comtemp\\" + filename + i + ".txt")));
		String line;
		while ((line = reader.readLine()) != null) {
			// System.out.println(line);
			if (line.contains("content")) {
				Pattern p = Pattern.compile("content\":\"(.+?)\",\"cttype");
				Matcher m = p.matcher(line);
				while (m.find()) {
					// System.out.println(m.group(1)+"\n");
					fw.write(m.group(1) + "\n");
				}
			}
			if (line.contains("total")) {
				Pattern p = Pattern.compile("total\":(\\d+)");
				Matcher m = p.matcher(line);
				while (m.find()) {
					total_comments = Integer.valueOf(m.group(1));
				}
			}
			if (total_comments < 11) {
				reader.close();
				fw.close();
				return;
			} else if (total_comments > 1000) {
				i = 100;
			} else {
				i = total_comments / 10 + 1;
			}
		}
		for (j = 2; j < i + 1; j++) {
			url = new URL("http://apk.hiapk.com/web/api.do?" + "qt=1701&id=" + topic_id + "&pi=" + j + "&ps=10");
			try {
				HtmlDownloader.download("E:\\Desktop\\LAB\\comtemp\\" + filename + j + ".txt", url);
				Thread.sleep(1000);
			} catch (Exception ex2) {
				System.out.println(ex2);
				FileWriter erf = new FileWriter("E:\\Desktop\\LAB\\comtemp\\" + filename + j + ".txt");
				erf.write(0);
				erf.close();
			}
		}
		for (j = 2; j < i + 1; j++) {
			reader = new BufferedReader(
					new InputStreamReader(new FileInputStream("E:\\Desktop\\LAB\\comtemp\\" + filename + j + ".txt")));
			while ((line = reader.readLine()) != null) {
				if (line.contains("content")) {
					Pattern p = Pattern.compile("content\":\"(.+?)\",\"cttype");
					Matcher m = p.matcher(line);
					while (m.find()) {
						fw.write(m.group(1) + "\n");
					}
				}
			}
		}
		fw.close();
		reader.close();
		System.out.println(apkname + "  comments created\n");
	}

	private static void inforewrite(String infopath) throws Exception {
		line_no = 0;
		FileWriter fw = new FileWriter("E:\\Desktop\\LAB\\info_re.csv");
		int i = 0;
		int[] flag = { 0, 0 };
		BufferedReader reader1 = new BufferedReader(
				new InputStreamReader(new FileInputStream("E:\\Desktop\\LAB\\info.csv")));
		String line1;
		BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(infopath)));
		String line2;

		line1 = reader1.readLine();
		fw.write(line1 + "\n");
		while ((line2 = reader2.readLine()) != null) {
			Pattern p = Pattern.compile("(\\d+?),(\\S+)");
			Matcher m = p.matcher(line2);
			m.find();
			if (line_no == Integer.valueOf(m.group(1))) {
				flag[1] = 1;
			} else {
				flag[1] = 0;
				line_no = Integer.valueOf(m.group(1));
				line1 = reader1.readLine();
				i++;
			}
			System.out.println(line_no + "\n");
			while (true) {
				System.out.println(i + "\t" + line_no + "\n");
				if (i == line_no) {
					System.out.println("hi\n");
					flag[0] = 1;
				}
				if (flag[1] == 0) {
					fw.write(line1 + "\n");
				}
				if (flag[0] == 1) {
					fw.write("," + m.group(2) + "\n");
					flag[0] = 0;
					break;
				}
				i++;
				if ((line1 = reader1.readLine()) == null) {
					break;
				}
			}
		}
		reader2.close();
		reader1.close();
		fw.close();
	}

	private static void createlist(String filepath, String savepath) throws Exception, FileNotFoundException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filepath)));
		String line;
		FileWriter filewriter = new FileWriter(savepath);
		Pattern p = Pattern
				.compile("<span class=\"list_title font14_2\"><a href=\"\\/appinfo\\/(\\S+?)\">(\\S+?)</a> </span>");
		while ((line = reader.readLine()) != null) {
			Matcher m = p.matcher(line);
			while (m.find()) {
				filewriter.write(m.group(2) + "," + m.group(1) + "\n");
			}
		}
		reader.close();
		filewriter.close();
	}
}