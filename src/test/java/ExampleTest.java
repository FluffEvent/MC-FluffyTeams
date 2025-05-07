import java.util.ArrayList;
import java.util.List;

import junit.framework.*;

public class ExampleTest extends TestCase {

    protected void setUp() {
        // No setup needed
    }

    /**
     * This is a dummy test
     * it should not end up in plugin releases
     */
    public void testExample() {
        List<String> testList = new ArrayList<>();
        testList.add("test");
        assertEquals(testList.size(), 1);
    }
}
