package org.taskdeck.app;
import org.json.*;
import java.util.*;

/** Groups real task windows without summing identical package-wide RAM samples. */
public final class TaskGroups {
    private TaskGroups(){}
    public static List<JSONObject> group(List<JSONObject> source,boolean enabled){
        if(!enabled) return new ArrayList<>(source);
        Map<String,JSONObject> groups=new LinkedHashMap<>();
        for(JSONObject task:source){
            String name=task.optString("package");
            try {
                JSONObject group=groups.get(name);
                if(group==null){ group=new JSONObject(task.toString()); group.put("members",new JSONArray()); groups.put(name,group); }
                group.getJSONArray("members").put(task);
                group.put("windowCount",group.getJSONArray("members").length());
                if(TaskOrder.mandatory(task)) group.put("mandatoryProtected",true);
            } catch(JSONException impossible){ throw new IllegalArgumentException("Invalid task data",impossible); }
        } return new ArrayList<>(groups.values());
    }
    public static List<JSONObject> expand(JSONObject group){
        JSONArray members=group.optJSONArray("members"); List<JSONObject> result=new ArrayList<>();
        if(members==null) result.add(group); else for(int i=0;i<members.length();i++) if(members.optJSONObject(i)!=null) result.add(members.optJSONObject(i));
        return result;
    }
}
