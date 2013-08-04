/**
 * ########################  SHENBAISE'S WORK  ##########################
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sbs.goodcrawler.plugin.extract;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.sbs.goodcrawler.conf.jobconf.JobConfiguration;
import org.sbs.goodcrawler.exception.QueueException;
import org.sbs.goodcrawler.extractor.Extractor;
import org.sbs.goodcrawler.job.Page;
import org.sbs.goodcrawler.storage.PendingStore.ExtractedPage;
import org.sbs.goodcrawler.urlmanager.WebURL;

import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author whiteme
 * @date 2013年7月28日
 * @desc 
 */
public class ExtractYouku extends Extractor {
	private Log log = LogFactory.getLog(this.getClass());
//http://player.youku.com/player.php/sid/XNTg3ODI3NTIw/v.swf?isAutoPlay=true 
	public ExtractYouku(JobConfiguration conf) {
		super(conf);
	}

	@Override
	public ExtractedPage<?, ?> onExtract(Page page) {
		if(null!=page){
			ExtractedPage<String,Object> epage = pendingStore.new ExtractedPage<String, Object>();
			epage.setUrl(page.getWebURL());
			HashMap<String, Object> result = getInformation(page);
			epage.setMessages(result);
			try {
				pendingStore.addExtracedPage(epage);
			} catch (QueueException e) {
				 log.error(e.getMessage());
			}
			return epage;
		}
		return null;
	}

