package com.applanga.applangaflutter;

import android.app.Activity;
import android.util.Log;

import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import com.applanga.android.Applanga;
import com.applanga.android.ApplangaCallback;

import java.util.HashMap;

/**
 * ApplangaFlutterPlugin
 */
public class ApplangaFlutterPlugin implements MethodCallHandler {
  
  private static Registrar registrar = null;
  /**
   * Plugin registration.
   */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "applanga_flutter");
    channel.setMethodCallHandler(new ApplangaFlutterPlugin());
    ApplangaFlutterPlugin.registrar = registrar;
    Applanga.init(registrar.activeContext());
  }

  @Override
  public void onMethodCall(MethodCall call, final Result result) {
    //Log.d("onMethodCall", "name = " + call.method);
    if (call.method.equals("getString")) {

      String key = call.argument("key");

      String defaultValue = call.argument("defaultValue");

      Log.d("applanga", "calling get string with key: " + key + " and default value: " + defaultValue);

      result.success(Applanga.getString(key, defaultValue));

    } else if (call.method.equals("update")){
      Applanga.update(new ApplangaCallback() {
        @Override
        public void onLocalizeFinished(final boolean b) {
          Log.d("applanga", String.format("onLocalizeFinished(%b)", b));
          if(registrar != null){
           registrar.activity().runOnUiThread(new Runnable() {
             @Override
             public void run() {
               result.success(b);
             }
           });
          }
        }
      });
    } else if(call.method.equals("localizeMap")) {
      HashMap<String, HashMap<String, String>> map =
              (call.arguments instanceof HashMap<?,?>) ? (HashMap)call.arguments : new HashMap<>();
      HashMap<String, HashMap<String,String>> applangaMap = Applanga.localizeMap(map);
      result.success(applangaMap);
    } else if(call.method.equals("isDebuggerConnected")) {
      result.success(android.os.Debug.isDebuggerConnected());
    } else if(call.method.equals("setLanguage")) {
      result.success(Applanga.setLanguage(call.arguments.toString()));
    } else if(call.method.equals("showDraftModeDialog")) {
      Activity activity = null;
      if(registrar != null){
        activity = registrar.activity();
      }
      if( activity != null ) {
        Applanga.showDraftModeDialog(activity);
        result.success(null);
      } else {
        result.error("DraftModeDialog", "Activity not found?", null);
      }
    }else if(call.method.equals("showScreenShotMenu")){
      Applanga.setScreenShotMenuVisible(true);
      result.success(null);
    }else if(call.method.equals("hideScreenShotMenu")){
      Applanga.setScreenShotMenuVisible(false);
      result.success(null);
    } else {
      result.notImplemented();
    }
  }
}
