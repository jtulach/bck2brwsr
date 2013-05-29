package ${package};

import org.apidesign.bck2brwsr.vmtest.BrwsrTest;
import org.apidesign.bck2brwsr.vmtest.Http;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

public class TwitterProtocolTest {
    private TwitterModel page;
    @Http(@Http.Resource(
        path = "/search.json",
        mimeType = "application/json",
        parameters = {"callback"},
        content = "$0({\"completed_in\":0.04,\"max_id\":320055706885689344,\"max_id_str\""
        + ":\"320055706885689344\",\"page\":1,\"query\":\"from%3AJaroslavTulach\",\"refresh_url\":"
        + "\"?since_id=320055706885689344&q=from%3AJaroslavTulach\","
        + "\"results\":[{\"created_at\":\"Fri, 05 Apr 2013 06:10:01 +0000\","
        + "\"from_user\":\"JaroslavTulach\",\"from_user_id\":420944648,\"from_user_id_str\":"
        + "\"420944648\",\"from_user_name\":\"Jaroslav Tulach\",\"geo\":null,\"id\":320055706885689344,"
        + "\"id_str\":\"320055706885689344\",\"iso_language_code\":\"en\",\"metadata\":{\"result_type\":"
        + "\"recent\"},\"profile_image_url\":\"http:\\/\\/a0.twimg.com\\/profile_images\\/1656828312\\/jst_normal.gif\","
        + "\"profile_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_images\\/1656828312\\/jst_normal.gif\","
        + "\"source\":\"&lt;a href=&quot;http:\\/\\/twitter.com\\/&quot;&gt;web&lt;\\/a&gt;\",\"text\":"
        + "\"@tom_enebo Amzng! Not that I would like #ruby, but I am really glad you guys stabilized the plugin + "
        + "made it work in #netbeans 7.3! Gd wrk.\",\"to_user\":\"tom_enebo\",\"to_user_id\":14498747,"
        + "\"to_user_id_str\":\"14498747\",\"to_user_name\":\"tom_enebo\",\"in_reply_to_status_id\":319832359509839872,"
        + "\"in_reply_to_status_id_str\":\"319832359509839872\"},{\"created_at\":\"Thu, 04 Apr 2013 07:33:06 +0000\","
        + "\"from_user\":\"JaroslavTulach\",\"from_user_id\":420944648,\"from_user_id_str\":"
        + "\"420944648\",\"from_user_name\":\"Jaroslav Tulach\",\"geo\":null,\"id\":319714227088678913,"
        + "\"id_str\":\"319714227088678913\",\"iso_language_code\":\"en\",\"metadata\":{\"result_type\":"
        + "\"recent\"},\"profile_image_url\":\"http:\\/\\/a0.twimg.com\\/profile_images\\/1656828312\\/jst_normal.gif\","
        + "\"profile_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_images\\/1656828312\\/jst_normal.gif\","
        + "\"source\":\"&lt;a href=&quot;http:\\/\\/twitter.com\\/&quot;&gt;web&lt;\\/a&gt;\",\"text\":"
        + "\"RT @drkrab: At #erlangfactory @joerl: Frameworks grow in complexity until nobody can use them.\"},"
        + "{\"created_at\":\"Tue, 02 Apr 2013 07:44:34 +0000\",\"from_user\":\"JaroslavTulach\","
        + "\"from_user_id\":420944648,\"from_user_id_str\":\"420944648\",\"from_user_name\":\"Jaroslav Tulach\","
        + "\"geo\":null,\"id\":318992336145248256,\"id_str\":\"318992336145248256\",\"iso_language_code\":\"en\","
        + "\"metadata\":{\"result_type\":\"recent\"},\"profile_image_url\":"
        + "\"http:\\/\\/a0.twimg.com\\/profile_images\\/1656828312\\/jst_normal.gif\","
        + "\"profile_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_images\\/1656828312\\/jst_normal.gif\","
        + "\"source\":\"&lt;a href=&quot;http:\\/\\/twitter.com\\/&quot;&gt;web&lt;\\/a&gt;\",\"text\":"
        + "\"Twitter renamed to twttr http:\\/\\/t.co\\/tqaN4T1xlZ - good, I don't have to rename #bck2brwsr!\"},"
        + "{\"created_at\":\"Sun, 31 Mar 2013 03:52:04 +0000\",\"from_user\":\"JaroslavTulach\",\"from_user_id\":420944648,"
        + "\"from_user_id_str\":\"420944648\",\"from_user_name\":\"Jaroslav Tulach\",\"geo\":null,"
        + "\"id\":318209051223789568,\"id_str\":\"318209051223789568\",\"iso_language_code\":\"en\",\"metadata\":"
        + "{\"result_type\":\"recent\"},\"profile_image_url\":"
        + "\"http:\\/\\/a0.twimg.com\\/profile_images\\/1656828312\\/jst_normal.gif\","
        + "\"profile_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_images\\/1656828312\\/jst_normal.gif\","
        + "\"source\":\"&lt;a href=&quot;http:\\/\\/twitter.com\\/&quot;&gt;web&lt;\\/a&gt;\",\"text\":"
        + "\"Math proofs without words. Ingenious: http:\\/\\/t.co\\/sz7yVbfpGw\"}],\"results_per_page\":100,"
        + "\"since_id\":0,\"since_id_str\":\"0\"})"
    ))
    @BrwsrTest public void readFromTwttr() throws InterruptedException {
        if (page == null) {
            page = new TwitterModel();
            page.applyBindings();
            page.queryTweets("", "q=xyz");
        }

        if (page.getCurrentTweets().isEmpty()) {
            throw new InterruptedException();
        }

        assert 4 == page.getCurrentTweets().size() : "Four tweets: " + page.getCurrentTweets();
        
        String firstDate = page.getCurrentTweets().get(0).getCreated_at();
        assert "Fri, 05 Apr 2013 06:10:01 +0000".equals(firstDate) : "Date is OK: " + firstDate;
    }
    
    @Factory public static Object[] create() {
        return VMTest.create(TwitterProtocolTest.class);
    }
}
