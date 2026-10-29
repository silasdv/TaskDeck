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
        @Override public void onCreate(){}
        @Override public void connect(boolean request){}
    }
    static class FakeBackend extends ITaskBackend.Stub {
        final List<Integer> removed=new ArrayList<>(); final Set<String> protectedApps=new HashSet<>(); int opened=-1,reads; boolean extra;
        public synchronized void setProtectedPackages(String[] names){ protectedApps.clear(); protectedApps.addAll(Arrays.asList(names)); }
        public synchronized String readTasks(){
            reads++;
            try {
                JSONArray rows=new JSONArray();
                for(int id:extra?new int[]{7,8,10,11,9}:new int[]{7,8,9}) if(!removed.contains(id)){
                    String name=id==9?"wave.launcher":"test.game"+id;
                    rows.put(new JSONObject().put("id",id).put("package",name).put("label",id==9?"Wave Launcher":"Game "+id)
                        .put("launcher",id==9).put("mandatoryProtected",id==9).put("userProtected",protectedApps.contains(name)).put("removable",id!=9&&!protectedApps.contains(name)));
                }
                return new JSONObject().put("uid",2000).put("sdk",33).put("tasks",rows).put("backgroundApps",new JSONArray().put(new JSONObject().put("label","Test Keyboard").put("pssKb",51200).put("reason","Protected service"))).put("previousTask",new JSONObject().put("id",7).put("package","test.game7")).toString();
            } catch(JSONException e){ throw new RuntimeException(e); }
        }
        public synchronized String openTask(int id,String name){ opened=id; return "{}"; }
        public synchronized String removeTask(int id,String name){ if(id==9||protectedApps.contains(name)) return "{\"error\":\"protected\"}"; removed.add(id); return "{}"; }
        public void destroy(){}
    }
    private TestApp app; private FakeBackend backend;
    private org.robolectric.android.controller.ActivityController<SwitcherActivity> start() throws Exception {
        app=(TestApp)RuntimeEnvironment.getApplication(); backend=new FakeBackend(); app.backend=backend;
        app.getSharedPreferences("settings",0).edit().putBoolean("setupComplete",true).commit();
        var controller=Robolectric.buildActivity(SwitcherActivity.class).setup().visible(); controller.windowFocusChanged(true); drain(); return controller;
    }
    private void drain() throws Exception { for(int i=0;i<3;i++){ app.worker.submit(()->{}).get(5,TimeUnit.SECONDS); Shadows.shadowOf(Looper.getMainLooper()).idle(); } }
    private void press(SwitcherActivity activity,int key){ activity.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,key)); activity.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,key)); }
    private Object field(SwitcherActivity activity,String name) throws Exception { var f=SwitcherActivity.class.getDeclaredField(name); f.setAccessible(true); return f.get(activity); }
    private void end(org.robolectric.android.controller.ActivityController<SwitcherActivity> controller){ controller.pause().stop().destroy(); app.worker.shutdownNow(); }

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
        assertEquals(List.of(8),backend.removed); assertEquals(7,backend.opened); assertTrue(activity.isFinishing()); end(controller);
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
        press(activity,KeyEvent.KEYCODE_BUTTON_A); assertEquals(SwitcherActivity.Page.SETUP,field(activity,"page"));
        assertEquals(Set.of("138:0"),ShortcutKeys.decode(app.getSharedPreferences("settings",0).getString("shortcutChord",""))); end(controller);
    }
    @Test public void touchActivatesThemeOnFirstTapAndControllerCanResume() throws Exception {
        var controller=start(); var activity=controller.get(); show(activity,SwitcherActivity.Page.SETUP);
        findButton(activity.getWindow().getDecorView(),"Next step").performClick(); findButton(activity.getWindow().getDecorView(),"Next step").performClick();
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
        var controller=start(); var activity=controller.get(); Shadows.shadowOf(app).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS); show(activity,SwitcherActivity.Page.SETUP);
        for(int i=0;i<3;i++) findButton(activity.getWindow().getDecorView(),"Next step").performClick();
        findButton(activity.getWindow().getDecorView(),"Show notification shortcut").performClick();
        assertNotNull(findButton(activity.getWindow().getDecorView(),"Hide notification shortcut"));
        findButton(activity.getWindow().getDecorView(),"Hide notification shortcut").performClick();
        assertNotNull(findButton(activity.getWindow().getDecorView(),"Show notification shortcut"));
        assertEquals(0,activity.getSystemService(android.app.NotificationManager.class).getActiveNotifications().length); end(controller);
    }
    @Test public void sortingPopupWorksWithDpadAndConfirmWithoutTouch() throws Exception {
        var controller=start(); var activity=controller.get(); invoke(activity,"sortMenu"); PopupWindow popup=(PopupWindow)field(activity,"protectionPopup");
        popup.getContentView().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_DOWN));
        popup.getContentView().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BUTTON_A));
        assertEquals("ram",app.getSharedPreferences("settings",0).getString("sort","recent")); assertFalse(popup.isShowing()); end(controller);
    }
    @Test public void emptyShortcutOpeningFocusesRefreshAndCanNavigateWithoutAnUpPress() throws Exception {
        var controller=start(); var activity=controller.get(); backend.removed.addAll(List.of(7,8));
        MotionEvent touch=MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN,0,0,0); activity.dispatchTouchEvent(touch); touch.recycle(); assertEquals(false,field(activity,"controllerMode"));
        activity.onNewIntent(new android.content.Intent(activity,SwitcherActivity.class).putExtra("controllerEntry",true)); drain(); assertEquals(true,field(activity,"controllerMode"));
        assertEquals(true,field(activity,"toolZone")); assertEquals(0,field(activity,"toolIndex"));
        List<?> tools=(List<?>)field(activity,"tools"); assertTrue(((View)tools.get(0)).isActivated());
        int reads=backend.reads; press(activity,KeyEvent.KEYCODE_BUTTON_A); drain(); assertTrue(backend.reads>reads);
        press(activity,KeyEvent.KEYCODE_DPAD_RIGHT); assertEquals(1,field(activity,"toolIndex")); press(activity,KeyEvent.KEYCODE_BUTTON_A);
        PopupWindow popup=(PopupWindow)field(activity,"protectionPopup"); assertTrue(popup.isShowing()); popup.dismiss();
        press(activity,KeyEvent.KEYCODE_DPAD_DOWN); assertEquals(false,field(activity,"toolZone")); assertEquals(Integer.valueOf(9),((DeckSelection)field(activity,"selection")).id());
        press(activity,KeyEvent.KEYCODE_DPAD_UP); assertEquals(true,field(activity,"toolZone")); end(controller);
    }
    @Test public void trulyEmptyDeckNeverLosesToolbarSelection() throws Exception {
        var controller=start(); var activity=controller.get(); backend.removed.addAll(List.of(7,8,9)); activity.onNewIntent(new android.content.Intent(activity,SwitcherActivity.class)); drain();
        press(activity,KeyEvent.KEYCODE_DPAD_DOWN); assertEquals(true,field(activity,"toolZone")); press(activity,KeyEvent.KEYCODE_DPAD_RIGHT); assertEquals(1,field(activity,"toolIndex"));
        press(activity,KeyEvent.KEYCODE_DPAD_LEFT); int reads=backend.reads; press(activity,KeyEvent.KEYCODE_BUTTON_A); drain(); assertTrue(backend.reads>reads); end(controller);
    }
    @Test public void sidebarExplainsBackgroundMemoryWithoutOfferingToDismissAService() throws Exception {
        var controller=start(); var activity=controller.get(); String value=((android.widget.TextView)field(activity,"backgroundInfo")).getText().toString();
        assertTrue(value.contains("Can't close here")); assertTrue(value.contains("Test Keyboard · ~50 MiB")); assertTrue(value.contains("cache also use RAM"));
        assertEquals(3,((List<?>)field(activity,"cards")).size()); end(controller);
    }
    @Test public void protectedHomeCardOpensViaHomeIntentInsteadOfRecentsApi() throws Exception {
        var controller=start(); var activity=controller.get(); backend.removed.addAll(List.of(7,8)); invoke(activity,"refresh"); drain();
        @SuppressWarnings("unchecked") List<JSONObject> tasks=(List<JSONObject>)field(activity,"tasks"); tasks.get(0).put("home",true);
        press(activity,KeyEvent.KEYCODE_DPAD_DOWN); press(activity,KeyEvent.KEYCODE_BUTTON_A);
        assertEquals(-1,backend.opened); assertTrue(activity.isFinishing()); assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().hasCategory(android.content.Intent.CATEGORY_HOME)); end(controller);
    }
    @Test @Config(qualifiers="w800dp-h360dp-land") public void everyLandscapeSetupScreenFitsWithoutScrolling() throws Exception {
        var controller=start(); var activity=controller.get(); show(activity,SwitcherActivity.Page.SETUP);
        for(int step=0;step<4;step++){
            assertNull(field(activity,"pageScroll")); layout(activity); assertFits((View)field(activity,"formMain")); assertFits((View)field(activity,"formInfo"));
            if(step<3) findButton(activity.getWindow().getDecorView(),"Next step").performClick();
        }
        for(var page:List.of(SwitcherActivity.Page.CONNECT,SwitcherActivity.Page.CHORD,SwitcherActivity.Page.CONFIRM)){
            show(activity,page); layout(activity); assertNull(field(activity,"pageScroll")); assertFits((View)field(activity,"formMain")); assertFits((View)field(activity,"formInfo"));
        }
        end(controller);
    }
    @Test @Config(qualifiers="w640dp-h360dp-land") public void compactLandscapeSetupAlsoFitsWithoutScrolling() throws Exception { everyLandscapeSetupScreenFitsWithoutScrolling(); }
    private void layout(SwitcherActivity activity){ View decor=activity.getWindow().getDecorView(); int w=activity.getResources().getDisplayMetrics().widthPixels,h=activity.getResources().getDisplayMetrics().heightPixels; decor.measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(h,View.MeasureSpec.EXACTLY)); decor.layout(0,0,w,h); }
    private void assertFits(View parent){ if(parent instanceof ViewGroup) for(int i=0;i<((ViewGroup)parent).getChildCount();i++){ View child=((ViewGroup)parent).getChildAt(i); assertTrue("Child exceeds bottom: "+(child instanceof android.widget.TextView?((android.widget.TextView)child).getText():child.getClass()),child.getBottom()<=parent.getHeight()-parent.getPaddingBottom()); assertTrue("Child exceeds width",child.getRight()<=parent.getWidth()-parent.getPaddingRight()); } }
}