	@Override
	public ExtractedPage<?, ?> beforeExtract(Page page) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExtractedPage<?, ?> afterExtract(Page page) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * 返回影视资源信息
	 * @param url
	 * @return
	 */
	public HashMap<String, Object> getInformation(Page page){
		HashMap<String, Object> map = Maps.newHashMap();
		String url = page.getWebURL().getURL();
		try {
			ExtractedPage<String,Object> epage = pendingStore.new ExtractedPage<String, Object>();
			epage.setUrl(page.getWebURL());
			
			if(url.contains("/show_page/")){
				Document doc = Jsoup.parse(new String(page.getContentData(),page.getContentCharset()), urlUtils.getBaseUrl(page.getWebURL().getURL()));
				// 提取Url，放入待抓取Url队列
				Elements links = doc.getElementsByTag("a"); 
		        if (!links.isEmpty()) { 
		            for (Element link : links) { 
		                String linkHref = link.absUrl("href"); 
		                if(filterUrls(linkHref)){
		                	WebURL weburl = new WebURL();
		                	weburl.setURL(linkHref);
		                	weburl.setJobName(conf.getName());
		                	try {
								pendingUrls.addUrl(weburl);
							} catch (QueueException e) {
								 log.error(e.getMessage());
							}
		                }
		            }
		        }
		        
				String title = doc.select(".title .name").text();
				map.put("title", title);
				String category = doc.select(".title .type a").text();
				map.put("category", category);
				int year = Integer.parseInt(CharMatcher.DIGIT.retainFrom(doc.select(".title .pub").text()));
				map.put("year", year);
				String score = CharMatcher.DIGIT.retainFrom(doc.select(".ratingstar .num").text());
				map.put("score", score);
				String alias = doc.select(".alias").text();
				if(alias.contains(":")){
					map.put("translation", alias.split(":")[1]);
				}
				String img = doc.select(".thumb img").attr("src");
				map.put("thumbnail",Lists.newArrayList(img));
				String area = doc.select(".row2 .area a").text();
				map.put("area", area);
				String[] type = doc.select(".row2 .type a").text().split(" ");
				map.put("type", Lists.newArrayList(type));
				String director = doc.select(".row2 .director a").text();
				map.put("director", director);
				int duration = Integer.parseInt(CharMatcher.DIGIT.retainFrom(doc.select(".row2 .duration").text()));
				map.put("duration", duration);
				int hot = Integer.parseInt(CharMatcher.anyOf(",").removeFrom(doc.select(".row2 .vr .num").text()));
				map.put("hot", hot);
				String sumary = doc.select(".detail .long").text();
				map.put("summary", sumary);
				// 播放和预告
				Elements elements = doc.select(".baseaction a");
				HashMap<String, String> playList = Maps.newHashMap();
				for(Element element:elements){
					String n = element.text();
					String urlString = element.attr("href");
					Document d2 = Jsoup.parse(new URL(urlString), 10000);
					if(null!=d2){
						String x = d2.select("#link2").attr("value");
						playList.put(n, x);
					}
				}
				map.put("online", playList);
			}else if(url.contains("/v_show/")) {
				Document d3 = Jsoup.parse(new String(page.getContentData(),page.getContentCharset()), urlUtils.getBaseUrl(page.getWebURL().getURL()));
				// 提取Url，放入待抓取Url队列
				Elements links = d3.getElementsByTag("a"); 
		        if (!links.isEmpty()) { 
		            for (Element link : links) { 
		                String linkHref = link.absUrl("href"); 
		                if(filterUrls(linkHref)){
		                	WebURL weburl = new WebURL();
		                	weburl.setURL(linkHref);
		                	weburl.setJobName(conf.getName());
		                	try {
								pendingUrls.addUrl(weburl);
							} catch (QueueException e) {
								 log.error(e.getMessage());
							}
		                }
		            }
		        }
				String p = d3.select("h1.title a").attr("href");
				return getInformation(p);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(map);
		return map;
	}
	
	private HashMap<String, Object> getInformation(String p) {
		HashMap<String, Object> map = Maps.newHashMap();
		try {
			if(p.contains("/show_page/")){
				Document doc = Jsoup.parse(new URL(p),15000);
				// 提取Url，放入待抓取Url队列
				Elements links = doc.getElementsByTag("a"); 
		        if (!links.isEmpty()) { 
		            for (Element link : links) { 
		                String linkHref = link.absUrl("href"); 
		                if(filterUrls(linkHref)){
		                	WebURL weburl = new WebURL();
		                	weburl.setURL(linkHref);
		                	weburl.setJobName(conf.getName());
		                	try {
								pendingUrls.addUrl(weburl);
							} catch (QueueException e) {
								 log.error(e.getMessage());
							}
		                }
		            }
		        }
		        
				String title = doc.select(".title .name").text();
				map.put("title", title);
				String category = doc.select(".title .type a").text();
				map.put("category", category);
				int year = Integer.parseInt(CharMatcher.DIGIT.retainFrom(doc.select(".title .pub").text()));
				map.put("year", year);
				String score = CharMatcher.DIGIT.retainFrom(doc.select(".ratingstar .num").text());
				map.put("score", score);
				String alias = doc.select(".alias").text();
				if(alias.contains(":")){
					map.put("translation", alias.split(":")[1]);
				}
				String img = doc.select(".thumb img").attr("src");
				map.put("thumbnail",Lists.newArrayList(img));
				String area = doc.select(".row2 .area a").text();
				map.put("area", area);
				String[] type = doc.select(".row2 .type a").text().split(" ");
				map.put("type", Lists.newArrayList(type));
				String director = doc.select(".row2 .director a").text();
				map.put("director", director);
				int duration = Integer.parseInt(CharMatcher.DIGIT.retainFrom(doc.select(".row2 .duration").text()));
				map.put("duration", duration);
				int hot = Integer.parseInt(CharMatcher.anyOf(",").removeFrom(doc.select(".row2 .vr .num").text()));
				map.put("hot", hot);
				String sumary = doc.select(".detail .long").text();
				map.put("summary", sumary);
				// 播放和预告
				Elements elements = doc.select(".baseaction a");
				HashMap<String, String> playList = Maps.newHashMap();
				for(Element element:elements){
					String n = element.text();
					String urlString = element.attr("href");
					Document d2 = Jsoup.parse(new URL(urlString), 10000);
					if(null!=d2){
						String x = d2.select("#link2").attr("value");
						playList.put(n, x);
					}
				}
				map.put("online", playList);
			}
			else
				return null;
		} catch (Exception e) {
			return map;
		}
		return map;
	}

	public static void main(String[] args) {
		
		String url = "http://www.youku.com/show_page/id_z14d2c054b4d611e0a046.html";
		String vshow = "http://v.youku.com/v_show/id_XNTcwNjI2NTY4.html";
		
	}
}