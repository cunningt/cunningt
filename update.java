///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.apache.velocity:velocity-engine-core:2.3
//DEPS https://github.com/w3stling/rssreader/tree/v2.5.0

//JAVA 11+

import com.apptastic.rssreader.Item;
import com.apptastic.rssreader.RssReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Collection;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public class update {

    public static Collection<Item> getPosts(String feedUrl, int limit) {
        Collection<Item> sorted = new PriorityQueue<>(Collections.reverseOrder());
        RssReader reader = new RssReader();
        try {
            Stream<Item> rssFeed = reader.read(feedUrl);
            sorted.addAll(rssFeed.limit(limit).collect(Collectors.toList()));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return sorted;
    }

    public static String readStaticReadme(String filename) {
        File file = new File(filename);
        StringBuilder sb = new StringBuilder(1000);
        String line = null;

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {
                sb.append(line);
		sb.append(System.getProperty("line.separator"));
            }
            br.close();
        } catch (java.io.IOException ioe) {
            ioe.printStackTrace();
        } 
        return sb.toString();
    }

    public static void main (String args[]) throws Exception {
        String staticReadme = readStaticReadme("readme-static.txt");

        Collection<Item> mastodonPosts = getPosts("https://mastodon.social/users/tcunning.rss", 5);

        VelocityEngine ve = new VelocityEngine();
        ve.init();
            
        Template t = ve.getTemplate("template.vtl");
        VelocityContext vc = new VelocityContext();
        vc.put("bio", staticReadme);
        vc.put("toots", mastodonPosts);
            
	StringWriter sw = new StringWriter(); 
	PrintWriter pw = new PrintWriter("readme.md");
        t.merge(vc, sw);
	pw.write(sw.toString());
	pw.close();
    }
}
