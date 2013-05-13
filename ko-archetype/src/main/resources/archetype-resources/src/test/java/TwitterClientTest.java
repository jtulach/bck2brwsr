package ${package};

import java.util.List;
import net.java.html.json.Context;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** We can unit test the TwitterModel smoothly.
 */
public class TwitterClientTest {
    private TwitterModel model;
    

    @BeforeMethod
    public void initModel() {
        model = new TwitterModel(Context.EMPTY);
    }

    @Test public void testIsValidToAdd() {
        model.setUserNameToAdd("Joe");
        Tweeters t = new Tweeters(Context.EMPTY);
        t.setName("test");
        model.getSavedLists().add(t);
        model.setActiveTweetersName("test");
        
        assertTrue(model.isUserNameToAddIsValid(), "Joe is OK");
        TwitterClient.addUser(model);
        assertFalse(model.isUserNameToAddIsValid(), "Can't add Joe for the 2nd time");
        assertEquals(t.getUserNames().size(), 0, "Original tweeters list remains empty");
        
        List<String> mod = model.getActiveTweeters();
        assertTrue(model.isHasUnsavedChanges(), "We have modifications");
        assertEquals(mod.size(), 1, "One element in the list");
        assertEquals(mod.get(0), "Joe", "Its name is Joe");
        
        assertSame(model.getActiveTweeters(), mod, "Editing list is the modified one");
        
        TwitterClient.saveChanges(model);
        assertFalse(model.isHasUnsavedChanges(), "Does not have anything to save");
        
        assertSame(model.getActiveTweeters(), mod, "Still editing the old modified one");
    }
    
    @Test public void httpAtTheEnd() {
        String res = TwitterClient.Twt.html("Ahoj http://kuk");
        assertEquals(res, "Ahoj <a href='http://kuk'>http://kuk</a>");
    }
}
