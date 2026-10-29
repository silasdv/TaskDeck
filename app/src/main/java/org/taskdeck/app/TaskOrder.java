package org.taskdeck.app;

import org.json.JSONObject;
import java.util.*;
import java.util.function.Predicate;

public final class TaskOrder {
    private TaskOrder(){}
    public static boolean mandatory(JSONObject task){ return task.optBoolean("mandatoryProtected",!task.optBoolean("removable")&&!task.optBoolean("userProtected")); }
    public static List<JSONObject> arrange(List<JSONObject> source,String mode,Predicate<JSONObject> locked){
        List<JSONObject> result=new ArrayList<>(); for(JSONObject task:source) if(!mandatory(task)||task.optBoolean("launcher")) result.add(task);
        result.sort((a,b)->{
            int group=Boolean.compare(locked.test(a),locked.test(b)); if(group!=0) return group;
            int order=0;
            if("ram".equals(mode)) order=Long.compare(b.optLong("pssKb",-1),a.optLong("pssKb",-1));
            else if("alphabetical".equals(mode)) order=String.CASE_INSENSITIVE_ORDER.compare(a.optString("label"),b.optString("label"));
            if(order==0) order=Integer.compare(a.optInt("recentRank",Integer.MAX_VALUE),b.optInt("recentRank",Integer.MAX_VALUE));
            return order;
        }); return result;
    }
}
