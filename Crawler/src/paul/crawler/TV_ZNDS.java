package paul.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TV_ZNDS {

	static ArrayList<String> list_address = new ArrayList<String>(200);// 用于存放各个类别下每一页的地址
	static ArrayList<String> local_list_webpage = new ArrayList<String>(200); // 用于存放上述每页的网页信息
	static int capacity;
	static int[] cnt = new int[6];
	static String[] category = new String[] { "media", "education", "life", "tools", "entertainment", "games" };
	static String apkname = null, uptime = null, language = null, device = null, version = null, size = null,
			downtime = null, apkaddress = null, oldversion = null, intro = null, total_page_no = null, topic_id = null;

	public static void main(String[] args) throws Exception {
 
		// 创建目录，设置地址
		String root = new String("E:\\Desktop");
		TV_ZNDS.initial(root);
		// 设置输出
		FileOutputStream debugger = new FileOutputStream(root + "\\Crawled\\Debugger.txt");
		MultiOutputStream multi = new MultiOutputStream(new PrintStream(debugger), System.out);
		System.setOut(new PrintStream(multi));

		int i;
		File temp = new File(root + "\\Crawled\\info.csv");
		temp.createNewFile();
		FileWriter filewriter = new FileWriter(root + "\\Crawled\\info.csv", true);
		filewriter.write("软件名称,类型,更新时间,软件语言,操控设备,软件版本,软件大小,下载量," + "软件地址,旧版本,软件简介\n");
		filewriter.close();

		for (i = 0; i < list_address.size(); i++) {
			URL url_temp = new URL(TV_ZNDS.list_address.get(i));
			HtmlDownloader.download(root + "\\Crawled\\html\\" + TV_ZNDS.local_list_webpage.get(i), url_temp);

			// 从类别页面提取APK的页面地址，生成对应类别的地址列表
			if (i < cnt[0]) {
				TV_ZNDS.createlist(root + "\\Crawled\\html\\" + TV_ZNDS.local_list_webpage.get(i),
						root + "\\Crawled\\html\\list0.txt");
			}
			else if (i >= cnt[0] && i < cnt[1]+cnt[0]) {
				TV_ZNDS.createlist(root + "\\Crawled\\html\\" + TV_ZNDS.local_list_webpage.get(i),
						root + "\\Crawled\\html\\list1.txt");
			}
			else if (i >= cnt[1]+cnt[0] && i < cnt[2]+cnt[1]+cnt[0]) {
				TV_ZNDS.createlist(root + "\\Crawled\\html\\" + TV_ZNDS.local_list_webpage.get(i),
						root + "\\Crawled\\html\\list2.txt");
			}
			else if (i >= cnt[2]+cnt[1]+cnt[0] && i < cnt[3]+cnt[2]+cnt[1]+cnt[0]) {
				TV_ZNDS.createlist(root + "\\Crawled\\html\\" + TV_ZNDS.local_list_webpage.get(i),
						root + "\\Crawled\\html\\list3.txt");
			}
			else if (i >= cnt[3]+cnt[2]+cnt[1]+cnt[0] && i < cnt[4]+cnt[3]+cnt[2]+cnt[1]+cnt[0]) {
				TV_ZNDS.createlist(root + "\\Crawled\\html\\" + TV_ZNDS.local_list_webpage.get(i),
						root + "\\Crawled\\html\\list4.txt");
			}
			else if (i >= cnt[4]+cnt[3]+cnt[2]+cnt[1]+cnt[0] && i<list_address.size()) {
				TV_ZNDS.createlist(root + "\\Crawled\\html\\" + TV_ZNDS.local_list_webpage.get(i),
						root + "\\Crawled\\html\\list5.txt");
			}
		}

		for (i = 0; i < 6; i++) { // 循环读取每个类别已经生成的地址列表
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(root + "\\Crawled\\html\\list" + i + ".txt"), "UTF-8"));
			String line;
			int j = 0;

			while ((line = reader.readLine()) != null) { // 读入某一个APK页面地址
				URL url1 = new URL(line);

				// 下载该页面
				try {
					HtmlDownloader.download(root + "\\Crawled\\" + TV_ZNDS.category[i] + "\\html\\" + j + ".txt", url1);
				} catch (SocketTimeoutException ex1) {
					System.out.println(ex1);
					FileWriter fw = new FileWriter(root + "\\Crawled\\" + TV_ZNDS.category[i] + "\\html\\" + j + ".txt");
					fw.write("0");
					fw.close();
				}

				// 提取信息
				try {
					TV_ZNDS.getinfo(root + "\\Crawled\\" + TV_ZNDS.category[i] + "\\html\\" + j + ".txt",
							root + "\\Crawled\\info.csv", i);
				} catch (StringIndexOutOfBoundsException ex2) {
					System.out.println(ex2);
				}

				// 获取评论
				try {
					TV_ZNDS.getcomments(url1, root + "\\Crawled\\" + TV_ZNDS.category[i] + "\\html\\" + j,
						root + "\\Crawled\\" + TV_ZNDS.category[i] + "\\comments\\");
				} catch (FileNotFoundException ex3) {
					System.out.println(ex3);
				}
				/*
				 * //下载APK try { TV_ZNDS.download(root+"\\Crawled\\" +
				 * TV_ZNDS.category[i]); } catch (Exception ex3) {
				 * System.out.println(ex3); }
				 */
				j++;
			}
			reader.close();
		}
		System.out.println("Finished");
		System.exit(0);
	}

	public static void initial(String root) throws Exception {
		File file1 = new File(root + "\\Crawled\\html");
		file1.mkdirs();
		for (int i = 0; i < 6; i++) {
			File file2 = new File(root + "\\Crawled\\" + category[i] + "\\html");
			file2.mkdirs();
			File file3 = new File(root + "\\Crawled\\" + category[i] + "\\comments");
			file3.mkdirs();
		}
		String[] temp = { "http://down.znds.com/apk/TV_ZNDS/", "http://down.znds.com/apk/xuexi/",
				"http://down.znds.com/apk/life/", "http://down.znds.com/apk/tool/", "http://down.znds.com/apk/home/",
				"http://down.znds.com/apk/game/" };

		for (int i = 0; i < 6; i++) {
			URL url = new URL(temp[i]);
			HtmlDownloader.download(root + "\\Crawled\\html\\" + category[i] + ".txt", url);
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(root + "\\Crawled\\html\\" + category[i] + ".txt")));
			String line;
			while ((line = reader.readLine()) != null) {
				// System.out.println(line);
				Pattern p = Pattern.compile("共 <strong>(\\d+)</strong>页");
				Matcher m = p.matcher(line);
				while (m.find()) {
					cnt[i] = Integer.valueOf(m.group(1));
					// System.out.println(cnt[i]);
				}
				if (cnt[i] != 0) {
					reader.close();
					break;
				}
			}
			capacity = capacity + cnt[i];// 统计页面个数
		}

		int i;
		for (i = 1; i <= cnt[0]; i++) {
			list_address.add("http://down.znds.com/apk/TV_ZNDS/list_7_" + i + ".html"); // 影音
			local_list_webpage.add("TV_ZNDS_" + i + ".txt");
			System.out.println(i + "\n");
		}
		for (i = 1; i <= cnt[1]; i++) {
			list_address.add("http://down.znds.com/apk/xuexi/list_5_" + i + ".html"); // 教育
			local_list_webpage.add("xuexi_" + i + ".txt");
		}
		for (i = 1; i <= cnt[2]; i++) {
			list_address.add("http://down.znds.com/apk/life/list_4_" + i + ".html"); // 生活
			local_list_webpage.add("life_" + i + ".txt");
		}
		for (i = 1; i <= cnt[3]; i++) {
			list_address.add("http://down.znds.com/apk/tool/list_6_" + i + ".html"); // 工具
			local_list_webpage.add("tool_" + i + ".txt");
		}
		for (i = 1; i <= cnt[4]; i++) {
			list_address.add("http://down.znds.com/apk/home/list_3_" + i + ".html"); // 娱乐
			local_list_webpage.add("home_" + i + ".txt");
		}
		for (i = 1; i <= cnt[5]; i++) {
			list_address.add("http://down.znds.com/apk/game/list_2_" + i + ".html"); // 游戏
			local_list_webpage.add("game_" + i + ".txt");
		}
	}

	public static void createlist(String in_path, String save_path) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(in_path), "UTF-8"));
		String line;
		File temp = new File(save_path);
		temp.createNewFile();
		FileWriter filewriter = new FileWriter(save_path, true);

		while ((line = reader.readLine()) != null) {
			if (line.contains("h2><a href=")) {
				int beginIndex = line.indexOf("href=");
				int endIndex = line.indexOf(">", beginIndex);
				String listmember = line.substring(beginIndex + 7, endIndex - 1);
				filewriter.write("http://down.znds.com/" + listmember + "\n");
			}
		}
		filewriter.close();
		reader.close();
		System.out.println("INFO: LIST CREATED");
	}

	public static void getinfo(String in_path, String out_path, int cate) throws Exception {
		oldversion = null; // 重置旧版本信息，以防获取不到
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(in_path), "gb2312"));
		String line;
		FileWriter filewriter = new FileWriter(out_path, true);
		int flag = 0;
		int beginIndex = 0, endIndex = 0;

		while ((line = reader.readLine()) != null) {
			if (line.contains("color:#09C;\">")) {
				System.out.println(line+"\n");
				Pattern p = Pattern.compile("color:#09C;\">(.+?)</a>");
				Matcher m = p.matcher(line);
				m.find();
				apkname = m.group(1);
			} else if (line.contains(">更新时间 ")) {
				beginIndex = line.indexOf("更新时间 :");
				endIndex = line.indexOf(" ", beginIndex + 6);
				uptime = line.substring(beginIndex + 6, endIndex);
			} else if (line.contains(">软件语言")) {
				beginIndex = line.indexOf("软件语言 : ");
				endIndex = line.indexOf(" ", beginIndex + 7);
				language = line.substring(beginIndex + 7, endIndex);
			} else if (line.contains(">操控设备")) {
				beginIndex = line.indexOf("操控设备 : ");
				endIndex = line.indexOf("<", beginIndex + 7);
				device = line.substring(beginIndex + 7, endIndex - 1);
			} else if (line.contains(">软件版本")) {
				beginIndex = line.indexOf("软件版本 : ");
				endIndex = line.indexOf(" ", beginIndex + 7);
				version = line.substring(beginIndex + 7, endIndex);
			} else if (line.contains(">软件大小")) {
				beginIndex = line.indexOf("软件大小: ");
				endIndex = line.indexOf("<", beginIndex + 6);
				size = line.substring(beginIndex + 6, endIndex - 1);
			} else if (line.contains(">下载量：")) {
				beginIndex = line.indexOf("下载量：");
				endIndex = line.indexOf("h3", beginIndex);
				downtime = line.substring(beginIndex + 4, endIndex - 2);
			} else if (line.contains("软件简介")) {
				beginIndex = line.indexOf("软件简介 : ");
				intro = line.substring(beginIndex + 7);
			} else if ((line.contains("='http://app.znds.com/down/")) && (flag == 0)) {
				beginIndex = line.indexOf("a href='");
				endIndex = line.indexOf("'", beginIndex + 8);
				apkaddress = line.substring(beginIndex + 8, endIndex);
				flag++;
			} else if ((line.contains("='http://app.znds.com/down/")) && (flag == 1)) {
				flag++;
			} else if (line.contains("版本号：")) {
				beginIndex = line.indexOf("版本号：");
				endIndex = line.indexOf("<", beginIndex);
				if (flag == 2)
					oldversion = line.substring(beginIndex + 4, endIndex) + ":";
				else
					oldversion = oldversion + line.substring(beginIndex + 4, endIndex) + ":";
			} else if ((line.contains("='http://app.znds.com/down/")) && (flag != 0) && (flag != 1)) {
				beginIndex = line.indexOf("href='");
				endIndex = line.indexOf("'", beginIndex + 6);
				oldversion = oldversion + line.substring(beginIndex + 6, endIndex) + "|";
				flag++;
			}
		}

		filewriter.write(apkname + "," + category[cate] + ","+ uptime + "," + language + "," + device + "," + version + "," + size + ","
				+ downtime + "," + apkaddress + "," + oldversion + "," + intro + "," + "\n");
		filewriter.close();
		reader.close();
		System.out.println("INFO: INFOFILE CREATED");
	}

	public static void download(String path) throws Exception {
		// 创建目录并下载
		File file = new File(path + "\\" + apkname);
		file.mkdir();
		URL url = new URL(apkaddress);
		ApkDownloader.download(path + "\\" + apkname, apkname + ".apk", url);
	}

	public static void getcomments(URL topicurl, String htmlpath, String savepath) throws Exception {
		File is_exist = new File(savepath + apkname + ".csv");
		if(is_exist.exists()) {
			return;
		}
		String src_id = new String(topicurl.toString());
		src_id = src_id.substring(src_id.lastIndexOf(".")-4, src_id.lastIndexOf("."));
		URL url1 = new URL("http://changyan.sohu.com/api/2/topic/load?client_id=cyrgSqm47"
				+ "&topic_url=" + topicurl + "&page_size=10&topic_source_id=" + src_id);
		try {
			HtmlDownloader.download(htmlpath + "_t.txt", url1);
		} catch (SocketTimeoutException ex1) {
			System.out.println(ex1);
			return;
		}

		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(htmlpath + "_t.txt"), "gb2312"));
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.contains("total_page_no")) {
				int beginIndex_no = 0, endIndex_no = 0;
				int beginIndex_id = 0, endIndex_id = 0;
				beginIndex_no = line.indexOf("total_page_no");
				endIndex_no = line.indexOf(",", beginIndex_no);
				total_page_no = line.substring(beginIndex_no + 15, endIndex_no);
				beginIndex_id = line.indexOf("topic_id");
				endIndex_id = line.indexOf(",", beginIndex_id);
				topic_id = line.substring(beginIndex_id + 10, endIndex_id);
			}
		}
		reader.close();

		//System.out.println(topic_id+":"+total_page_no+"1\n");
		int no;
		for (no = 1; no <= Integer.valueOf(total_page_no); no++) {
			URL url2 = new URL("http://changyan.sohu.com/api/2/topic/comments?&client_id=cyrgSqm47&topic_id="
					+ topic_id + "&page_size=10&page_no=" + no);
			try {
				HtmlDownloader.download(htmlpath + "_c.txt", url2);
			} catch (SocketTimeoutException ex1) {
				System.out.println(ex1);
				return;
			}
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		HashSet<String> hs = new HashSet<String>();
		File test = new File(htmlpath + "_c.txt");
		if (test.exists()) {
			FileWriter comment = new FileWriter(savepath + apkname + ".csv", true);
			BufferedReader reader_c = new BufferedReader(
					new InputStreamReader(new FileInputStream(htmlpath + "_c.txt"), "gb2312"));
			String line_c;
			while ((line_c = reader_c.readLine()) != null) {
				Pattern p = Pattern.compile("content\":\"(.*?)\",\"create_time\":(.*?),");
				Matcher m = p.matcher(line_c);
				while (m.find()) {
					if (hs.contains(m.group(1))) {
						continue;
					}
					hs.add(m.group(1));
					String str = m.group(1).replace(",", "，");
					str = str.replace(".", "。");
					str = str.replace("\"", "");
					
					comment.write((sdf.format(Long.parseLong(m.group(2))))+","+ str + "\n");
					System.out.println("finding comments...\n");
				}
			}
			//System.out.println("comments completed\n");
			reader_c.close();
			comment.close();
		}
	}

}
