///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.quarkus.platform:quarkus-bom:2.14.1.Final@pom
//DEPS io.quarkus:quarkus-resteasy-reactive
//DEPS io.quarkus:quarkus-rest-client-reactive-jackson
//DEPS io.quarkus:quarkus-qute
//DEPS https://github.com/w3stling/rssreader/tree/v2.5.0

//JAVA 16+

import com.apptastic.rssreader.Item;
import com.apptastic.rssreader.RssReader;
import io.quarkus.qute.Engine;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Collection;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

@QuarkusMain
public class update implements QuarkusApplication {

    @Inject
    Engine qute;

    public Collection<Item> getPosts(String feedUrl, int limit) {
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

    public String readStaticReadme(String filename) {
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

    public int run(String... args) throws Exception {
        String staticReadme = readStaticReadme("readme-static.txt");

        Collection<Item> mastodonPosts = getPosts("https://mastodon.social/users/tcunning.rss", 5);

        Files.writeString(Path.of("readme.md"), 
            qute.parse(Files.readString(Path.of("template.md.qute")))
                .data("bio", staticReadme)
                .data("toots", mastodonPosts)
                .render());
        return 0;
    }
}
