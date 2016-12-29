package com.arctro.imagefetcher;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ImageFetcher {
	
	public IFS[] search = {new IFS("[name=twitter:image]","content"), new IFS("[name=og:image]","href"), new IFS("[property=og:image]","content")};
	
	public Detector detector = TikaConfig.getDefaultConfig().getDetector();
	
	
	public ImageFetcher(){
		
	}
	
	public ImageFetcher(IFS[] search){
		this();
		this.search = search;
	}
	
	public FetchResult fetch(URL url) throws IOException{
		URLConnection conn = open(url);
		
		if(isImage(conn.getContentType())){
			return fetchImage(url);
		}
		
		Document doc = Jsoup.connect(url.toString()).get();
		
		for(IFS s : search){
			FetchResult b = getFromAttr(url, doc, s);
			if(b != null){
				return b;
			}
		}
		
		Elements imgs = doc.select("img");
		FetchResult b = null;
		int bArea = 0;
		for(Element img : imgs){
			FetchResult tmp = fetchImage(genURL(url, img.attr("src")));
			int tmpArea = tmp.getImage().getWidth() * tmp.getImage().getHeight();
			
			if(b == null || bArea < tmpArea){
				b = tmp;
				bArea = tmpArea;
			}
		}
		
		return b;
	}
	
	public FetchResult fetchImage(URL url) throws IOException{
		InputStream is = new BufferedInputStream(open(url).getInputStream());
		
		MediaType t = detector.detect(is, new Metadata());
		return new FetchResult(ImageIO.read(is), t);
	}
	
	private boolean isImage(String ct){
		return (ct.contains("image"));
	}
	
	private FetchResult getFromAttr(URL main, Document doc, IFS f) throws IOException{
		Elements link = doc.select(f.name);
		if(link.size() > 0){
			return fetchImage(genURL(main, link.attr(f.value)));
		}
		
		return null;
	}
	
	private URL genURL(URL main, String u) throws MalformedURLException{
		URL url = null;
		
		if(u.endsWith("?fb")){
			u = u.substring(0, u.length() - 3);
		}
		
		try{
			url = new URL(u);
		}catch(Exception e){
			url = new URL(main.getProtocol() + "://" + main.getHost() + "/" + u);
		}
		
		return url;
	}
	
	private URLConnection open(URL url) throws IOException{
		URLConnection conn = url.openConnection();
		conn.setRequestProperty("User-Agent", "Arctro Scraper");
		return conn;
	}
	
	public static class IFS {
		String name;
		String value;
		
		public IFS(String name, String value) {
			super();
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}
}
