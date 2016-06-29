package paul.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Update {

	static String[] category = new String[] { "game"};
	static String apkname = null, 
			pinyin = null, 
			cate1 = null,
			cate2 = null,
			rate = null,
			operate = null,
			resolution = null,
			size = null,
			downloadAmount = null,
			language = null,
			latestVersion = null,
			datePublished = null,
			operatingSystem = null,
			page_ad = null,
			apk_ad = null,
			intro = null,
			exception = null;
	
	static int[] cnt = new int[category.length];//每一类的页数

	public static void main(String[] args) throws Exception {
		// 创建目录，设置地址
		String root = new String("F:\\");
		initial(root);		
		for (int i = 0; i < category.length; i++) {
			for (int j = 1; j <= cnt[i]; j++) {
				URL url = new URL("http://app.shafa.com/list/"+category[i]+"/?sort_by=edit_time&page="+j);
				HtmlDownloader.download(root + "\\Crawled_Shafa\\html\\"+category[i]+"_"+j+".txt", url);
				createlist(root + "\\Crawled_Shafa\\html\\"+category[i]+"_"+j+".txt",
						root + "\\Crawled_Shafa\\html\\"+category[i]+"_list.txt");
			}
		}

		for (int i = 0; i < 1; i++) { // 循环读取每个类别已经生成的地址列表
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(root + "\\Crawled_Shafa\\html\\" + category[i] + "_list.txt"), "UTF-8"));
			String line;
			int j = 0;
			while ((line = reader.readLine()) != null) { // 读入某一个APK页面地址

				URL url = new URL(line);
				
				Pattern p = Pattern.compile("http://app.shafa.com/apk/(.+?).html");
				Matcher m = p.matcher(line);
				m.find();
				File filetemp = new File(root + "\\Crawled_Shafa\\" + category[i] + "\\" + m.group(1) + "\\" );
				filetemp.mkdirs();
				
				//下载APP信息页
				try {
					HtmlDownloader.download(root + "\\Crawled_Shafa\\" + category[i] + "\\html\\" + j + "_" + m.group(1) + ".txt", url);
				} catch (SocketTimeoutException ex) {
					System.out.println(ex + " " + url);
					FileWriter fw = new FileWriter(root + "\\Crawled_Shafa\\" + category[i] + "\\html\\" + j + "_" + m.group(1) + ".txt");
					fw.write("0");
					fw.close();
				}

				// 提取信息
				try {
					getinfo(root + "\\Crawled_Shafa\\" + category[i] + "\\html\\" + j + "_" + m.group(1) + ".txt",
							root + "\\Crawled_Shafa\\info.csv", category[i], m.group(1),line);
				} catch (IllegalStateException ex) {
					System.out.println(ex+"------"+exception);
				}

				// 获取历史版本
				try {
					getversion("http://app.shafa.com/apk/"+ pinyin +"/ver.html",
							root + "\\Crawled_Shafa\\" + category[i] + "\\" + pinyin + "\\");
				} catch (IllegalStateException ex) {
					System.out.println(ex+"------"+exception);
				}
				
				// 获取评论
				try {
					getcomments("http://app.shafa.com/apk/"+pinyin+"/comments/?page=",
							root + "\\Crawled_Shafa\\" + category[i] + "\\" + pinyin + "\\");
				} catch (IllegalStateException ex) {
					System.out.println(ex+"------"+exception);
				}
				
				//下载APK 
				try { 
					URL url1 = new URL(apk_ad);
					ApkDownloader.download(root + "\\Crawled_Shafa\\" + category[i] + "\\" + m.group(1)
						, pinyin + ".apk", url1);
				} catch (OutOfMemoryError ex) {
					System.out.println(ex); 
				}
				 
				j++;
			}
			reader.close();
		}
		System.out.println("Finished");
		System.exit(0);
	}

	public static void initial(String root) throws Exception {
		//初始化文件结构
		File file = new File(root + "\\Crawled_Shafa\\html");
		file.mkdirs();
		for (int i = 0; i < category.length; i++) {
			File file_temp = new File(root + "\\Crawled_Shafa\\" + category[i] + "\\html");
			file_temp.mkdirs();
		}
		File info = new File(root + "\\Crawled_Shafa\\info.csv");
		info.createNewFile();
		//FileWriter infoWR = new FileWriter(root + "\\Crawled_Shafa\\info.csv", true);
		//infoWR.write("软件名称,文件夹名,类型,细分类型,评分,操作方式,分辨率,大小,下载量,语言,最新版本,更新时间,系统要求,页面地址,下载地址,软件简介\n");
		//infoWR.close();
		//下载各类首页，统计页面数
		for (int i = 0; i < category.length; i++) {
			URL url = new URL("http://app.shafa.com/list/"+category[i]+"/?sort_by=edit_time&page=1");
			HtmlDownloader.download(root + "\\Crawled_Shafa\\html\\" + category[i] + "_t.txt", url);
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(root + "\\Crawled_Shafa\\html\\" + category[i] + "_t.txt")));
			String line;
			cnt[i] = 0;
			while ((line = reader.readLine()) != null) {
				Pattern p = Pattern.compile("共 (.+?) 页");
				Matcher m = p.matcher(line);
				while (m.find()) {
					System.out.println(m.group(1));
					cnt[i] = Integer.valueOf(m.group(1));
				}
				if (cnt[i] != 0) {
					reader.close();
					break;
				}
			}
		}

		//设置输出
		FileOutputStream debugger = new FileOutputStream(root + "\\Crawled_Shafa\\Debugger.txt");
		MultiOutputStream multi = new MultiOutputStream(new PrintStream(debugger), System.out);
		System.setOut(new PrintStream(multi));
		System.out.println("------INFO: INITIAL SUCCEEDED------\n");
	}
	
	public static void createlist(String in_path, String out_path) throws Exception {
		//参数为输入输出路径，含文件名
		File save = new File(out_path);
		save.createNewFile();
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(in_path), "UTF-8"));
		FileWriter fw = new FileWriter(out_path, true);
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.contains("app-item-element")) {
				Pattern p = Pattern.compile("href=\"(.+?)\"");
				Matcher m = p.matcher(line);
				while(m.find()) {
					fw.write(m.group(1)+"\n");
				}
			}
		}
		fw.close();
		reader.close();
		System.out.println("------INFO: " + in_path + "ADD TO LIST------\n");
	}

	public static void getinfo(String in_path, String out_path, String category, String py, String page) throws Exception {
		pinyin = py;				//以拼音作文件夹名
		cate1 = category;			//大的分类
		page_ad = page;				//页面地址
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(in_path), "gb2312"));
		String line;
		FileWriter filewriter = new FileWriter(out_path, true);
		int rating_level = 5;//评分星级

		while ((line = reader.readLine()) != null) {
			exception = line;
			if (line.contains("itemprop=\"name\"")) {	//软件名称
				Pattern p = Pattern.compile("<h1 title=\"(.+?)\"");
				Matcher m = p.matcher(line);
				m.find();
				apkname = m.group(1);
			} else if (line.contains("applicationCategory")) {  //细分类型
				Pattern p = Pattern.compile("<span itemprop=\"applicationCategory\">(.+?)</span>");
				Matcher m = p.matcher(line);
				m.find();
				cate2 = m.group(1);
			} else if (line.contains("ratingValue")) {	//评分
				Pattern p = Pattern.compile("itemprop=\"ratingValue\" content=\"(\\d)\"");
				Matcher m = p.matcher(line);
				m.find();
				rate = "总分:"+m.group(1)+"，\t";
			} else if (line.contains("review-chart-percentage")) { //评分分布
				Pattern p = Pattern.compile("review-chart-percentage\">(.+?)<");
				Matcher m = p.matcher(line);
				m.find();
				rate += rating_level + "分:"+m.group(1)+"，\t";
				rating_level--;
			} else if (line.contains("操作方式：</span>")) {			//操作方式
				reader.readLine();
				Pattern p = Pattern.compile("(\\S+)");
				Matcher m = p.matcher(reader.readLine());
				m.find();
				operate = m.group(1);
			} else if (line.contains("分辨率：</span>")) {			//分辨率
				reader.readLine();
				Pattern p = Pattern.compile("(\\S+)");
				Matcher m = p.matcher(reader.readLine());
				m.find();
				resolution = m.group(1);
			} else if (line.contains("大小</div>")) {				//大小
				Pattern p = Pattern.compile("<span>(.+?)</span>");
				Matcher m = p.matcher(reader.readLine());
				m.find();
				size = m.group(1);
			} else if (line.contains("下载</div>")) {				//下载量
				Pattern p = Pattern.compile("<span>(.+?)</span>");
				Matcher m = p.matcher(reader.readLine());
				m.find();
				downloadAmount = m.group(1);
			} else if (line.contains("语言</div>")) {				//语言
				Pattern p = Pattern.compile("<span>(.+?)</span>");
				Matcher m = p.matcher(reader.readLine());
				m.find();
				language = m.group(1);
			} else if (line.contains("softwareVersion")) {			//最新版本
				Pattern p = Pattern.compile("softwareVersion\">(.+?)</span>");
				Matcher m = p.matcher(line);
				m.find();
				latestVersion = m.group(1);
			} else if (line.contains("datePublished")) {			//更新时间
				Pattern p = Pattern.compile(">(.+?)</span>");
				Matcher m = p.matcher(line);
				m.find();
				datePublished = m.group(1);
			} else if (line.contains("operatingSystem")) {			//系统要求
				Pattern p = Pattern.compile(">(.+?)</span>");
				Matcher m = p.matcher(line);
				m.find();
				operatingSystem = m.group(1).replace(",", "，");
			} else if (line.contains("<a href=\"#\" data-url=")) {	//下载地址
				Pattern p = Pattern.compile("data-url=\"(.+?)\"");
				Matcher m = p.matcher(line);
				m.find();
				apk_ad = m.group(1);
			} else if (line.contains("app-view-desc app-view-section")) {//软件简介
				reader.readLine();
				reader.readLine();
				reader.readLine();
				line = reader.readLine();
				line = line.replace(" ", "");
				line =line.replace(",", "，");
				line =line.replace("<p>", "");
				line =line.replace("</p>", "\t");
				intro = line;
			}
		}

		filewriter.write(apkname+","+pinyin+","+cate1+","+cate2+","+rate+","+operate+","+resolution+","+size +","+
				downloadAmount+","+language+","+latestVersion+","+datePublished+","+operatingSystem+","+page_ad+","+apk_ad+","+intro+"\n");
		filewriter.close();
		reader.close();
		System.out.println("------INFO: " + apkname+ "INFO COLLECTED------\n");
	}

	public static void getversion(String page, String out_path) throws Exception {
		String version = null, size = null, downloadAddress = null;
		URL url = new URL(page);
		HtmlDownloader.download(out_path + "version_page.txt", url);
		File ver = new File(out_path+"version.csv");
		ver.createNewFile();
		FileWriter fw = new FileWriter(out_path + "version.csv");
		fw.write("软件版本,软件大小,下载地址\n");
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(out_path+"version_page.txt"), "gb2312"));
		String line;
		while ((line = reader.readLine()) != null) {
			exception = line;
			if (line.contains("<td>")) {
				if (line.contains("href")) {
					continue;
				} else if(line.contains("<span>")){
					Pattern p = Pattern.compile("<span>软件大小： (.+?)</span>");
					Matcher m = p.matcher(line);
					m.find();
					size = m.group(1);
				} else if(line.contains("</td>")){
					Pattern p = Pattern.compile("<td>(.+?)</td>");
					Matcher m = p.matcher(line);
					m.find();
					version = m.group(1);
				}
			} else if (line.contains("<a href=\"#\" data-url=")) {
				Pattern p = Pattern.compile("data-url=\"(.+?)\"");
				Matcher m = p.matcher(line);
				m.find();
				downloadAddress = m.group(1);
				fw.write(version+","+size+","+downloadAddress+"\n");
			}
		}
		fw.close();
		reader.close();
		System.out.println("------INFO: "+ apkname + " VERSIONS COLLECTED------\n");
	}
	
	public static void getcomments(String page, String out_path) throws Exception {
		String text = null, version = null, date = null, tv_type = null;
		int rate = 0, totalPageNo = 1;
		File file1 = new File(out_path+"comment");
		file1.mkdirs();
		File file2 = new File(out_path+"comments.csv");
		file2.createNewFile();
		FileWriter fw = new FileWriter(out_path+"comments.csv");
		fw.write("软件版本,评论时间,评论星级,评论内容,电视型号\n");
		URL url1 = new URL(page+"1");
		HtmlDownloader.download(out_path+"comment\\comment_1.txt", url1);
		BufferedReader reader1 = new BufferedReader(
				new InputStreamReader(new FileInputStream(out_path+"comment\\comment_1.txt"),"gb2312"));
		String line = null;
		while ((line = reader1.readLine()) != null) {
			exception = line;
			if (line.contains("pagination-element-total")) {	//评论页数
				Pattern p = Pattern.compile("共 (.+?) 页");
				Matcher m = p.matcher(line);
				m.find();
				totalPageNo = Integer.valueOf(m.group(1));
			}
		}
		reader1.close();
		for (int i = 1; i <= totalPageNo; i++) {
			if (i != 1) {
				URL url = new URL(page+ i);
				HtmlDownloader.download(out_path+"comment\\comment_"+i+".txt", url);
			}
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(out_path+"comment\\comment_"+i+".txt"),"gb2312"));
			String line_before = "";	
			while ((line = reader.readLine()) != null) {
				exception = line;
				if (line.contains("comment-text-review")) {
					text = null; version = null; date = null; tv_type = null; rate = 0;
				} else if (line.contains("span class=\"version\"")) {					//软件版本
					Pattern p = Pattern.compile("（版本 (.+?)）");
					Matcher m = p.matcher(line);
					m.find();
					version = m.group(1);
				} else if (line.contains("span class=\"pull-right\"")) {//评论时间
					Pattern p = Pattern.compile(">(.+?)</span>");
					Matcher m = p.matcher(line);
					m.find();
					date = m.group(1);
				} else if (line.contains("<p>")&&line.contains("</p>")) {						//评论内容
					Pattern p = Pattern.compile("<p>(.+?)</p>");
					Matcher m = p.matcher(line);
					m.find();
					text = m.group(1).replace(",", "，");
				} else if (line.contains("review-star small hit")) {	//评论星级
					rate++;
				} else if (line_before.contains("<span>")) {			//电视型号
					Pattern p = Pattern.compile("(\\S+)");
					Matcher m = p.matcher(line);
					if (m.find()) {
						tv_type = m.group(1);
					}
				} else if (line.contains("pull-right comment-operation")) {
					fw.write(version +","+date+","+rate+","+text+","+tv_type+"\n");
				}
				line_before = line;
			}
			reader.close();
			System.out.println("------INFO: "+ apkname + " COMMENT PAGE=" + i + " COMPLETE------\n");
		}
		fw.close();
		System.out.println("------INFO: "+ apkname + " COMMENT COMPLETE------\n");
	}
}
