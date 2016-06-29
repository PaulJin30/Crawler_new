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
	
	static int[] cnt = new int[category.length];//ÿһ���ҳ��

	public static void main(String[] args) throws Exception {
		// ����Ŀ¼�����õ�ַ
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

		for (int i = 0; i < 1; i++) { // ѭ����ȡÿ������Ѿ����ɵĵ�ַ�б�
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(root + "\\Crawled_Shafa\\html\\" + category[i] + "_list.txt"), "UTF-8"));
			String line;
			int j = 0;
			while ((line = reader.readLine()) != null) { // ����ĳһ��APKҳ���ַ

				URL url = new URL(line);
				
				Pattern p = Pattern.compile("http://app.shafa.com/apk/(.+?).html");
				Matcher m = p.matcher(line);
				m.find();
				File filetemp = new File(root + "\\Crawled_Shafa\\" + category[i] + "\\" + m.group(1) + "\\" );
				filetemp.mkdirs();
				
				//����APP��Ϣҳ
				try {
					HtmlDownloader.download(root + "\\Crawled_Shafa\\" + category[i] + "\\html\\" + j + "_" + m.group(1) + ".txt", url);
				} catch (SocketTimeoutException ex) {
					System.out.println(ex + " " + url);
					FileWriter fw = new FileWriter(root + "\\Crawled_Shafa\\" + category[i] + "\\html\\" + j + "_" + m.group(1) + ".txt");
					fw.write("0");
					fw.close();
				}

				// ��ȡ��Ϣ
				try {
					getinfo(root + "\\Crawled_Shafa\\" + category[i] + "\\html\\" + j + "_" + m.group(1) + ".txt",
							root + "\\Crawled_Shafa\\info.csv", category[i], m.group(1),line);
				} catch (IllegalStateException ex) {
					System.out.println(ex+"------"+exception);
				}

				// ��ȡ��ʷ�汾
				try {
					getversion("http://app.shafa.com/apk/"+ pinyin +"/ver.html",
							root + "\\Crawled_Shafa\\" + category[i] + "\\" + pinyin + "\\");
				} catch (IllegalStateException ex) {
					System.out.println(ex+"------"+exception);
				}
				
				// ��ȡ����
				try {
					getcomments("http://app.shafa.com/apk/"+pinyin+"/comments/?page=",
							root + "\\Crawled_Shafa\\" + category[i] + "\\" + pinyin + "\\");
				} catch (IllegalStateException ex) {
					System.out.println(ex+"------"+exception);
				}
				
				//����APK 
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
		//��ʼ���ļ��ṹ
		File file = new File(root + "\\Crawled_Shafa\\html");
		file.mkdirs();
		for (int i = 0; i < category.length; i++) {
			File file_temp = new File(root + "\\Crawled_Shafa\\" + category[i] + "\\html");
			file_temp.mkdirs();
		}
		File info = new File(root + "\\Crawled_Shafa\\info.csv");
		info.createNewFile();
		//FileWriter infoWR = new FileWriter(root + "\\Crawled_Shafa\\info.csv", true);
		//infoWR.write("�������,�ļ�����,����,ϸ������,����,������ʽ,�ֱ���,��С,������,����,���°汾,����ʱ��,ϵͳҪ��,ҳ���ַ,���ص�ַ,������\n");
		//infoWR.close();
		//���ظ�����ҳ��ͳ��ҳ����
		for (int i = 0; i < category.length; i++) {
			URL url = new URL("http://app.shafa.com/list/"+category[i]+"/?sort_by=edit_time&page=1");
			HtmlDownloader.download(root + "\\Crawled_Shafa\\html\\" + category[i] + "_t.txt", url);
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(root + "\\Crawled_Shafa\\html\\" + category[i] + "_t.txt")));
			String line;
			cnt[i] = 0;
			while ((line = reader.readLine()) != null) {
				Pattern p = Pattern.compile("�� (.+?) ҳ");
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

		//�������
		FileOutputStream debugger = new FileOutputStream(root + "\\Crawled_Shafa\\Debugger.txt");
		MultiOutputStream multi = new MultiOutputStream(new PrintStream(debugger), System.out);
		System.setOut(new PrintStream(multi));
		System.out.println("------INFO: INITIAL SUCCEEDED------\n");
	}
	
	public static void createlist(String in_path, String out_path) throws Exception {
		//����Ϊ�������·�������ļ���
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
		pinyin = py;				//��ƴ�����ļ�����
		cate1 = category;			//��ķ���
		page_ad = page;				//ҳ���ַ
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(in_path), "gb2312"));
		String line;
		FileWriter filewriter = new FileWriter(out_path, true);
		int rating_level = 5;//�����Ǽ�

		while ((line = reader.readLine()) != null) {
			exception = line;
			if (line.contains("itemprop=\"name\"")) {	//�������
				Pattern p = Pattern.compile("<h1 title=\"(.+?)\"");
				Matcher m = p.matcher(line);
				m.find();
				apkname = m.group(1);
			} else if (line.contains("applicationCategory")) {  //ϸ������
				Pattern p = Pattern.compile("<span itemprop=\"applicationCategory\">(.+?)</span>");
				Matcher m = p.matcher(line);
				m.find();
				cate2 = m.group(1);
			} else if (line.contains("ratingValue")) {	//����
				Pattern p = Pattern.compile("itemprop=\"ratingValue\" content=\"(\\d)\"");
				Matcher m = p.matcher(line);
				m.find();
				rate = "�ܷ�:"+m.group(1)+"��\t";
			} else if (line.contains("review-chart-percentage")) { //���ֲַ�
				Pattern p = Pattern.compile("review-chart-percentage\">(.+?)<");
				Matcher m = p.matcher(line);
				m.find();
				rate += rating_level + "��:"+m.group(1)+"��\t";
				rating_level--;
			} else if (line.contains("������ʽ��</span>")) {			//������ʽ
				reader.readLine();
				Pattern p = Pattern.compile("(\\S+)");
				Matcher m = p.matcher(reader.readLine());
				m.find();
				operate = m.group(1);
			} else if (line.contains("�ֱ��ʣ�</span>")) {			//�ֱ���
				reader.readLine();
				Pattern p = Pattern.compile("(\\S+)");
				Matcher m = p.matcher(reader.readLine());
				m.find();
				resolution = m.group(1);
			} else if (line.contains("��С</div>")) {				//��С
				Pattern p = Pattern.compile("<span>(.+?)</span>");
				Matcher m = p.matcher(reader.readLine());
				m.find();
				size = m.group(1);
			} else if (line.contains("����</div>")) {				//������
				Pattern p = Pattern.compile("<span>(.+?)</span>");
				Matcher m = p.matcher(reader.readLine());
				m.find();
				downloadAmount = m.group(1);
			} else if (line.contains("����</div>")) {				//����
				Pattern p = Pattern.compile("<span>(.+?)</span>");
				Matcher m = p.matcher(reader.readLine());
				m.find();
				language = m.group(1);
			} else if (line.contains("softwareVersion")) {			//���°汾
				Pattern p = Pattern.compile("softwareVersion\">(.+?)</span>");
				Matcher m = p.matcher(line);
				m.find();
				latestVersion = m.group(1);
			} else if (line.contains("datePublished")) {			//����ʱ��
				Pattern p = Pattern.compile(">(.+?)</span>");
				Matcher m = p.matcher(line);
				m.find();
				datePublished = m.group(1);
			} else if (line.contains("operatingSystem")) {			//ϵͳҪ��
				Pattern p = Pattern.compile(">(.+?)</span>");
				Matcher m = p.matcher(line);
				m.find();
				operatingSystem = m.group(1).replace(",", "��");
			} else if (line.contains("<a href=\"#\" data-url=")) {	//���ص�ַ
				Pattern p = Pattern.compile("data-url=\"(.+?)\"");
				Matcher m = p.matcher(line);
				m.find();
				apk_ad = m.group(1);
			} else if (line.contains("app-view-desc app-view-section")) {//������
				reader.readLine();
				reader.readLine();
				reader.readLine();
				line = reader.readLine();
				line = line.replace(" ", "");
				line =line.replace(",", "��");
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
		fw.write("����汾,�����С,���ص�ַ\n");
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(out_path+"version_page.txt"), "gb2312"));
		String line;
		while ((line = reader.readLine()) != null) {
			exception = line;
			if (line.contains("<td>")) {
				if (line.contains("href")) {
					continue;
				} else if(line.contains("<span>")){
					Pattern p = Pattern.compile("<span>�����С�� (.+?)</span>");
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
		fw.write("����汾,����ʱ��,�����Ǽ�,��������,�����ͺ�\n");
		URL url1 = new URL(page+"1");
		HtmlDownloader.download(out_path+"comment\\comment_1.txt", url1);
		BufferedReader reader1 = new BufferedReader(
				new InputStreamReader(new FileInputStream(out_path+"comment\\comment_1.txt"),"gb2312"));
		String line = null;
		while ((line = reader1.readLine()) != null) {
			exception = line;
			if (line.contains("pagination-element-total")) {	//����ҳ��
				Pattern p = Pattern.compile("�� (.+?) ҳ");
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
				} else if (line.contains("span class=\"version\"")) {					//����汾
					Pattern p = Pattern.compile("���汾 (.+?)��");
					Matcher m = p.matcher(line);
					m.find();
					version = m.group(1);
				} else if (line.contains("span class=\"pull-right\"")) {//����ʱ��
					Pattern p = Pattern.compile(">(.+?)</span>");
					Matcher m = p.matcher(line);
					m.find();
					date = m.group(1);
				} else if (line.contains("<p>")&&line.contains("</p>")) {						//��������
					Pattern p = Pattern.compile("<p>(.+?)</p>");
					Matcher m = p.matcher(line);
					m.find();
					text = m.group(1).replace(",", "��");
				} else if (line.contains("review-star small hit")) {	//�����Ǽ�
					rate++;
				} else if (line_before.contains("<span>")) {			//�����ͺ�
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
