package org.taskdeck.app;
import org.json.*;
public final class OperationResults {
    private OperationResults(){}
    public static JSONObject item(JSONObject task,String action,JSONObject result){
        try { return new JSONObject().put("id",task.optInt("id")).put("label",task.optString("label")).put("package",task.optString("package")).put("action",action)
            .put("status",result.optBoolean("uncertain")?"unverified":result.has("error")?"failed":result.optBoolean("verified",true)?"confirmed":"unverified")
            .put("message",result.optString("error",result.optString("warning",result.optBoolean("reopened")?"Android reopened the launcher":"Confirmed"))); }
        catch(JSONException impossible){ throw new IllegalArgumentException(impossible); }
    }
    public static JSONObject report(JSONArray items){
        int confirmed=0,failed=0,uncertain=0; StringBuilder summary=new StringBuilder();
        for(int i=0;i<items.length();i++){ JSONObject item=items.optJSONObject(i); if(item==null) continue;
            String state=item.optString("status"); if(state.equals("confirmed")) confirmed++; else { if(state.equals("failed")) failed++; else uncertain++;
                if(summary.length()>0) summary.append("; "); summary.append(item.optString("label")).append(": ").append(item.optString("message")); }
        }
        try { return new JSONObject().put("items",items).put("confirmed",confirmed).put("failed",failed).put("unverified",uncertain)
            .put("summary",confirmed+" confirmed"+(failed>0?" · "+failed+" failed":"")+(uncertain>0?" · "+uncertain+" unverified":"")+(summary.length()>0?" · "+summary:"")); }
        catch(JSONException impossible){ throw new IllegalArgumentException(impossible); }
    }
}
