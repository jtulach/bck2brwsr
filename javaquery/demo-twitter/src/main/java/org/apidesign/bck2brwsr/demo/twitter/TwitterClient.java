/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://opensource.org/licenses/GPL-2.0.
 */
package org.apidesign.bck2brwsr.demo.twitter;

import java.util.Arrays;
import java.util.List;
import org.apidesign.bck2brwsr.htmlpage.api.*;
import org.apidesign.bck2brwsr.htmlpage.api.Page;
import org.apidesign.bck2brwsr.htmlpage.api.Property;
import org.apidesign.bck2brwsr.htmlpage.api.ComputedProperty;

/** Controller class for access to Twitter.
 * 
 * @author Jaroslav Tulach
 */
@Page(xhtml="index.html", className="TwitterModel", properties={
    @Property(name="savedLists", type=TwitterClient.Twttrs.class, array = true),
    @Property(name="activeTweetersName", type=String.class),
    @Property(name="modifiedList", type=TwitterClient.Twttrs.class),
    @Property(name="userNameToAdd", type=String.class),
    @Property(name="currentTweets", type=TwitterClient.Twt.class, array = true)
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
        
    })
    static final class Twt {
    }
    @Model(className = "TwitterQuery", properties = {
        @Property(array = true, name = "results", type = Twt.class)
    })
    public static final class TwttrQr {
    }
    
    @OnReceive(url="{url}")
    static void queryTweets(TwitterModel page, TwitterQuery q) {
        page.getCurrentTweets().clear();
        page.getCurrentTweets().addAll(q.getResults());
    }
    
    @OnFunction
    static void refreshTweets(TwitterModel model) {
        Tweeters people = model.getActiveTweeters();
        StringBuilder sb = new StringBuilder();
        sb.append("http://search.twitter.com/search.json?callback=?&rpp=25&q=");
        String sep = "";
        for (String p : people.getUserNames()) {
            sb.append(sep);
            sb.append("from:");
            sb.append(p);
            sep = " OR ";
        }
        model.queryTweets(sb.toString());
    }
    
    private static Tweeters tweeters(String listName, String... userNames) {
        Tweeters t = new Tweeters();
        t.setName(listName);
        t.getUserNames().addAll(Arrays.asList(userNames));
        return t;
    }
    
    static {
        final TwitterModel model = new TwitterModel();
        final List<Tweeters> svdLst = model.getSavedLists();
        svdLst.add(tweeters("API Design", "JaroslavTulach"));
        svdLst.add(tweeters("Celebrities", "JohnCleese", "MCHammer", "StephenFry", "algore", "StevenSanderson"));
        svdLst.add(tweeters("Microsoft people", "BillGates", "shanselman", "ScottGu"));
        svdLst.add(tweeters("NetBeans", "GeertjanW","monacotoni", "NetBeans"));
        svdLst.add(tweeters("Tech pundits", "Scobleizer", "LeoLaporte", "techcrunch", "BoingBoing", "timoreilly", "codinghorror"));

        model.setActiveTweetersName("NetBeans");

        model.applyBindings();
    }
    
    @ComputedProperty
    static Tweeters activeTweeters(String activeTweetersName, List<Tweeters> savedLists, Tweeters modifiedList) {
        if (modifiedList != null && modifiedList.getName() != null) {
            if (modifiedList.getName().equals(activeTweetersName)) {
                return modifiedList;
            } else {
                modifiedList.setName(null);
            }
        }
        return findByName(savedLists, activeTweetersName);
    }
    
    @ComputedProperty
    static boolean hasUnsavedChanges(Tweeters modifiedList) {
        return modifiedList != null;
    }
    
    @ComputedProperty
    static boolean userNameToAddIsValid(String userNameToAdd, String activeTweetersName, List<Tweeters> savedLists, Tweeters modifiedList) {
        Tweeters editingList = activeTweeters(activeTweetersName, savedLists, modifiedList);
        return editingList != null && userNameToAdd != null && 
            userNameToAdd.matches("[a-zA-Z0-9_]{1,15}") &&
            !editingList.getUserNames().contains(userNameToAdd);
    }
    
    @OnFunction
    static void deleteList(TwitterModel model) {
        final List<Tweeters> sl = model.getSavedLists();
        sl.remove(model.getActiveTweeters());
        if (sl.isEmpty()) {
            final Tweeters t = new Tweeters();
            t.setName("New");
            sl.add(t);
        }
        model.setActiveTweetersName(sl.get(0).getName());
    }
    
    @OnFunction
    static void saveChanges(TwitterModel model) {
        Tweeters t = findByName(model.getSavedLists(), model.getActiveTweetersName());
        int indx = model.getSavedLists().indexOf(t);
        assert indx != -1;
        model.getSavedLists().set(indx, model.getModifiedList());
        model.setModifiedList(null);
    }
    
    @OnFunction
    static void addUser(TwitterModel model) {
        String n = model.getUserNameToAdd();
        findModifiedList(model).getUserNames().add(n);
    }
    @OnFunction
    static void removeUser(String data, TwitterModel model) {
        findModifiedList(model).getUserNames().remove(data);
    }
    
    private static Tweeters findModifiedList(TwitterModel model) {
        if (model.getModifiedList() == null || model.getModifiedList().getName() == null) {
            model.setModifiedList(model.getActiveTweeters().clone());
        }
        return model.getModifiedList();
    }
    private static Tweeters findByName(List<Tweeters> list, String name) {
        for (Tweeters l : list) {
            if (l.getName() != null && l.getName().equals(name)) {
                return l;
            }
        }
        return list.isEmpty() ? null : list.get(0);
    }
}
