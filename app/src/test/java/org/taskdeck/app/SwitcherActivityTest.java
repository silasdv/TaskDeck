package org.taskdeck.app;

import android.os.Looper;
import android.view.*;
import android.widget.PopupWindow;
import org.json.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;

/** Fake IPC verifies input and UI behavior only, not Retroid/Shizuku permissions. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=33,application=SwitcherActivityTest.TestApp.class)
public class SwitcherActivityTest {
    public static class TestApp extends TaskDeckApp {
        boolean separateMemory; int recoveries;
        @Override public void onCreate(){}
        @Override public void connect(boolean request){}
        @Override public void verifyLiveAccess(){}
        @Override protected java.util.concurrent.ExecutorService memoryExecutor(){ return separateMemory?memoryWorker:worker; }
        @Override public void recoverBackend(String reason){ recoveries++; super.recoverBackend(reason); }
    }
    static class FakeBackend extends ITaskBackend.Stub {
        final List<Integer> removed=new ArrayList<>(); final Set<String> protectedApps=new HashSet<>(),unlockedLaunchers=new HashSet<>(); final List<Boolean> launcherStops=new ArrayList<>(); int opened=-1,reads,directSwitches,taskCount=20,entryId=7; boolean extra,many,feedbackBeforeOpen,duplicates; int failId=-1,unverifiedId=-1;
        java.util.concurrent.CountDownLatch memoryStarted,releaseMemory;
        public synchronized void setProtectedPackages(String[] names){ protectedApps.clear(); protectedApps.addAll(Arrays.asList(names)); }
        public synchronized void setUnlockedLaunchers(String[] names){ unlockedLaunchers.clear(); unlockedLaunchers.addAll(Arrays.asList(names)); }
        public synchronized String stopLauncher(String name,boolean restart){ if(!name.equals("wave.launcher")||!restart&&!unlockedLaunchers.contains(name)) return "{\"error\":\"protected\"}"; launcherStops.add(restart); if(!removed.contains(9)) removed.add(9); return "{\"verified\":true,\"previousPids\":[111]}"; }
        public String verifyLauncherRestart(String name,int[] previous){ return "{\"verified\":true,\"newPids\":[222]}"; }
        public String switchPreviousTask(){ directSwitches++; return "{\"switched\":true}"; }
        public String readMemory(){
            try { if(memoryStarted!=null) memoryStarted.countDown(); if(releaseMemory!=null) releaseMemory.await();
                JSONArray values=new JSONArray(); for(int id=7;id<27;id++) values.put(new JSONObject().put("package","test.game"+id).put("pssKb",id*1024).put("memoryState","sampled"));
                return new JSONObject().put("samples",values).put("backgroundApps",new JSONArray().put(new JSONObject().put("label","Test Keyboard").put("pssKb",51200))).toString();
            } catch(Exception error){ return "{\"error\":\"RAM unavailable\"}"; }
        }
        public synchronized String readTasks(){
            reads++;
            try {
                JSONArray rows=new JSONArray();
                for(int id:duplicates?new int[]{7,10,8,9}:many?java.util.stream.IntStream.range(7,7+taskCount).toArray():extra?new int[]{7,8,10,11,9}:new int[]{7,8,9}) if(!removed.contains(id)){
                    String name=id==9?"wave.launcher":"test.game"+(duplicates&&id==10?7:id);
                    rows.put(new JSONObject().put("id",id).put("package",name).put("label",id==9?"Wave Launcher":"Game "+id)
                        .put("launcher",id==9).put("mandatoryProtected",false).put("userProtected",protectedApps.contains(name)).put("removable",(id!=9||unlockedLaunchers.contains(name))&&!protectedApps.contains(name)));
                }
                return new JSONObject().put("uid",2000).put("sdk",33).put("tasks",rows).put("backgroundApps",new JSONArray().put(new JSONObject().put("label","Test Keyboard").put("pssKb",51200).put("reason","Protected service"))).put("previousTask",new JSONObject().put("id",entryId).put("package","test.game"+entryId)).toString();
            } catch(JSONException e){ throw new RuntimeException(e); }
        }
        public synchronized String openTask(int id,String name){ feedbackBeforeOpen=!((TaskDeckApp)RuntimeEnvironment.getApplication()).lastMemoryMessage.isEmpty(); opened=id; return "{}"; }
        public synchronized String removeTask(int id,String name){ if(id==9||protectedApps.contains(name)||id==failId) return "{\"error\":\"Permission denied for test app\"}"; if(id==unverifiedId) return "{\"verified\":false,\"warning\":\"Still reported by Android\"}"; removed.add(id); return "{\"verified\":true}"; }
        public void destroy(){}
    }
    private TestApp app; private FakeBackend backend;
    private org.robolectric.android.controller.ActivityController<SwitcherActivity> start() throws Exception {
        app=(TestApp)RuntimeEnvironment.getApplication(); backend=new FakeBackend(); app.backend=backend;
        app.getSharedPreferences("settings",0).edit().putBoolean("setupComplete",true).putString("view","cards").commit();
        var controller=Robolectric.buildActivity(SwitcherActivity.class).setup().visible(); controller.windowFocusChanged(true); drain(); return controller;
    }
    private void drain() throws Exception { for(int i=0;i<6;i++){ app.worker.submit(()->{}).get(5,TimeUnit.SECONDS); Shadows.shadowOf(Looper.getMainLooper()).idle(); } }
    private void press(SwitcherActivity activity,int key){ activity.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,key)); activity.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,key)); }
    private Object field(SwitcherActivity activity,String name) throws Exception { var f=SwitcherActivity.class.getDeclaredField(name); f.setAccessible(true); return f.get(activity); }
    private void end(org.robolectric.android.controller.ActivityController<SwitcherActivity> controller){ controller.pause().stop().destroy(); app.worker.shutdownNow(); app.memoryWorker.shutdownNow(); }

    @Test public void dpadSelectsAndOpensSecondCardWithoutTouchOrNativeListFocus() throws Exception {
        var controller=start(); SwitcherActivity activity=controller.get(); activity.getWindow().getDecorView().clearFocus();
        press(activity,KeyEvent.KEYCODE_DPAD_RIGHT);
        assertEquals(Integer.valueOf(8),((DeckSelection)field(activity,"selection")).id());
        press(activity,KeyEvent.KEYCODE_BUTTON_A); drain(); assertEquals(8,backend.opened); assertTrue(activity.isFinishing()); end(controller);
    }
    @Test public void xClosesSelectedCardImmediatelyAndRetainsNeighborSelection() throws Exception {
        var controller=start(); SwitcherActivity activity=controller.get(); press(activity,KeyEvent.KEYCODE_DPAD_RIGHT); press(activity,KeyEvent.KEYCODE_BUTTON_X); drain();
        assertEquals(List.of(8),backend.removed); assertEquals(Integer.valueOf(9),((DeckSelection)field(activity,"selection")).id()); end(controller);
    }
    @Test public void selectOpensLockPopupAndAProtectsAgainstCloseAll() throws Exception {
        var controller=start(); SwitcherActivity activity=controller.get(); press(activity,KeyEvent.KEYCODE_BUTTON_SELECT);
        PopupWindow popup=(PopupWindow)field(activity,"protectionPopup"); assertTrue(popup.isShowing());
        popup.getContentView().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BUTTON_A)); drain();
        assertTrue(app.getSharedPreferences("settings",0).getStringSet("protectedApps",Set.of()).contains("test.game7"));
        press(activity,KeyEvent.KEYCODE_BUTTON_Y); drain(); assertEquals(List.of(8),backend.removed); end(controller);
    }
    @Test public void leftStickSelectsAndNeutralInputStopsRepeat() throws Exception {
        var controller=start(); SwitcherActivity activity=controller.get();
        MotionEvent.PointerProperties p=new MotionEvent.PointerProperties(); p.id=0;
        MotionEvent.PointerCoords c=new MotionEvent.PointerCoords(); c.setAxisValue(MotionEvent.AXIS_X,1f);
        MotionEvent event=MotionEvent.obtain(0,1,MotionEvent.ACTION_MOVE,1,new MotionEvent.PointerProperties[]{p},new MotionEvent.PointerCoords[]{c},0,0,1,1,7,0,InputDevice.SOURCE_JOYSTICK,0);
        activity.onGenericMotionEvent(event); event.recycle();
        assertEquals(Integer.valueOf(8),((DeckSelection)field(activity,"selection")).id());
        c.setAxisValue(MotionEvent.AXIS_X,0f); event=MotionEvent.obtain(0,2,MotionEvent.ACTION_MOVE,1,new MotionEvent.PointerProperties[]{p},new MotionEvent.PointerCoords[]{c},0,0,1,1,7,0,InputDevice.SOURCE_JOYSTICK,0);
        activity.onGenericMotionEvent(event); event.recycle(); assertEquals(0,field(activity,"stickDirection")); end(controller);
    }
    @Test public void macroRecorderCapturesTwoSimultaneousButtonsAndSavesWithStart() throws Exception {
        var controller=start(); SwitcherActivity activity=controller.get();
        var method=SwitcherActivity.class.getDeclaredMethod("show",SwitcherActivity.Page.class); method.setAccessible(true); method.invoke(activity,SwitcherActivity.Page.CHORD);
        int first=KeyEvent.KEYCODE_F8,second=KeyEvent.KEYCODE_F9;
        activity.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,first)); activity.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,second));
        assertEquals(2,((Set<?>)field(activity,"capturedKeys")).size());
        activity.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,first)); activity.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,second)); press(activity,KeyEvent.KEYCODE_BUTTON_START);
        assertEquals(2,ShortcutKeys.decode(app.getSharedPreferences("settings",0).getString("shortcutChord","")).size());
        assertFalse(TaskDeckApp.recordingShortcut); end(controller);
    }
    @Test public void longPressSelectsCardAndUnlockingRestoresItsCloseAction() throws Exception {
        var controller=start(); SwitcherActivity activity=controller.get();
        @SuppressWarnings("unchecked") List<View> cards=(List<View>)field(activity,"cards"); cards.get(1).performLongClick();
        PopupWindow popup=(PopupWindow)field(activity,"protectionPopup"); assertTrue(popup.isShowing());
        popup.getContentView().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BUTTON_A)); drain();
        assertTrue(app.getSharedPreferences("settings",0).getStringSet("protectedApps",Set.of()).contains("test.game8"));
        press(activity,KeyEvent.KEYCODE_BUTTON_SELECT); popup=(PopupWindow)field(activity,"protectionPopup");
        popup.getContentView().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BUTTON_A)); drain();
        assertFalse(app.getSharedPreferences("settings",0).getStringSet("protectedApps",Set.of()).contains("test.game8"));
        press(activity,KeyEvent.KEYCODE_BUTTON_X); drain(); assertEquals(List.of(8),backend.removed); end(controller);
    }
    @Test @Config(qualifiers="w800dp-h360dp-land") public void landscapeShowsMoreThanTwoCardsWithoutClippingTheirContents() throws Exception {
        var controller=start(); SwitcherActivity activity=controller.get();
        backend.extra=true; invoke(activity,"refresh"); drain();
        @SuppressWarnings("unchecked") List<View> cards=(List<View>)field(activity,"cards");
        android.widget.HorizontalScrollView rail=(android.widget.HorizontalScrollView)field(activity,"rail");
        int visible=0;
        for(View card:cards){
            if(card.getRight()<=rail.getWidth()) visible++;
            android.widget.LinearLayout contents=(android.widget.LinearLayout)card;
            assertTrue("Card content clipped at "+card.getHeight(),contents.getChildAt(contents.getChildCount()-1).getBottom()<=card.getHeight());
        }
        assertTrue("Only "+visible+" cards visible",visible>2); end(controller);
    }
    private void invoke(SwitcherActivity activity,String name) throws Exception { var method=SwitcherActivity.class.getDeclaredMethod(name); method.setAccessible(true); method.invoke(activity); }
    private void show(SwitcherActivity activity,SwitcherActivity.Page page) throws Exception { var method=SwitcherActivity.class.getDeclaredMethod("show",SwitcherActivity.Page.class); method.setAccessible(true); method.invoke(activity,page); Shadows.shadowOf(Looper.getMainLooper()).idle(); }
    private android.widget.Button findButton(View view,String text){ if(view instanceof android.widget.Button && text.equals(((android.widget.Button)view).getText().toString())) return (android.widget.Button)view; if(view instanceof ViewGroup) for(int i=0;i<((ViewGroup)view).getChildCount();i++){ var found=findButton(((ViewGroup)view).getChildAt(i),text); if(found!=null) return found; } return null; }
    @Test public void closeOthersKeepsEntryAppRegardlessOfSelectedCardAndExits() throws Exception {
        var controller=start(); var activity=controller.get(); press(activity,KeyEvent.KEYCODE_DPAD_RIGHT); press(activity,KeyEvent.KEYCODE_BUTTON_R1); drain();
        assertEquals(List.of(8),backend.removed); assertEquals(7,backend.opened); assertTrue(backend.feedbackBeforeOpen); assertTrue(activity.isFinishing()); end(controller);
    }
    @Test public void closeAllGoesHomeEvenWhenThereIsNothingToClose() throws Exception {
        var controller=start(); var activity=controller.get(); backend.removed.addAll(List.of(7,8)); invoke(activity,"refresh"); drain();
        assertEquals(1,((List<?>)field(activity,"cards")).size()); press(activity,KeyEvent.KEYCODE_BUTTON_Y); drain();
        assertTrue(activity.isFinishing()); assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().hasCategory(android.content.Intent.CATEGORY_HOME)); end(controller);
    }
    @Test public void bulkStayOpenPreferenceWorks() throws Exception {
        var controller=start(); var activity=controller.get(); app.getSharedPreferences("settings",0).edit().putBoolean("exitAfterBulk",false).commit();
        press(activity,KeyEvent.KEYCODE_BUTTON_Y); drain(); assertEquals(List.of(7,8),backend.removed); assertFalse(activity.isFinishing()); assertEquals(1,((List<?>)field(activity,"cards")).size()); end(controller);
    }
    @Test public void newOpeningResetsSelectionToFirstCard() throws Exception {
        var controller=start(); var activity=controller.get(); press(activity,KeyEvent.KEYCODE_DPAD_RIGHT); activity.onNewIntent(new android.content.Intent(activity,SwitcherActivity.class)); drain();
        assertEquals(Integer.valueOf(7),((DeckSelection)field(activity,"selection")).id()); end(controller);
    }
    @Test public void recorderReservesNavigationAndConfirmCanSave() throws Exception {
        var controller=start(); var activity=controller.get(); show(activity,SwitcherActivity.Page.CHORD);
        press(activity,KeyEvent.KEYCODE_DPAD_DOWN); assertTrue(((Set<?>)field(activity,"capturedKeys")).isEmpty());
        press(activity,KeyEvent.KEYCODE_F8); assertEquals(1,((Set<?>)field(activity,"capturedKeys")).size());
        press(activity,KeyEvent.KEYCODE_BUTTON_A); assertEquals(SwitcherActivity.Page.SHORTCUTS,field(activity,"page"));
        assertEquals(Set.of("138:0"),ShortcutKeys.decode(app.getSharedPreferences("settings",0).getString("shortcutChord",""))); end(controller);
    }
    @Test public void touchActivatesThemeOnFirstTapAndControllerCanResume() throws Exception {
        var controller=start(); var activity=controller.get(); show(activity,SwitcherActivity.Page.SETUP);
        for(int i=0;i<5;i++) findButton(activity.getWindow().getDecorView(),"Next step").performClick();
        var button=findButton(activity.getWindow().getDecorView(),"Light"); button.requestRectangleOnScreen(new android.graphics.Rect(0,0,button.getWidth(),button.getHeight()),true); Shadows.shadowOf(Looper.getMainLooper()).idle();
        int[] xy=new int[2]; button.getLocationOnScreen(xy); long now=android.os.SystemClock.uptimeMillis();
        MotionEvent down=MotionEvent.obtain(now,now,MotionEvent.ACTION_DOWN,xy[0]+button.getWidth()/2f,xy[1]+button.getHeight()/2f,0);
        MotionEvent up=MotionEvent.obtain(now,now+50,MotionEvent.ACTION_UP,xy[0]+button.getWidth()/2f,xy[1]+button.getHeight()/2f,0);
        activity.dispatchTouchEvent(down); activity.dispatchTouchEvent(up); down.recycle(); up.recycle(); Shadows.shadowOf(Looper.getMainLooper()).idle();
        assertEquals("light",app.getSharedPreferences("settings",0).getString("theme","dark")); assertEquals(false,field(activity,"controllerMode"));
        press(activity,KeyEvent.KEYCODE_DPAD_DOWN); assertEquals(true,field(activity,"controllerMode"));
        List<?> controls=(List<?>)field(activity,"controls"); assertTrue(((View)controls.get((Integer)field(activity,"controlIndex"))).isActivated()); end(controller);
    }
    @Test public void notificationShortcutCanBeShownAndHidden() throws Exception {
        var controller=start(); var activity=controller.get(); Shadows.shadowOf(app).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS); show(activity,SwitcherActivity.Page.SETTINGS);
        findButton(activity.getWindow().getDecorView(),"Notifications").performClick();
        findButton(activity.getWindow().getDecorView(),"Notification shortcut · Off").performClick();
        assertNotNull(findButton(activity.getWindow().getDecorView(),"Notification shortcut · On"));
        findButton(activity.getWindow().getDecorView(),"Notification shortcut · On").performClick();
        assertNotNull(findButton(activity.getWindow().getDecorView(),"Notification shortcut · Off"));
        assertEquals(0,activity.getSystemService(android.app.NotificationManager.class).getActiveNotifications().length); end(controller);
    }
    @Test public void sortingPopupWorksWithDpadAndConfirmWithoutTouch() throws Exception {
        var controller=start(); var activity=controller.get(); invoke(activity,"sortMenu"); PopupWindow popup=(PopupWindow)field(activity,"protectionPopup");
        popup.getContentView().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_DOWN));
        popup.getContentView().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BUTTON_A));
        assertEquals("ram",app.getSharedPreferences("settings",0).getString("sort","recent")); assertFalse(popup.isShowing()); end(controller);
    }
    @Test public void protectedOnlyOpeningSelectsLeftmostAppAndToolbarRemainsReachable() throws Exception {
        var controller=start(); var activity=controller.get(); backend.removed.addAll(List.of(7,8)); activity.onNewIntent(new android.content.Intent(activity,SwitcherActivity.class).putExtra("controllerEntry",true)); drain();
        assertEquals(false,field(activity,"toolZone")); assertEquals(Integer.valueOf(9),((DeckSelection)field(activity,"selection")).id());
        press(activity,KeyEvent.KEYCODE_DPAD_UP); assertEquals(true,field(activity,"toolZone")); assertEquals(0,field(activity,"toolIndex"));
        press(activity,KeyEvent.KEYCODE_DPAD_RIGHT); press(activity,KeyEvent.KEYCODE_BUTTON_A); PopupWindow popup=(PopupWindow)field(activity,"protectionPopup"); assertTrue(popup.isShowing()); popup.dismiss(); end(controller);
    }
    @Test public void trulyEmptyDeckNeverLosesToolbarSelection() throws Exception {
        var controller=start(); var activity=controller.get(); backend.removed.addAll(List.of(7,8,9)); activity.onNewIntent(new android.content.Intent(activity,SwitcherActivity.class)); drain();
        press(activity,KeyEvent.KEYCODE_DPAD_DOWN); assertEquals(true,field(activity,"dockZone")); press(activity,KeyEvent.KEYCODE_DPAD_UP); assertEquals(true,field(activity,"toolZone")); press(activity,KeyEvent.KEYCODE_DPAD_RIGHT); assertEquals(1,field(activity,"toolIndex"));
        press(activity,KeyEvent.KEYCODE_DPAD_LEFT); int reads=backend.reads; press(activity,KeyEvent.KEYCODE_BUTTON_A); drain(); assertTrue(backend.reads>reads); end(controller);
    }
    @Test public void sidebarExplainsBackgroundMemoryWithoutOfferingToDismissAService() throws Exception {
        var controller=start(); var activity=controller.get(); String value=((android.widget.TextView)field(activity,"backgroundInfo")).getText().toString();
        assertTrue(value.contains("Can't close here")); assertTrue(value.contains("Test Keyboard · ~52 MB")); assertTrue(value.contains("cache also use RAM"));
        assertEquals(3,((List<?>)field(activity,"cards")).size()); end(controller);
    }
    @Test public void protectedHomeCardOpensViaHomeIntentInsteadOfRecentsApi() throws Exception {
        var controller=start(); var activity=controller.get(); backend.removed.addAll(List.of(7,8)); invoke(activity,"refresh"); drain();
        @SuppressWarnings("unchecked") List<JSONObject> tasks=(List<JSONObject>)field(activity,"tasks"); tasks.get(0).put("home",true);
        press(activity,KeyEvent.KEYCODE_BUTTON_A);
        assertEquals(-1,backend.opened); assertTrue(activity.isFinishing()); assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().hasCategory(android.content.Intent.CATEGORY_HOME)); end(controller);
    }
    @Test @Config(qualifiers="w800dp-h360dp-land") public void everyLandscapeSetupScreenFitsWithoutScrolling() throws Exception {
        var controller=start(); var activity=controller.get(); show(activity,SwitcherActivity.Page.SETUP);
        for(int step=0;step<8;step++){
            assertNull(field(activity,"pageScroll")); layout(activity); assertFits((View)field(activity,"formMain")); assertFits((View)field(activity,"formInfo"));
            if(step<7) findButton(activity.getWindow().getDecorView(),"Next step").performClick();
        }
        for(var page:List.of(SwitcherActivity.Page.CONNECT,SwitcherActivity.Page.CHORD,SwitcherActivity.Page.CONFIRM,SwitcherActivity.Page.SHORTCUTS,SwitcherActivity.Page.SETTINGS,SwitcherActivity.Page.DOCKSTYLE,SwitcherActivity.Page.DOCKBEHAVIOR,SwitcherActivity.Page.PICKER,SwitcherActivity.Page.MAPPING,SwitcherActivity.Page.BIND,SwitcherActivity.Page.MEMORY,SwitcherActivity.Page.RESULTS)){
            show(activity,page); layout(activity); assertNull(field(activity,"pageScroll")); assertFits((View)field(activity,"formMain")); assertFits((View)field(activity,"formInfo"));
        }
        end(controller);
    }
    @Test @Config(qualifiers="w640dp-h360dp-land") public void compactLandscapeSetupAlsoFitsWithoutScrolling() throws Exception { everyLandscapeSetupScreenFitsWithoutScrolling(); }
    @Test public void launcherStartsLockedCanBeUnlockedClosedAndLockedAgain() throws Exception {
        var controller=start(); var activity=controller.get(); press(activity,KeyEvent.KEYCODE_DPAD_RIGHT); press(activity,KeyEvent.KEYCODE_DPAD_RIGHT);
        press(activity,KeyEvent.KEYCODE_BUTTON_X); drain(); assertTrue(backend.launcherStops.isEmpty());
        press(activity,KeyEvent.KEYCODE_BUTTON_SELECT); PopupWindow popup=(PopupWindow)field(activity,"protectionPopup");
        popup.getContentView().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BUTTON_A)); drain();
        assertTrue(app.getSharedPreferences("settings",0).getStringSet("unlockedLaunchers",Set.of()).contains("wave.launcher"));
        press(activity,KeyEvent.KEYCODE_BUTTON_SELECT); popup=(PopupWindow)field(activity,"protectionPopup");
        popup.getContentView().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BUTTON_A)); drain();
        assertFalse(app.getSharedPreferences("settings",0).getStringSet("unlockedLaunchers",Set.of()).contains("wave.launcher"));
        press(activity,KeyEvent.KEYCODE_BUTTON_SELECT); popup=(PopupWindow)field(activity,"protectionPopup"); popup.getContentView().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BUTTON_A)); drain();
        press(activity,KeyEvent.KEYCODE_BUTTON_X); drain(); assertEquals(List.of(false),backend.launcherStops); assertTrue(backend.removed.contains(9)); assertEquals(2,((List<?>)field(activity,"cards")).size()); end(controller);
    }
    @Test public void explicitLauncherRestartKeepsDefaultProtection() throws Exception {
        var controller=start(); var activity=controller.get(); press(activity,KeyEvent.KEYCODE_DPAD_RIGHT); press(activity,KeyEvent.KEYCODE_DPAD_RIGHT); press(activity,KeyEvent.KEYCODE_BUTTON_SELECT);
        PopupWindow popup=(PopupWindow)field(activity,"protectionPopup"); popup.getContentView().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_DOWN));
        popup.getContentView().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BUTTON_A)); drain();
        assertEquals(List.of(true),backend.launcherStops); assertTrue(app.getSharedPreferences("settings",0).getStringSet("unlockedLaunchers",Set.of()).isEmpty());
        assertEquals("wave.launcher",Shadows.shadowOf(activity).getNextStartedActivity().getPackage()); assertTrue(activity.isFinishing()); end(controller);
    }
    @Test public void ramFeedbackAppearsAfterActivityIsDestroyedWithoutNotificationPermission() throws Exception {
        var controller=start(); var activity=controller.get(); org.robolectric.shadows.ShadowToast.reset();
        Shadows.shadowOf(app).denyPermissions(android.Manifest.permission.POST_NOTIFICATIONS);
        press(activity,KeyEvent.KEYCODE_BUTTON_Y); drain(); assertTrue(activity.isFinishing()); controller.pause().stop().destroy();
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(1500,TimeUnit.MILLISECONDS);
        String message=org.robolectric.shadows.ShadowToast.getTextOfLatestToast(); assertNotNull(message); assertTrue(message.contains("RAM change:")); assertTrue(message.contains("Used:")); assertTrue(message.contains("MB")); app.worker.shutdownNow();
    }
    @Test public void ramFeedbackCanBeDisabledAndRapidReportsAreCoalesced() throws Exception {
        var controller=start(); org.robolectric.shadows.ShadowToast.reset(); app.getSharedPreferences("settings",0).edit().putBoolean("ramNotifications",false).commit();
        app.reportMemoryResult(1,0); Shadows.shadowOf(Looper.getMainLooper()).idleFor(1500,TimeUnit.MILLISECONDS); assertNull(org.robolectric.shadows.ShadowToast.getTextOfLatestToast());
        app.getSharedPreferences("settings",0).edit().putBoolean("ramNotifications",true).commit(); app.reportMemoryResult(1,0); app.reportMemoryResult(2,0);
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(1500,TimeUnit.MILLISECONDS); assertEquals(1,org.robolectric.shadows.ShadowToast.shownToastCount()); end(controller);
    }
    @Test @Config(qualifiers="w640dp-h360dp-land") public void allFiveFooterActionsFitOneVisibleRowWithoutLegend() throws Exception {
        var controller=start(); var activity=controller.get(); layout(activity);
        var open=findButton(activity.getWindow().getDecorView(),"Open"); ViewGroup row=(ViewGroup)open.getParent(); assertEquals(5,row.getChildCount());
        for(int i=0;i<5;i++) assertEquals(open.getTop(),row.getChildAt(i).getTop());
        for(int i=0;i<5;i++) assertTrue(row.getChildAt(i).getContentDescription().toString().startsWith(new String[]{"A:","B:","X:","Y:","R1:"}[i]));
        View last=row.getChildAt(4); View viewport=(View)row.getParent(); assertTrue("Footer requires horizontal scrolling: "+last.getRight()+" / "+viewport.getWidth(),last.getRight()<=viewport.getWidth());
        ViewGroup root=(ViewGroup)field(activity,"root"); assertSame(field(activity,"dock"),root.getChildAt(root.getChildCount()-1)); assertSame(root.getChildAt(0),viewport.getParent()); end(controller);
    }
    @Test @Config(qualifiers="w800dp-h360dp-land") public void setupShouldersMoveStepsAndNextIsRightAligned() throws Exception {
        var controller=start(); var activity=controller.get(); show(activity,SwitcherActivity.Page.SETUP); layout(activity);
        var next=findButton(activity.getWindow().getDecorView(),"Next step"); var parent=(ViewGroup)next.getParent(); assertEquals(parent.getWidth(),next.getRight()); assertTrue(next.getCompoundDrawables()[0] instanceof ControllerBadge);
        press(activity,KeyEvent.KEYCODE_BUTTON_L1); assertEquals(0,field(activity,"setupStep")); press(activity,KeyEvent.KEYCODE_BUTTON_R1); assertEquals(1,field(activity,"setupStep"));
        press(activity,KeyEvent.KEYCODE_BUTTON_L1); assertEquals(0,field(activity,"setupStep")); for(int i=0;i<8;i++) press(activity,KeyEvent.KEYCODE_BUTTON_R1);
        assertEquals(SwitcherActivity.Page.COMPLETE,field(activity,"page")); Shadows.shadowOf(Looper.getMainLooper()).idleFor(2000,TimeUnit.MILLISECONDS);
        drain(); assertEquals(SwitcherActivity.Page.APPS,field(activity,"page")); assertTrue(backend.removed.isEmpty()); end(controller);
    }
    private void changeView(SwitcherActivity activity,String view) throws Exception { app.getSharedPreferences("settings",0).edit().putString("view",view).commit(); show(activity,SwitcherActivity.Page.APPS); invoke(activity,"refresh"); drain(); layout(activity); }
    @Test @Config(qualifiers="w800dp-h360dp-land") public void gridShowsMoreAppsAndDpadMovesByRows() throws Exception {
        var controller=start(); var activity=controller.get(); backend.many=true; changeView(activity,"grid");
        List<?> cards=(List<?>)field(activity,"cards"); View viewport=(View)field(activity,"appContent"); int visible=0;
        for(Object value:cards){ View card=(View)value; if(card.getBottom()<=viewport.getHeight()&&card.getRight()<=viewport.getWidth()) visible++; }
        assertTrue("Only "+visible+" fully visible grid tiles",visible>=8); int columns=(Integer)field(activity,"gridColumns");
        press(activity,KeyEvent.KEYCODE_DPAD_RIGHT); press(activity,KeyEvent.KEYCODE_DPAD_DOWN); assertEquals(1+columns,((DeckSelection)field(activity,"selection")).index());
        Integer selected=((DeckSelection)field(activity,"selection")).id(); press(activity,KeyEvent.KEYCODE_BUTTON_A); drain(); assertEquals(selected.intValue(),backend.opened); end(controller);
    }
    @Test @Config(qualifiers="w800dp-h360dp-land") public void fitAllKeepsTwentyAppsInsideTheViewport() throws Exception {
        var controller=start(); var activity=controller.get(); backend.many=true; changeView(activity,"fit"); List<?> cards=(List<?>)field(activity,"cards"); assertEquals(20,cards.size());
        View viewport=(View)field(activity,"appContent");
        for(Object value:cards){ View card=(View)value; assertTrue("Tile clipped vertically",card.getBottom()<=viewport.getHeight()); assertTrue("Tile clipped horizontally",card.getRight()<=viewport.getWidth()); assertTrue(card.getHeight()>0); }
        assertNull(field(activity,"appScroll")); end(controller);
    }
    @Test public void listUsesVerticalSelectionAndViewMenuPersistsChoice() throws Exception {
        var controller=start(); var activity=controller.get(); invoke(activity,"viewMenu"); PopupWindow popup=(PopupWindow)field(activity,"protectionPopup");
        popup.getContentView().dispatchKeyEvent(new KeyEvent(0,KeyEvent.KEYCODE_DPAD_DOWN)); popup.getContentView().dispatchKeyEvent(new KeyEvent(0,KeyEvent.KEYCODE_BUTTON_A)); layout(activity);
        assertEquals("list",app.getSharedPreferences("settings",0).getString("view","")); assertEquals(1,field(activity,"gridColumns"));
        press(activity,KeyEvent.KEYCODE_DPAD_DOWN); assertEquals(Integer.valueOf(8),((DeckSelection)field(activity,"selection")).id()); end(controller);
    }
    @Test public void bulkExitPublishesRamFeedbackBeforeLeavingForeground() throws Exception {
        var controller=start(); var activity=controller.get(); org.robolectric.shadows.ShadowToast.reset(); press(activity,KeyEvent.KEYCODE_BUTTON_Y); drain();
        assertTrue(activity.isFinishing()); assertTrue(app.lastMemoryMessage.contains("RAM change:")); assertNotNull(org.robolectric.shadows.ShadowToast.getTextOfLatestToast());
        controller.pause().stop().destroy(); int count=org.robolectric.shadows.ShadowToast.shownToastCount(); Shadows.shadowOf(Looper.getMainLooper()).idleFor(1500,TimeUnit.MILLISECONDS); assertEquals(count,org.robolectric.shadows.ShadowToast.shownToastCount()); app.worker.shutdownNow();
    }
    @Test @Config(qualifiers="w640dp-h360dp-land") public void testFeedbackPostsHighPriorityNotificationAndDisplaysLastResult() throws Exception {
        var controller=start(); var activity=controller.get(); Shadows.shadowOf(app).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS); show(activity,SwitcherActivity.Page.SETTINGS);
        findButton(activity.getWindow().getDecorView(),"Notifications").performClick(); findButton(activity.getWindow().getDecorView(),"Test feedback").performClick(); layout(activity);
        var manager=app.getSystemService(android.app.NotificationManager.class); assertEquals(android.app.NotificationManager.IMPORTANCE_HIGH,manager.getNotificationChannel("ram_results").getImportance());
        assertTrue(app.lastMemoryMessage.contains("test")); assertEquals("RAM feedback test",manager.getActiveNotifications()[0].getNotification().extras.getCharSequence(android.app.Notification.EXTRA_TITLE)); assertFits((View)field(activity,"formMain")); assertFits((View)field(activity,"formInfo")); end(controller);
    }
    @Test public void groupedAppClosesAllItsWindowsAndKeepsOneRamSample() throws Exception {
        var controller=start(); var activity=controller.get(); backend.duplicates=true; invoke(activity,"refresh"); drain();
        List<?> tasks=(List<?>)field(activity,"tasks"); JSONObject first=(JSONObject)tasks.get(0); assertEquals(3,tasks.size()); assertEquals(2,first.getInt("windowCount")); assertEquals(7168,first.getLong("pssKb"));
        press(activity,KeyEvent.KEYCODE_BUTTON_X); drain(); assertEquals(List.of(7,10),backend.removed); assertEquals(2,app.lastOperation.getInt("confirmed")); end(controller);
    }
    @Test public void windowPickerOpensSpecificWindowAndSeparateModeRestoresAllCards() throws Exception {
        var controller=start(); var activity=controller.get(); backend.duplicates=true; invoke(activity,"refresh"); drain();
        app.getSharedPreferences("settings",0).edit().putBoolean("groupApps",false).commit(); invoke(activity,"refresh"); drain(); assertEquals(4,((List<?>)field(activity,"cards")).size());
        app.getSharedPreferences("settings",0).edit().putBoolean("groupApps",true).commit(); invoke(activity,"refresh"); drain(); press(activity,KeyEvent.KEYCODE_BUTTON_SELECT);
        PopupWindow popup=(PopupWindow)field(activity,"protectionPopup"); findButton(popup.getContentView(),"Choose window").performClick();
        assertEquals(SwitcherActivity.Page.WINDOWS,field(activity,"page")); findButton(activity.getWindow().getDecorView(),"Window 10").performClick(); drain(); assertEquals(10,backend.opened); end(controller);
    }
    @Test public void partialFailuresAndUnverifiedResultsAreRetainedByAppName() throws Exception {
        var controller=start(); var activity=controller.get(); backend.failId=8; backend.unverifiedId=7; press(activity,KeyEvent.KEYCODE_BUTTON_Y); drain();
        assertFalse(activity.isFinishing()); assertEquals(1,app.lastOperation.getInt("failed")); assertEquals(1,app.lastOperation.getInt("unverified")); assertEquals(0,app.lastOperation.getInt("confirmed"));
        assertTrue(app.lastOperation.getString("summary").contains("Game 8")); assertTrue(app.lastOperation.getString("summary").contains("Game 7"));
        assertEquals(View.VISIBLE,((View)field(activity,"operationInfo")).getVisibility()); show(activity,SwitcherActivity.Page.RESULTS); layout(activity); assertFits((View)field(activity,"formMain")); end(controller);
    }
    @Test public void slowRamDoesNotBlockOpeningAnApp() throws Exception {
        var controller=start(); var activity=controller.get(); app.separateMemory=true; backend.memoryStarted=new java.util.concurrent.CountDownLatch(1); backend.releaseMemory=new java.util.concurrent.CountDownLatch(1);
        try {
            invoke(activity,"refresh"); drain(); assertTrue(backend.memoryStarted.await(2,TimeUnit.SECONDS)); assertEquals(false,field(activity,"busy")); assertEquals(3,((List<?>)field(activity,"cards")).size());
            press(activity,KeyEvent.KEYCODE_DPAD_RIGHT); press(activity,KeyEvent.KEYCODE_BUTTON_A); drain(); assertEquals(8,backend.opened); assertTrue(activity.isFinishing());
        } finally { backend.releaseMemory.countDown(); end(controller); }
    }
    @Test public void ramTimeoutLeavesTaskControlsAvailable() throws Exception {
        var controller=start(); var activity=controller.get(); app.separateMemory=true; backend.memoryStarted=new java.util.concurrent.CountDownLatch(1); backend.releaseMemory=new java.util.concurrent.CountDownLatch(1);
        try {
            invoke(activity,"refresh"); drain(); assertTrue(backend.memoryStarted.await(2,TimeUnit.SECONDS)); Shadows.shadowOf(Looper.getMainLooper()).idleFor(5100,TimeUnit.MILLISECONDS);
            assertTrue(((String)field(activity,"memoryStatus")).contains("timed out")); assertEquals(false,field(activity,"busy")); assertEquals(0,app.recoveries);
            press(activity,KeyEvent.KEYCODE_DPAD_RIGHT); assertEquals(Integer.valueOf(8),((DeckSelection)field(activity,"selection")).id());
        } finally { backend.releaseMemory.countDown(); end(controller); }
    }
    @Test public void directShortcutRecordingRejectsNestedChordsThenSavesIndependentBinding() throws Exception {
        var controller=start(); var activity=controller.get(); app.getSharedPreferences("settings",0).edit().putString("shortcutChord","138:0,139:0").commit(); show(activity,SwitcherActivity.Page.SHORTCUTS);
        findButton(activity.getWindow().getDecorView(),"Record direct switch").performClick(); press(activity,KeyEvent.KEYCODE_F8); assertFalse(((android.widget.Button)field(activity,"saveChord")).isEnabled());
        assertTrue(((android.widget.TextView)field(activity,"recordSaved")).getText().toString().contains("conflicts"));
        findButton(activity.getWindow().getDecorView(),"Record again").performClick(); activity.dispatchKeyEvent(new KeyEvent(0,KeyEvent.KEYCODE_F10)); activity.dispatchKeyEvent(new KeyEvent(0,KeyEvent.KEYCODE_F11)); activity.dispatchKeyEvent(new KeyEvent(1,KeyEvent.KEYCODE_F10)); activity.dispatchKeyEvent(new KeyEvent(1,KeyEvent.KEYCODE_F11));
        press(activity,KeyEvent.KEYCODE_BUTTON_START); assertEquals(SwitcherActivity.Page.SHORTCUTS,field(activity,"page")); assertEquals(2,ShortcutKeys.decode(app.getSharedPreferences("settings",0).getString("quickSwitchChord","")).size()); end(controller);
    }
    @Test public void quickSwitchUsesBackendWithoutOpeningAnotherDeckActivity() throws Exception {
        var controller=start(); app.quickSwitch(); drain(); assertEquals(1,backend.directSwitches); assertNull(Shadows.shadowOf(app).getNextStartedActivity()); end(controller);
    }
    @Test @Config(qualifiers="w800dp-h360dp-land") public void settingsToggleReflectsStateAndControllerCanCrossPanes() throws Exception {
        var controller=start(); var activity=controller.get(); show(activity,SwitcherActivity.Page.SETTINGS);
        findButton(activity.getWindow().getDecorView(),"Appearance").performClick(); layout(activity);
        var toggle=findButton(activity.getWindow().getDecorView(),"Stay open after clearing · Off"); assertFalse(toggle.isSelected()); toggle.performClick();
        toggle=findButton(activity.getWindow().getDecorView(),"Stay open after clearing · On"); assertTrue(toggle.isSelected()); assertFalse(app.getSharedPreferences("settings",0).getBoolean("exitAfterBulk",true));
        var focus=SwitcherActivity.class.getDeclaredMethod("focusControl",int.class); focus.setAccessible(true); focus.invoke(activity,3); press(activity,KeyEvent.KEYCODE_DPAD_RIGHT); assertTrue((Integer)field(activity,"controlIndex")>5);
        press(activity,KeyEvent.KEYCODE_DPAD_LEFT); assertEquals(3,field(activity,"controlIndex")); layout(activity); assertFits((View)field(activity,"formMain")); assertFits((View)field(activity,"formInfo")); end(controller);
    }
    @Test @Config(qualifiers="w800dp-h360dp-land") public void fitAllScrollsRatherThanViolatingMinimumTextSize() throws Exception {
        var controller=start(); var activity=controller.get(); backend.many=true; backend.taskCount=200; app.getSharedPreferences("settings",0).edit().putInt("minLabelSp",22).commit(); changeView(activity,"fit");
        assertNotNull(field(activity,"appScroll")); List<?> cards=(List<?>)field(activity,"cards"); ViewGroup first=(ViewGroup)cards.get(0); android.widget.TextView name=(android.widget.TextView)first.getChildAt(1);
        assertTrue(name.getTextSize()>=22*activity.getResources().getDisplayMetrics().scaledDensity); end(controller);
    }
    @Test @Config(qualifiers="w640dp-h360dp-land") public void singleDeckStartsOnFirstAppWithCompactIconSettingsAndFourViewPresets() throws Exception {
        var controller=start(); var activity=controller.get(); app.getSharedPreferences("settings",0).edit().remove("view").commit(); show(activity,SwitcherActivity.Page.APPS); invoke(activity,"refresh"); drain(); layout(activity);
        assertEquals(View.VISIBLE,((View)field(activity,"sidebar")).getVisibility()); assertNull(field(activity,"appScroll")); assertEquals(false,field(activity,"toolZone")); assertEquals(Integer.valueOf(7),((DeckSelection)field(activity,"selection")).id());
        List<?> tools=(List<?>)field(activity,"tools"); for(Object tool:tools){ String label=((android.widget.Button)tool).getText().toString(); assertFalse(Set.of("Switch apps","Manage tasks","Setup","Connect","Diagnostics","Custom").contains(label)); }
        android.widget.Button settings=(android.widget.Button)tools.get(3); assertEquals("",settings.getText().toString()); assertEquals("Settings",settings.getContentDescription());
        ViewGroup footer=(ViewGroup)findButton(activity.getWindow().getDecorView(),"Open").getParent(); assertEquals(5,footer.getChildCount()); var others=findButton(activity.getWindow().getDecorView(),"Close all but current"); assertNull(others.getCompoundDrawables()[2]); assertTrue(others.getTextSize()<findButton(activity.getWindow().getDecorView(),"Open").getTextSize()); assertFits((View)findButton(activity.getWindow().getDecorView(),"Refresh").getParent());
        press(activity,KeyEvent.KEYCODE_DPAD_RIGHT); activity.onNewIntent(new android.content.Intent(activity,SwitcherActivity.class)); assertEquals(Integer.valueOf(7),((DeckSelection)field(activity,"selection")).id()); drain(); end(controller);
    }
    @Test public void viewPopupContainsOnlyFourPresetsAndHasAnOutline() throws Exception {
        var controller=start(); var activity=controller.get(); invoke(activity,"viewMenu"); PopupWindow popup=(PopupWindow)field(activity,"protectionPopup"); ViewGroup panel=(ViewGroup)popup.getContentView(); int count=0;
        for(int i=0;i<panel.getChildCount();i++) if(panel.getChildAt(i) instanceof android.widget.Button){ count++; String label=((android.widget.Button)panel.getChildAt(i)).getText().toString(); assertTrue(Set.of("Fit all","Grid","List","Cards").contains(label)); }
        assertEquals(4,count); assertTrue(panel.getBackground() instanceof android.graphics.drawable.GradientDrawable); popup.dismiss();
        invoke(activity,"sortMenu"); popup=(PopupWindow)field(activity,"protectionPopup"); assertTrue(popup.getContentView().getBackground() instanceof android.graphics.drawable.GradientDrawable); popup.dismiss(); end(controller);
    }
    @Test @Config(qualifiers="w800dp-h360dp-land") public void allSettingsCategoriesFitAndBackNeverReopensSetup() throws Exception {
        var controller=start(); var activity=controller.get(); show(activity,SwitcherActivity.Page.SETTINGS);
        for(String category:List.of("Connection","Controller","Appearance","Dock","Notifications","About")){ findButton(activity.getWindow().getDecorView(),category).performClick(); layout(activity); assertFits((View)field(activity,"formMain")); assertFits((View)field(activity,"formInfo")); }
        press(activity,KeyEvent.KEYCODE_BUTTON_B); assertEquals(SwitcherActivity.Page.APPS,field(activity,"page")); end(controller);
    }
    @Test @Config(qualifiers="w800dp-h360dp-land") public void ramChartsExposeMoreThanThreeEntriesAndPageThroughAllApps() throws Exception {
        var controller=start(); var activity=controller.get(); backend.many=true; backend.taskCount=20; invoke(activity,"refresh"); drain(); show(activity,SwitcherActivity.Page.MEMORY); layout(activity);
        ViewGroup pane=(ViewGroup)field(activity,"formInfo"); int bars=0; for(int i=0;i<pane.getChildCount();i++) if(pane.getChildAt(i) instanceof android.widget.ProgressBar) bars++; assertTrue(bars>3); assertFits(pane);
        findButton(activity.getWindow().getDecorView(),"Next page").performClick(); layout(activity); assertEquals(1,field(activity,"ramPage")); assertFits((View)field(activity,"formInfo")); end(controller);
    }
    @Test public void finishingEightStepSetupShowsCheckThenNeverRepeatsOnLaunch() throws Exception {
        var controller=start(); var activity=controller.get(); app.getSharedPreferences("settings",0).edit().putBoolean("setupComplete",false).commit(); activity.onNewIntent(new android.content.Intent(activity,SwitcherActivity.class));
        assertEquals(SwitcherActivity.Page.SETUP,field(activity,"page")); for(int i=0;i<8;i++) press(activity,KeyEvent.KEYCODE_BUTTON_R1);
        assertEquals(SwitcherActivity.Page.COMPLETE,field(activity,"page")); assertTrue(app.getSharedPreferences("settings",0).getBoolean("setupComplete",false));
        assertTrue(((ViewGroup)field(activity,"root")).getChildAt(0) instanceof SetupCheckView); Shadows.shadowOf(Looper.getMainLooper()).idleFor(2000,TimeUnit.MILLISECONDS); assertEquals(SwitcherActivity.Page.APPS,field(activity,"page"));
        controller.pause().stop().destroy(); controller=Robolectric.buildActivity(SwitcherActivity.class).setup().visible(); assertEquals(SwitcherActivity.Page.APPS,field(controller.get(),"page")); end(controller);
    }
    @Test @Config(qualifiers="w800dp-h360dp-land") public void settingsCategoriesNavigateWithoutExtraConfirmationAndThemeRowSupportsLeftRight() throws Exception {
        var controller=start(); var activity=controller.get(); show(activity,SwitcherActivity.Page.SETTINGS); press(activity,KeyEvent.KEYCODE_DPAD_DOWN);
        assertEquals(1,field(activity,"settingsCategory")); assertNotNull(findButton(activity.getWindow().getDecorView(),"Button mapping"));
        press(activity,KeyEvent.KEYCODE_DPAD_DOWN); assertEquals(2,field(activity,"settingsCategory")); press(activity,KeyEvent.KEYCODE_DPAD_RIGHT); assertEquals(7,field(activity,"controlIndex"));
        press(activity,KeyEvent.KEYCODE_DPAD_RIGHT); assertEquals(8,field(activity,"controlIndex")); press(activity,KeyEvent.KEYCODE_BUTTON_A); Shadows.shadowOf(Looper.getMainLooper()).idle(); assertEquals("dark",app.getSharedPreferences("settings",0).getString("theme",""));
        press(activity,KeyEvent.KEYCODE_DPAD_LEFT); assertEquals(7,field(activity,"controlIndex")); press(activity,KeyEvent.KEYCODE_DPAD_LEFT); assertEquals(3,field(activity,"controlIndex")); end(controller);
    }
    @Test public void calibrationKeepsGamepadDpadAndAndroidBackAvailable() throws Exception {
        var controller=start(); var activity=controller.get(); show(activity,SwitcherActivity.Page.CONFIRM);
        KeyEvent down=new KeyEvent(0,0,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_DOWN,0,0,7,0,0,InputDevice.SOURCE_GAMEPAD); activity.dispatchKeyEvent(down);
        assertEquals(0,field(activity,"controlIndex")); assertFalse(app.getSharedPreferences("settings",0).getBoolean("confirmCalibrated",false));
        press(activity,KeyEvent.KEYCODE_DPAD_CENTER); assertEquals(SwitcherActivity.Page.SETTINGS,field(activity,"page")); end(controller);
    }
    @Test public void firstSetupRequiresConnectionBeforeOptionalControllerSteps() throws Exception {
        var controller=start(); var activity=controller.get(); app.backend=null; show(activity,SwitcherActivity.Page.SETUP); assertFalse(((android.widget.Button)field(activity,"setupNext")).isEnabled());
        press(activity,KeyEvent.KEYCODE_BUTTON_R1); assertEquals(0,field(activity,"setupStep")); app.backend=backend; invoke(activity,"connectionChanged"); assertTrue(((android.widget.Button)field(activity,"setupNext")).isEnabled());
        press(activity,KeyEvent.KEYCODE_BUTTON_R1); assertEquals(1,field(activity,"setupStep")); end(controller);
    }
    @Test @Config(qualifiers="w800dp-h360dp-land") public void appChartsUseTotalPhysicalRamAndNeverShowAvailableRam() throws Exception {
        var controller=start(); var activity=controller.get(); var info=new android.app.ActivityManager.MemoryInfo(); info.totalMem=8L*1024*1024*1024; info.availMem=4L*1024*1024*1024;
        Shadows.shadowOf(app.getSystemService(android.app.ActivityManager.class)).setMemoryInfo(info); invoke(activity,"updateMemory"); layout(activity);
        ViewGroup bars=(ViewGroup)field(activity,"memoryBars"); List<android.widget.ProgressBar> charts=new ArrayList<>(); for(int i=0;i<bars.getChildCount();i++) if(bars.getChildAt(i) instanceof android.widget.ProgressBar) charts.add((android.widget.ProgressBar)bars.getChildAt(i));
        assertEquals(500,charts.get(0).getProgress()); assertTrue(charts.size()>1); assertEquals(6,charts.get(1).getProgress()); assertFalse(allText(activity.getWindow().getDecorView()).toLowerCase().matches("(?s).*\\bavailable\\b.*")); assertFalse(allText(activity.getWindow().getDecorView()).toLowerCase().contains("free"));
        show(activity,SwitcherActivity.Page.MEMORY); layout(activity); assertFalse(allText(activity.getWindow().getDecorView()).contains("Available")); end(controller);
    }
    @Test @Config(qualifiers="w800dp-h360dp-land") public void mappedConfirmUpdatesFooterAndActuallyOpensAnApp() throws Exception {
        var controller=start(); var activity=controller.get(); ControllerBindings.assign(app.getSharedPreferences("settings",0),"open",105); show(activity,SwitcherActivity.Page.SETTINGS); press(activity,KeyEvent.KEYCODE_DPAD_DOWN); press(activity,KeyEvent.KEYCODE_DPAD_RIGHT); press(activity,KeyEvent.KEYCODE_BUTTON_R2); assertEquals(SwitcherActivity.Page.MAPPING,field(activity,"page")); show(activity,SwitcherActivity.Page.APPS); invoke(activity,"refresh"); drain();
        assertEquals("R2: Open app",findButton(activity.getWindow().getDecorView(),"Open").getContentDescription()); press(activity,KeyEvent.KEYCODE_BUTTON_R2); drain(); assertEquals(7,backend.opened); end(controller);
    }
    @Test public void controllerMappingProfileAndCaptureFollowPadboardConvention() throws Exception {
        var controller=start(); var activity=controller.get(); show(activity,SwitcherActivity.Page.MAPPING); findButton(activity.getWindow().getDecorView(),"Labels: A/B/X/Y").performClick();
        var dialog=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog(); dialog.getListView().performItemClick(null,3,3);
        assertEquals("playstation_swapped",ControllerBindings.profile(app.getSharedPreferences("settings",0))); assertEquals("Open app: Cross",findButton(activity.getWindow().getDecorView(),"Open app").getContentDescription()); assertTrue(findButton(activity.getWindow().getDecorView(),"Open app").getCompoundDrawables()[0] instanceof ControllerBadge);
        findButton(activity.getWindow().getDecorView(),"Close app").performClick(); assertEquals(SwitcherActivity.Page.BIND,field(activity,"page")); assertTrue(TaskDeckApp.recordingShortcut); press(activity,KeyEvent.KEYCODE_DPAD_DOWN); assertEquals(SwitcherActivity.Page.BIND,field(activity,"page")); press(activity,KeyEvent.KEYCODE_BUTTON_R2); assertEquals(105,ControllerBindings.code(app.getSharedPreferences("settings",0),"close")); assertEquals(SwitcherActivity.Page.MAPPING,field(activity,"page")); end(controller);
    }
    @Test public void connectedSettingHasCheckmarkAndAboutLinksToSilasdvKofi() throws Exception {
        var controller=start(); var activity=controller.get(); show(activity,SwitcherActivity.Page.SETTINGS); var connection=findButton(activity.getWindow().getDecorView(),"Connection"); assertEquals("Connection: connected",connection.getContentDescription()); assertEquals("check",((ActionGlyph)connection.getCompoundDrawables()[0]).kind);
        findButton(activity.getWindow().getDecorView(),"Notifications").performClick(); assertEquals("notify",ActionGlyph.kind("Test feedback"));
        findButton(activity.getWindow().getDecorView(),"About").performClick(); assertTrue(allText(activity.getWindow().getDecorView()).contains("1.1")); assertTrue(allText(activity.getWindow().getDecorView()).contains("SilasDV")); findButton(activity.getWindow().getDecorView(),"Support SilasDV on Ko-fi").performClick(); assertEquals("https://ko-fi.com/silasdv",Shadows.shadowOf(activity).getNextStartedActivity().getDataString()); end(controller);
    }
    private String allText(View view){ StringBuilder text=new StringBuilder(); if(view instanceof android.widget.TextView) text.append(((android.widget.TextView)view).getText()); if(view instanceof ViewGroup) for(int i=0;i<((ViewGroup)view).getChildCount();i++) text.append(" ").append(allText(((ViewGroup)view).getChildAt(i))); return text.toString(); }
    @Test @Config(qualifiers="w800dp-h360dp-land") public void currentMarkerFollowsEntryAppNotSelectionAndUpdatesOnNewOpening() throws Exception {
        var controller=start(); var activity=controller.get(); List<?> cards=(List<?>)field(activity,"cards"); assertTrue(((View)cards.get(0)).getContentDescription().toString().contains("Current app"));
        press(activity,KeyEvent.KEYCODE_DPAD_RIGHT); assertTrue(((View)cards.get(0)).getContentDescription().toString().contains("Current app")); backend.entryId=8; activity.onNewIntent(new android.content.Intent(activity,SwitcherActivity.class)); drain(); cards=(List<?>)field(activity,"cards"); assertFalse(((View)cards.get(0)).getContentDescription().toString().contains("Current app")); assertTrue(((View)cards.get(1)).getContentDescription().toString().contains("Current app")); end(controller);
    }
    @Test @Config(qualifiers="w800dp-h360dp-land") public void rightFromAppEdgeReachesRamDetailsWithDpadAndStick() throws Exception {
        var controller=start(); var activity=controller.get(); for(int i=0;i<3;i++) press(activity,KeyEvent.KEYCODE_DPAD_RIGHT); assertTrue((Boolean)field(activity,"toolZone")); assertTrue(((View)field(activity,"ramButton")).isActivated()); press(activity,KeyEvent.KEYCODE_BUTTON_A); assertEquals(SwitcherActivity.Page.MEMORY,field(activity,"page")); press(activity,KeyEvent.KEYCODE_BUTTON_B); drain();
        for(int i=0;i<3;i++){ stick(activity,1,0); stick(activity,0,0); } press(activity,KeyEvent.KEYCODE_BUTTON_A); assertEquals(SwitcherActivity.Page.MEMORY,field(activity,"page")); end(controller);
    }
    @Test public void savedChoicesDoNotPretendPermissionsOrCalibrationAreGranted() throws Exception {
        var controller=start(); var activity=controller.get(); app.getSharedPreferences("settings",0).edit().putBoolean("swapped",false).putString("shortcutChord","138:0").putBoolean("shortcutEnabled",true).commit();
        setField(activity,"setupStep",1); show(activity,SwitcherActivity.Page.SETUP); assertFalse(allText(activity.getWindow().getDecorView()).contains("✓ Confirm")); setField(activity,"setupStep",2); show(activity,SwitcherActivity.Page.SETUP); assertTrue(allText(activity.getWindow().getDecorView()).contains("service not enabled")); assertFalse(allText(activity.getWindow().getDecorView()).contains("✓"));
        setField(activity,"setupStep",7); Shadows.shadowOf(app).denyPermissions(android.Manifest.permission.POST_NOTIFICATIONS); show(activity,SwitcherActivity.Page.SETUP); assertTrue(allText(activity.getWindow().getDecorView()).contains("Notifications: not allowed"));
        Shadows.shadowOf(app).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS); activity.onRequestPermissionsResult(1,new String[]{android.Manifest.permission.POST_NOTIFICATIONS},new int[]{android.content.pm.PackageManager.PERMISSION_GRANTED}); assertTrue(allText(activity.getWindow().getDecorView()).contains("Notifications: allowed"));
        Shadows.shadowOf(app).denyPermissions(android.Manifest.permission.POST_NOTIFICATIONS); controller.pause().resume(); assertTrue(allText(activity.getWindow().getDecorView()).contains("Notifications: not allowed")); end(controller);
    }
    @Test public void setupCompletionHoldsLongEnoughAndConfirmContinuesWithoutOpeningAnApp() throws Exception {
        var controller=start(); var activity=controller.get(); show(activity,SwitcherActivity.Page.COMPLETE); Shadows.shadowOf(Looper.getMainLooper()).idleFor(700,TimeUnit.MILLISECONDS); assertEquals(SwitcherActivity.Page.COMPLETE,field(activity,"page")); press(activity,KeyEvent.KEYCODE_BUTTON_A); assertEquals(SwitcherActivity.Page.APPS,field(activity,"page")); assertEquals(-1,backend.opened); end(controller);
    }
    @Test @Config(qualifiers="w800dp-h360dp-land") public void cardPinningDoesNotProtectOrCreateSyntheticTasksAndDockOpensExistingTask() throws Exception {
        var controller=start(); var activity=controller.get(); installLauncher("test.game7","Game 7"); press(activity,KeyEvent.KEYCODE_BUTTON_SELECT); PopupWindow popup=(PopupWindow)field(activity,"protectionPopup"); findButton(popup.getContentView(),"Pin to Quick Access").performClick();
        assertEquals(List.of("test.game7"),QuickAccess.read(app.getSharedPreferences("settings",0))); assertFalse(app.getSharedPreferences("settings",0).getStringSet("protectedApps",Set.of()).contains("test.game7")); assertEquals(3,((List<?>)field(activity,"cards")).size());
        press(activity,KeyEvent.KEYCODE_DPAD_DOWN); assertTrue((Boolean)field(activity,"dockZone")); press(activity,KeyEvent.KEYCODE_BUTTON_A); drain(); assertEquals(7,backend.opened); end(controller);
    }
    @Test @Config(qualifiers="w800dp-h360dp-land") public void dockCanLaunchClosedFavoriteWithoutMakingItAnOpenTaskCard() throws Exception {
        var controller=start(); var activity=controller.get(); installLauncher("closed.favorite","Closed favorite"); QuickAccess.save(app.getSharedPreferences("settings",0),List.of("closed.favorite")); show(activity,SwitcherActivity.Page.APPS); invoke(activity,"refresh"); drain(); assertEquals(3,((List<?>)field(activity,"cards")).size());
        press(activity,KeyEvent.KEYCODE_DPAD_DOWN); press(activity,KeyEvent.KEYCODE_BUTTON_A); assertEquals(-1,backend.opened); assertEquals("closed.favorite",Shadows.shadowOf(activity).getNextStartedActivity().getComponent().getPackageName()); end(controller);
    }
    @Test @Config(qualifiers="w800dp-h360dp-land") public void dockControllerReordersAndRemovesFavoritesPersistently() throws Exception {
        var controller=start(); var activity=controller.get(); QuickAccess.save(app.getSharedPreferences("settings",0),List.of("test.game7","test.game8","wave.launcher")); show(activity,SwitcherActivity.Page.APPS); press(activity,KeyEvent.KEYCODE_DPAD_DOWN); press(activity,KeyEvent.KEYCODE_BUTTON_SELECT);
        var dialog=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog(); dialog.getListView().performItemClick(null,2,2); assertEquals(List.of("test.game8","test.game7","wave.launcher"),QuickAccess.read(app.getSharedPreferences("settings",0))); press(activity,KeyEvent.KEYCODE_BUTTON_SELECT); dialog=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog(); dialog.getListView().performItemClick(null,3,3); assertEquals(List.of("test.game8","wave.launcher"),QuickAccess.read(app.getSharedPreferences("settings",0))); show(activity,SwitcherActivity.Page.APPS); assertEquals("test.game8",((QuickAccessDock)field(activity,"dock")).items.get(0).getTag()); end(controller);
    }
    @Test @Config(qualifiers="w800dp-h360dp-land") public void dockSizeBehaviorAndThumbCornersFitAndAreControllerReachable() throws Exception {
        var controller=start(); var activity=controller.get(); QuickAccess.save(app.getSharedPreferences("settings",0),List.of("test.game7","test.game8","wave.launcher","closed.favorite")); show(activity,SwitcherActivity.Page.SETTINGS); findButton(activity.getWindow().getDecorView(),"Dock").performClick(); findButton(activity.getWindow().getDecorView(),"Dock style & size").performClick();
        findButton(activity.getWindow().getDecorView(),"Size +").performClick(); assertEquals(110,QuickAccess.size(app.getSharedPreferences("settings",0))); findButton(activity.getWindow().getDecorView(),"Style: Floating").performClick(); var dialog=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog(); dialog.getListView().performItemClick(null,3,3); assertEquals("thumb",app.getSharedPreferences("settings",0).getString("dockStyle","")); layout(activity); assertFits((View)field(activity,"formMain")); assertFits((View)field(activity,"formInfo"));
        app.getSharedPreferences("settings",0).edit().putString("dockBehavior","focus").commit(); show(activity,SwitcherActivity.Page.APPS); layout(activity); QuickAccessDock dock=(QuickAccessDock)field(activity,"dock"); assertFalse(dock.isExpanded()); press(activity,KeyEvent.KEYCODE_DPAD_DOWN); layout(activity); assertTrue(dock.isExpanded()); assertEquals(4,dock.items.size()); int[] first=new int[2],last=new int[2]; dock.items.get(0).getLocationOnScreen(first); dock.items.get(3).getLocationOnScreen(last); assertTrue(first[0]<activity.getResources().getDisplayMetrics().widthPixels/3); assertTrue(last[0]>activity.getResources().getDisplayMetrics().widthPixels*2/3); assertTrue(dock.items.get(0).getWidth()>=56); press(activity,KeyEvent.KEYCODE_DPAD_UP); assertFalse(dock.isExpanded()); end(controller);
    }
    @Test @Config(qualifiers="w800dp-h360dp-land") public void dockTouchLaunchWorksOnFirstTapAfterControllerUse() throws Exception {
        var controller=start(); var activity=controller.get(); QuickAccess.save(app.getSharedPreferences("settings",0),List.of("test.game7")); show(activity,SwitcherActivity.Page.APPS); invoke(activity,"refresh"); drain(); layout(activity); View item=((QuickAccessDock)field(activity,"dock")).items.get(0); int[] xy=new int[2]; item.getLocationOnScreen(xy); long now=android.os.SystemClock.uptimeMillis();
        MotionEvent down=MotionEvent.obtain(now,now,MotionEvent.ACTION_DOWN,xy[0]+item.getWidth()/2f,xy[1]+item.getHeight()/2f,0),up=MotionEvent.obtain(now,now+50,MotionEvent.ACTION_UP,xy[0]+item.getWidth()/2f,xy[1]+item.getHeight()/2f,0); activity.dispatchTouchEvent(down); activity.dispatchTouchEvent(up); down.recycle(); up.recycle(); drain(); assertEquals(7,backend.opened); assertEquals(false,field(activity,"controllerMode")); end(controller);
    }
    @Test @Config(qualifiers="w640dp-h360dp-land") public void settingsCircleHintsAndAllDockPagesFitCompactLandscape() throws Exception {
        var controller=start(); var activity=controller.get(); layout(activity); List<?> tools=(List<?>)field(activity,"tools"); android.widget.Button settings=(android.widget.Button)tools.get(3); assertEquals(settings.getWidth(),settings.getHeight()); android.graphics.drawable.StateListDrawable states=(android.graphics.drawable.StateListDrawable)settings.getBackground(); assertEquals(android.graphics.drawable.GradientDrawable.OVAL,((android.graphics.drawable.GradientDrawable)states.getStateDrawable(0)).getShape());
        var open=findButton(activity.getWindow().getDecorView(),"Open"); assertTrue(open.getTextSize()<12*activity.getResources().getDisplayMetrics().scaledDensity); assertTrue(open.getCompoundDrawables()[0].getBounds().height()<22); for(var page:List.of(SwitcherActivity.Page.DOCKSTYLE,SwitcherActivity.Page.DOCKBEHAVIOR,SwitcherActivity.Page.PICKER)){ show(activity,page); layout(activity); assertFits((View)field(activity,"formMain")); assertFits((View)field(activity,"formInfo")); } end(controller);
    }
    @Test @Config(qualifiers="w800dp-h360dp-land") public void installedAppPickerAddsFavoriteAndReturnsToItsOrigin() throws Exception {
        var controller=start(); var activity=controller.get(); installLauncher("closed.favorite","Closed favorite"); invoke(activity,"showPicker"); drain(); assertEquals(SwitcherActivity.Page.PICKER,field(activity,"page")); findButton(activity.getWindow().getDecorView(),"Add Closed favorite").performClick(); assertEquals(List.of("closed.favorite"),QuickAccess.read(app.getSharedPreferences("settings",0))); press(activity,KeyEvent.KEYCODE_BUTTON_B); assertEquals(SwitcherActivity.Page.APPS,field(activity,"page")); assertEquals(3,((List<?>)field(activity,"cards")).size()); end(controller);
    }
    private void setField(Object object,String name,Object value) throws Exception { var field=object.getClass().getDeclaredField(name); field.setAccessible(true); field.set(object,value); }
    private void stick(SwitcherActivity activity,float x,float y){ MotionEvent.PointerProperties p=new MotionEvent.PointerProperties(); p.id=0; MotionEvent.PointerCoords c=new MotionEvent.PointerCoords(); c.setAxisValue(MotionEvent.AXIS_X,x); c.setAxisValue(MotionEvent.AXIS_Y,y); MotionEvent event=MotionEvent.obtain(0,1,MotionEvent.ACTION_MOVE,1,new MotionEvent.PointerProperties[]{p},new MotionEvent.PointerCoords[]{c},0,0,1,1,7,0,InputDevice.SOURCE_JOYSTICK,0); activity.onGenericMotionEvent(event); event.recycle(); }
    private void installLauncher(String pkg,String label){
        var application=new android.content.pm.ApplicationInfo(); application.packageName=pkg; application.nonLocalizedLabel=label; application.enabled=true;
        var activity=new android.content.pm.ActivityInfo(); activity.packageName=pkg; activity.name=pkg+".Main"; activity.applicationInfo=application; activity.enabled=true; activity.exported=true;
        var info=new android.content.pm.PackageInfo(); info.packageName=pkg; info.applicationInfo=application; info.activities=new android.content.pm.ActivityInfo[]{activity}; var pm=Shadows.shadowOf(app.getPackageManager()); pm.installPackage(info);
        var resolve=new android.content.pm.ResolveInfo(); resolve.activityInfo=activity; resolve.isDefault=true; pm.addResolveInfoForIntent(new android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_LAUNCHER).setPackage(pkg),resolve); pm.addResolveInfoForIntent(new android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_LAUNCHER),resolve);
    }
    private void layout(SwitcherActivity activity){ View decor=activity.getWindow().getDecorView(); int w=activity.getResources().getDisplayMetrics().widthPixels,h=activity.getResources().getDisplayMetrics().heightPixels; for(int i=0;i<3;i++){ decor.measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(h,View.MeasureSpec.EXACTLY)); decor.layout(0,0,w,h); Shadows.shadowOf(Looper.getMainLooper()).idle(); } }
    private String describe(View parent){ StringBuilder info=new StringBuilder(); if(parent instanceof ViewGroup) for(int i=0;i<((ViewGroup)parent).getChildCount();i++){ View c=((ViewGroup)parent).getChildAt(i); info.append(c instanceof android.widget.TextView?((android.widget.TextView)c).getText():c.getClass().getSimpleName()).append(":").append(c.getTop()).append("-").append(c.getBottom()).append("; "); } return info.toString(); }
    private void assertFits(View parent){ if(parent instanceof ViewGroup) for(int i=0;i<((ViewGroup)parent).getChildCount();i++){ View child=((ViewGroup)parent).getChildAt(i); assertTrue("Child exceeds bottom ["+describe(parent)+", height="+parent.getHeight()+", top="+child.getTop()+", bottom="+child.getBottom()+"]: "+(child instanceof android.widget.TextView?((android.widget.TextView)child).getText():child.getClass()),child.getBottom()<=parent.getHeight()-parent.getPaddingBottom()); assertTrue("Child exceeds width",child.getRight()<=parent.getWidth()-parent.getPaddingRight()); } }
}
