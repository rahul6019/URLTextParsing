package urlparser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetURLContent {
	
	static File file = new File("src/urlparser/example.txt");
	static String pattern = "head";
	static Pattern r = Pattern.compile(pattern);
	static Semaphore s = new Semaphore(20);
    
	public static void main(String[] args) {
		
		// Extract url list from link
		List<String> urllist = new ArrayList<>();
		try {
			URL url = new URL("https://s3.amazonaws.com/fieldlens-public/urls.txt");
			URLConnection conn = url.openConnection();
			BufferedReader br = new BufferedReader(
                               new InputStreamReader(conn.getInputStream()));
			if(!file.exists()) {
				file.createNewFile();
			}
			String inputLine = br.readLine();
			while ((inputLine = br.readLine()) != null) {
				urllist.add(inputLine.split(",")[1].replaceAll("\"", ""));
			}
			br.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// match for content & store in file
		for(String url : urllist) {
			try {
				s.acquire();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			new Thread() {
				public void run() {
					try {
						URL url1 = new URL("https://www." + url);
						HttpURLConnection huc = (HttpURLConnection) url1.openConnection();
						huc.setConnectTimeout(1 * 1000);
						huc.setReadTimeout(10 * 1000);
						BufferedReader br1 = new BufferedReader(
			                               new InputStreamReader(huc.getInputStream()));
						BufferedWriter output = new BufferedWriter(new FileWriter(file,true));
						
						String inputLine = br1.readLine();
						while ((inputLine = br1.readLine()) != null) {
							Matcher m = r.matcher(inputLine);
							if(m.find()) {
								output.write("Found value [" + pattern + "] " + "in: " + url);
								output.newLine();
								output.flush();
								break;
							}
						}
						br1.close();
						output.close();
					} catch (MalformedURLException e) {
						//e.printStackTrace();
					} catch (IOException e) {
						//e.printStackTrace();
					} finally {
				        s.release();
				    }
				}
			}.start();
		}
		
	// end main	
	}
//end class	
}