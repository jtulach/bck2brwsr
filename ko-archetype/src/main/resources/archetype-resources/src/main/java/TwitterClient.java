package ${package};

import java.util.Arrays;
import java.util.List;
import net.java.html.json.ComputedProperty;
import net.java.html.json.Context;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.OnPropertyChange;
import net.java.html.json.OnReceive;
import net.java.html.json.Property;

@Model(className="TwitterModel", properties={
    @Property(name="savedLists", type=Tweeters.class, array = true),
    @Property(name="activeTweetersName", type=String.class),
    @Property(name="activeTweeters", type=String.class, array = true),
    @Property(name="userNameToAdd", type=String.class),
    @Property(name="loading", type=boolean.class),
    @Property(name="currentTweets", type=Tweet.class, array = true)
})
public class TwitterClient {
    @Model(className = "Tweeters", properties = {
        @Property(name="name", type = String.class),
        @Property(name="userNames", type = String.class, array = true)
    })
    static class Twttrs {
    }
    @Model(className = "Tweet", properties = {
        @Property(name = "from_user", type = String.class),
        @Property(name = "from_user_id", type = int.class),
        @Property(name = "profile_image_url", type = String.class),
        @Property(name = "text", type = String.class),
        @Property(name = "created_at", type = String.class),
    })
    static final class Twt {
        @ComputedProperty static String html(String text) {
            StringBuilder sb = new StringBuilder(320);
            for (int pos = 0;;) {
                int http = text.indexOf("http", pos);
                if (http == -1) {
                    sb.append(text.substring(pos));
                    return sb.toString();
                }
                int spc = text.indexOf(' ', http);
                if (spc == -1) {
                    spc = text.length();
                }
                sb.append(text.substring(pos, http));
                String url = text.substring(http, spc);
                sb.append("<a href='").append(url).append("'>").append(url).append("</a>");
                pos = spc;
            }
        }
        
        @ComputedProperty static String userUrl(String from_user) {
            return "http://twitter.com/" + from_user;
        }
    }
    @Model(className = "TwitterQuery", properties = {
        @Property(array = true, name = "results", type = Twt.class)
    })
    public static final class TwttrQr {
    }
    
    @OnReceive(url="{root}/search.json?{query}&callback={me}", jsonp="me")
    static void queryTweets(TwitterModel page, TwitterQuery q) {
        page.getCurrentTweets().clear();
        page.getCurrentTweets().addAll(q.getResults());
        page.setLoading(false);
    }
    
    @OnPropertyChange("activeTweetersName")
    static void changeTweetersList(TwitterModel model) {
        Tweeters people = findByName(model.getSavedLists(), model.getActiveTweetersName());        
        model.getActiveTweeters().clear();
        model.getActiveTweeters().addAll(people.getUserNames());
    }
    
    @OnPropertyChange({ "activeTweeters", "activeTweetersCount" })
    static void refreshTweets(TwitterModel model) {
        StringBuilder sb = new StringBuilder();
        sb.append("rpp=25&q=");
        String sep = "";
        for (String p : model.getActiveTweeters()) {
            sb.append(sep);
            sb.append("from:");
            sb.append(p);
            sep = " OR ";
        }
        model.setLoading(true);
        model.queryTweets("http://search.twitter.com", sb.toString());
    }
    
    private static final Context DEFAULT = Context.findDefault(TwitterClient.class);
    static {
        final TwitterModel model = new TwitterModel(DEFAULT);
        final List<Tweeters> svdLst = model.getSavedLists();
        svdLst.add(newTweeters("API Design", "JaroslavTulach"));
        svdLst.add(newTweeters("Celebrities", "JohnCleese", "MCHammer", "StephenFry", "algore", "StevenSanderson"));
        svdLst.add(newTweeters("Microsoft people", "BillGates", "shanselman", "ScottGu"));
        svdLst.add(newTweeters("NetBeans", "GeertjanW","monacotoni", "NetBeans", "petrjiricka"));
        svdLst.add(newTweeters("Tech pundits", "Scobleizer", "LeoLaporte", "techcrunch", "BoingBoing", "timoreilly", "codinghorror"));

        model.setActiveTweetersName("NetBeans");

        model.applyBindings();
    }
    
    @ComputedProperty
    static boolean hasUnsavedChanges(List<String> activeTweeters, List<Tweeters> savedLists, String activeTweetersName) {
        Tweeters tw = findByName(savedLists, activeTweetersName);
        if (activeTweeters == null) {
            return false;
        }
        return !tw.getUserNames().equals(activeTweeters);
    }
    
    @ComputedProperty
    static int activeTweetersCount(List<String> activeTweeters) {
        return activeTweeters.size();
    }
    
    @ComputedProperty
    static boolean userNameToAddIsValid(
        String userNameToAdd, String activeTweetersName, List<Tweeters> savedLists, List<String> activeTweeters
    ) {
        return userNameToAdd != null && 
            userNameToAdd.matches("[a-zA-Z0-9_]{1,15}") &&
            !activeTweeters.contains(userNameToAdd);
    }
    
    @Function
    static void deleteList(TwitterModel model) {
        final List<Tweeters> sl = model.getSavedLists();
        sl.remove(findByName(sl, model.getActiveTweetersName()));
        if (sl.isEmpty()) {
            final Tweeters t = new Tweeters(DEFAULT);
            t.setName("New");
            sl.add(t);
        }
        model.setActiveTweetersName(sl.get(0).getName());
    }
    
    @Function
    static void saveChanges(TwitterModel model) {
        Tweeters t = findByName(model.getSavedLists(), model.getActiveTweetersName());
        int indx = model.getSavedLists().indexOf(t);
        if (indx != -1) {
            t.setName(model.getActiveTweetersName());
            t.getUserNames().clear();
            t.getUserNames().addAll(model.getActiveTweeters());
        }
    }
    
    @Function
    static void addUser(TwitterModel model) {
        String n = model.getUserNameToAdd();
        model.getActiveTweeters().add(n);
    }
    @Function
    static void removeUser(String data, TwitterModel model) {
        model.getActiveTweeters().remove(data);
    }
    
    private static Tweeters findByName(List<Tweeters> list, String name) {
        for (Tweeters l : list) {
            if (l.getName() != null && l.getName().equals(name)) {
                return l;
            }
        }
        return list.isEmpty() ? new Tweeters(DEFAULT) : list.get(0);
    }
    
    private static Tweeters newTweeters(String listName, String... userNames) {
        Tweeters t = new Tweeters(DEFAULT);
        t.setName(listName);
        t.getUserNames().addAll(Arrays.asList(userNames));
        return t;
    }
}
