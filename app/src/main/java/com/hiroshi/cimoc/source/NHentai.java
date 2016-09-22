package com.hiroshi.cimoc.source;

import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.core.parser.MangaParser;
import com.hiroshi.cimoc.core.parser.NodeIterator;
import com.hiroshi.cimoc.core.parser.SearchIterator;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.StringUtils;

import java.util.LinkedList;
import java.util.List;

import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/8/14.
 */
public class NHentai extends MangaParser {

    @Override
    public Request getSearchRequest(String keyword, int page) {
        String url = StringUtils.format("https://nhentai.net/search/?q=%s&page=%d", keyword, page);
        return new Request.Builder().url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("#content > div.index-container > div > a")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.attr("href", "/", 2);
                String title = node.text("div.caption");
                String author = StringUtils.match("\\[(.*?)\\]", title, 1);
                title = title.replaceFirst("\\[.*?\\]\\s+", "");
                String cover = "https:".concat(node.attr("img", "src"));
                return new Comic(SourceManager.SOURCE_NHENTAI, cid, title, cover, null, author);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = StringUtils.format("https://nhentai.net/g/%s", cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<Chapter> parseInfo(String html, Comic comic) {
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        list.add(new Chapter("全一话", ""));

        String title = body.text("#info > h1");
        String intro = body.text("#info > h2");
        String author = body.text("#tags > div > span > a[href^=/artist/]");
        String cover = "https:" + body.attr("#cover > a > img", "src");
        comic.setInfo(title, cover, null, intro, author, true);

        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        return getInfoRequest(cid);
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        Node body = new Node(html);
        List<ImageUrl> list = new LinkedList<>();
        int count = 0;
        for (Node node : body.list("#thumbnail-container > div > a > img")) {
            String url = "https:".concat(node.attr("data-src"));
            list.add(new ImageUrl(++count, url.replace("t.jpg", ".jpg"), false));
        }
        return list;
    }

}
