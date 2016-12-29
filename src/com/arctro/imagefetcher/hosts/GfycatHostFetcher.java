package com.arctro.imagefetcher.hosts;

import org.jsoup.nodes.Document;

public class GfycatHostFetcher implements HostFetcher{

	@Override
	public String get(Document d) {
		return d.select("#large-gif").attr("href");
	}

}
