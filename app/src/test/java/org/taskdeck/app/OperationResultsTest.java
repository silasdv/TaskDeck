package org.taskdeck.app;
import org.json.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=33)
public class OperationResultsTest {
    private JSONObject task(String label) throws Exception { return new JSONObject().put("id",1).put("label",label).put("package",label); }
    @Test public void everyFailureIsNamedAndUnverifiedIsNotSuccessful() throws Exception {
        JSONArray items=new JSONArray().put(OperationResults.item(task("A"),"Close",new JSONObject().put("verified",true)))
            .put(OperationResults.item(task("B"),"Close",BackendRequests.error("Denied")))
            .put(OperationResults.item(task("C"),"Close",new JSONObject().put("verified",false).put("warning","Still present")));
        JSONObject report=OperationResults.report(items); assertEquals(1,report.getInt("confirmed")); assertEquals(1,report.getInt("failed")); assertEquals(1,report.getInt("unverified"));
        assertTrue(report.getString("summary").contains("B: Denied")); assertTrue(report.getString("summary").contains("C: Still present")); assertEquals(3,report.getJSONArray("items").length());
    }
    @Test public void timeoutOutcomeRemainsUnknown() throws Exception {
        JSONObject item=OperationResults.item(task("A"),"Close",BackendRequests.uncertain("Timed out")); assertEquals("unverified",item.getString("status"));
    }
}
