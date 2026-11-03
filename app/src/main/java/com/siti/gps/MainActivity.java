package com.siti.gps;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.view.View;
import android.view.WindowManager;
import androidx.webkit.WebViewAssetLoader;

public class MainActivity extends Activity {
  private WebView web;
  private WebView musicWeb;
  private android.app.Dialog musicDialog;
  private static final int LOCATION_REQUEST = 7;
  private static final int MICROPHONE_REQUEST = 8;
  private android.webkit.PermissionRequest microphoneRequest;
  private static final int VEHICLE_REQUEST = 9;
  private VehicleReader vehicleReader;
  private androidx.webkit.JavaScriptReplyProxy vehiclePermissionReply;
  private final java.util.concurrent.ExecutorService vehicleExecutor=java.util.concurrent.Executors.newSingleThreadExecutor();
  private void immersive(){
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
  }
  @Override public void onCreate(Bundle b){super.onCreate(b);immersive();
    web=new WebView(this);setContentView(web,new android.view.ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,android.view.ViewGroup.LayoutParams.MATCH_PARENT));
    web.setOnSystemUiVisibilityChangeListener(visibility->{if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN)==0)web.postDelayed(()->immersive(),2000);});
    WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setGeolocationEnabled(true);
    s.setMediaPlaybackRequiresUserGesture(true);
    s.setUserAgentString(s.getUserAgentString()+" SitiGPSAndroid/1.5");
    vehicleReader=new VehicleReader(this);
    if(androidx.webkit.WebViewFeature.isFeatureSupported(androidx.webkit.WebViewFeature.WEB_MESSAGE_LISTENER)){
      androidx.webkit.WebViewCompat.addWebMessageListener(web,"SitiVehicle",java.util.Collections.singleton("https://appassets.androidplatform.net"),(view,message,origin,isMainFrame,reply)->{
        if(!isMainFrame || !"https".equals(origin.getScheme()) || !"appassets.androidplatform.net".equals(origin.getHost()))return;
        String command=message.getData();
        if(!"snapshot".equals(command)&&!"connect".equals(command))return;
        if("connect".equals(command)&&vehicleReader.supported()&&checkSelfPermission("android.car.permission.CAR_ENERGY")!=PackageManager.PERMISSION_GRANTED){vehiclePermissionReply=reply;requestPermissions(new String[]{"android.car.permission.CAR_ENERGY"},VEHICLE_REQUEST);}
        else sendVehicleSnapshot(reply);
      });
    }
    WebViewAssetLoader loader=new WebViewAssetLoader.Builder().addPathHandler("/assets/",new WebViewAssetLoader.AssetsPathHandler(this)).build();
    web.setWebViewClient(new WebViewClient(){
      @Override public boolean shouldOverrideUrlLoading(WebView view,android.webkit.WebResourceRequest request){
        android.net.Uri uri=request.getUrl();
        if("sitimusic".equals(uri.getScheme()) && view.getUrl()!=null && view.getUrl().startsWith("https://appassets.androidplatform.net/")){
          showMusic("search".equals(uri.getHost()) ? uri.getQueryParameter("q") : null);return true;
        }
        return !("https".equals(uri.getScheme()) && "appassets.androidplatform.net".equals(uri.getHost()) && uri.getPath()!=null && uri.getPath().startsWith("/assets/"));
      }
      @Override public android.webkit.WebResourceResponse shouldInterceptRequest(WebView view,android.webkit.WebResourceRequest request){return loader.shouldInterceptRequest(request.getUrl());}
    });
    web.setWebChromeClient(new WebChromeClient(){
      @Override public void onPermissionRequest(android.webkit.PermissionRequest request){runOnUiThread(()->{
        if(!"https".equals(request.getOrigin().getScheme()) || !"appassets.androidplatform.net".equals(request.getOrigin().getHost())){request.deny();return;}
        String[] resources=request.getResources();
        if(resources.length!=1 || !android.webkit.PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(resources[0])){request.deny();return;}
        if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED)request.grant(new String[]{android.webkit.PermissionRequest.RESOURCE_AUDIO_CAPTURE});
        else {if(microphoneRequest!=null)microphoneRequest.deny();microphoneRequest=request;requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},MICROPHONE_REQUEST);}
      });}
      @Override public void onPermissionRequestCanceled(android.webkit.PermissionRequest request){if(microphoneRequest==request)microphoneRequest=null;}
      @Override public void onGeolocationPermissionsShowPrompt(String origin,GeolocationPermissions.Callback cb){
        if(!"https://appassets.androidplatform.net".equals(origin)){cb.invoke(origin,false,false);return;}
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)cb.invoke(origin,true,false);
        else {pendingOrigin=origin; pendingCallback=cb; requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},LOCATION_REQUEST);}
      }
    });
    web.loadUrl("https://appassets.androidplatform.net/assets/index.html");
  }
  private void showMusic(String query){
    if(musicDialog==null){
      musicDialog=new android.app.Dialog(this);
      android.widget.LinearLayout layout=new android.widget.LinearLayout(this);layout.setOrientation(android.widget.LinearLayout.VERTICAL);layout.setBackgroundColor(0xff101b31);
      android.widget.LinearLayout bar=new android.widget.LinearLayout(this);bar.setOrientation(android.widget.LinearLayout.HORIZONTAL);
      android.widget.Button back=new android.widget.Button(this);back.setText("رجوع");back.setOnClickListener(v->{if(musicWeb.canGoBack())musicWeb.goBack();});bar.addView(back);
      android.widget.TextView title=new android.widget.TextView(this);title.setText("SoundCloud · ابحث ثم اضغط تشغيل");title.setTextColor(0xffffffff);title.setPadding(12,12,12,12);bar.addView(title,new android.widget.LinearLayout.LayoutParams(0,-2,1));
      android.widget.Button close=new android.widget.Button(this);close.setText("إخفاء");close.setOnClickListener(v->musicDialog.hide());bar.addView(close);layout.addView(bar);
      android.widget.Button stop=new android.widget.Button(this);stop.setText("إيقاف");stop.setOnClickListener(v->{musicWeb.loadUrl("about:blank");musicDialog.hide();});bar.addView(stop);
      title.setTextSize(12);back.setTextSize(12);close.setTextSize(12);stop.setTextSize(12);
      musicWeb=new WebView(this);musicWeb.getSettings().setJavaScriptEnabled(true);musicWeb.getSettings().setDomStorageEnabled(true);musicWeb.getSettings().setMediaPlaybackRequiresUserGesture(true);musicWeb.setWebChromeClient(new WebChromeClient());
      musicWeb.setWebViewClient(new WebViewClient(){@Override public boolean shouldOverrideUrlLoading(WebView view,android.webkit.WebResourceRequest req){return !"https".equals(req.getUrl().getScheme());}});
      layout.addView(musicWeb,new android.widget.LinearLayout.LayoutParams(-1,0,1));musicDialog.setContentView(layout);
      musicDialog.setOnKeyListener((d,key,event)->{if(key==android.view.KeyEvent.KEYCODE_BACK&&event.getAction()==android.view.KeyEvent.ACTION_UP){musicDialog.hide();return true;}return false;});
    }
    musicDialog.show();sizeMusicDialog();
    if(query!=null)musicWeb.loadUrl("https://soundcloud.com/search/sounds?q="+android.net.Uri.encode(query));
    else if(musicWeb.getUrl()==null || "about:blank".equals(musicWeb.getUrl()))musicWeb.loadUrl("https://soundcloud.com/search");
  }
  private void sizeMusicDialog(){if(musicDialog!=null&&musicDialog.isShowing())musicDialog.getWindow().setLayout((int)(getResources().getDisplayMetrics().widthPixels*.96),(int)(getResources().getDisplayMetrics().heightPixels*.94));}
  private void sendVehicleSnapshot(androidx.webkit.JavaScriptReplyProxy reply){
    if(vehicleExecutor.isShutdown())return;
    vehicleExecutor.execute(()->{String result=vehicleReader.snapshot();web.post(()->{if(!isFinishing()&&!isDestroyed())reply.postMessage(result);});});
  }
  String pendingOrigin; GeolocationPermissions.Callback pendingCallback;
  @Override public void onRequestPermissionsResult(int req,String[] perms,int[] results){super.onRequestPermissionsResult(req,perms,results);
   if(req==VEHICLE_REQUEST&&vehiclePermissionReply!=null){androidx.webkit.JavaScriptReplyProxy reply=vehiclePermissionReply;vehiclePermissionReply=null;sendVehicleSnapshot(reply);}
   if(req==MICROPHONE_REQUEST && microphoneRequest!=null){if(results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED)microphoneRequest.grant(new String[]{android.webkit.PermissionRequest.RESOURCE_AUDIO_CAPTURE});else microphoneRequest.deny();microphoneRequest=null;}
   if(req==LOCATION_REQUEST && pendingCallback!=null){pendingCallback.invoke(pendingOrigin,results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED,false);pendingCallback=null;}
  }
  @Override public void onConfigurationChanged(android.content.res.Configuration config){super.onConfigurationChanged(config);immersive();sizeMusicDialog();web.post(()->web.evaluateJavascript("if(typeof map!=='undefined' && map)map.invalidateSize();",null));}
  @Override public void onWindowFocusChanged(boolean hasFocus){super.onWindowFocusChanged(hasFocus);if(hasFocus) immersive();}
  @Override public void onBackPressed(){if(web.canGoBack())web.goBack();else super.onBackPressed();}
  @Override protected void onDestroy(){vehiclePermissionReply=null;vehicleExecutor.shutdownNow();if(vehicleReader!=null)vehicleReader.close();if(musicWeb!=null)musicWeb.destroy();if(musicDialog!=null)musicDialog.dismiss();web.destroy();super.onDestroy();}
}



