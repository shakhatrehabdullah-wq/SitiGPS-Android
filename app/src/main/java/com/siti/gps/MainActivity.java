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
  private void immersive(){
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
  }
  @Override public void onCreate(Bundle b){super.onCreate(b);immersive();
    web=new WebView(this);setContentView(web,new android.view.ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,android.view.ViewGroup.LayoutParams.MATCH_PARENT));
    web.setOnSystemUiVisibilityChangeListener(visibility->{if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN)==0)web.postDelayed(()->immersive(),2000);});
    WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setGeolocationEnabled(true);
    s.setMediaPlaybackRequiresUserGesture(true);
    WebViewAssetLoader loader=new WebViewAssetLoader.Builder().addPathHandler("/assets/",new WebViewAssetLoader.AssetsPathHandler(this)).build();
    web.setWebViewClient(new WebViewClient(){
      @Override public boolean shouldOverrideUrlLoading(WebView view,android.webkit.WebResourceRequest request){
        android.net.Uri uri=request.getUrl();
        if("sitimusic".equals(uri.getScheme()) && view.getUrl()!=null && view.getUrl().startsWith("https://appassets.androidplatform.net/")){
          showMusic("search".equals(uri.getHost()) ? uri.getQueryParameter("q") : null);return true;
        }
        return !"https".equals(uri.getScheme());
      }
      @Override public android.webkit.WebResourceResponse shouldInterceptRequest(WebView view,android.webkit.WebResourceRequest request){return loader.shouldInterceptRequest(request.getUrl());}
    });
    web.setWebChromeClient(new WebChromeClient(){
      @Override public void onGeolocationPermissionsShowPrompt(String origin,GeolocationPermissions.Callback cb){
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)cb.invoke(origin,true,false);
        else {pendingOrigin=origin; pendingCallback=cb; requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},LOCATION_REQUEST);}
      }
    });
    web.loadUrl("https://appassets.androidplatform.net/assets/index.html");
  }
  private void showMusic(String query){
    if(musicDialog==null){
      musicDialog=new android.app.Dialog(this);
      android.widget.LinearLayout layout=new android.widget.LinearLayout(this);layout.setOrientation(1);layout.setBackgroundColor(0xff101b31);
      android.widget.LinearLayout bar=new android.widget.LinearLayout(this);bar.setOrientation(0);
      android.widget.Button back=new android.widget.Button(this);back.setText("رجوع");back.setOnClickListener(v->{if(musicWeb.canGoBack())musicWeb.goBack();});bar.addView(back);
      android.widget.TextView title=new android.widget.TextView(this);title.setText("SoundCloud · ابحث ثم اضغط تشغيل");title.setTextColor(0xffffffff);title.setPadding(12,12,12,12);bar.addView(title,new android.widget.LinearLayout.LayoutParams(0,-2,1));
      android.widget.Button close=new android.widget.Button(this);close.setText("إخفاء");close.setOnClickListener(v->musicDialog.hide());bar.addView(close);layout.addView(bar);
      musicWeb=new WebView(this);musicWeb.getSettings().setJavaScriptEnabled(true);musicWeb.getSettings().setDomStorageEnabled(true);musicWeb.getSettings().setMediaPlaybackRequiresUserGesture(false);musicWeb.setWebChromeClient(new WebChromeClient());
      musicWeb.setWebViewClient(new WebViewClient(){@Override public boolean shouldOverrideUrlLoading(WebView view,android.webkit.WebResourceRequest req){return !"https".equals(req.getUrl().getScheme());}});
      layout.addView(musicWeb,new android.widget.LinearLayout.LayoutParams(-1,0,1));musicDialog.setContentView(layout);
      musicDialog.setOnKeyListener((d,key,event)->{if(key==android.view.KeyEvent.KEYCODE_BACK&&event.getAction()==android.view.KeyEvent.ACTION_UP){musicDialog.hide();return true;}return false;});
    }
    musicDialog.show();musicDialog.getWindow().setLayout((int)(getResources().getDisplayMetrics().widthPixels*.88),(int)(getResources().getDisplayMetrics().heightPixels*.92));
    if(query!=null)musicWeb.loadUrl("https://soundcloud.com/search/sounds?q="+android.net.Uri.encode(query));
    else if(musicWeb.getUrl()==null)musicWeb.loadUrl("https://soundcloud.com/search");
  }
  String pendingOrigin; GeolocationPermissions.Callback pendingCallback;
  @Override public void onRequestPermissionsResult(int req,String[] perms,int[] results){super.onRequestPermissionsResult(req,perms,results);
   if(req==LOCATION_REQUEST && pendingCallback!=null){pendingCallback.invoke(pendingOrigin,results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED,false);pendingCallback=null;}
  }
  @Override public void onConfigurationChanged(android.content.res.Configuration config){super.onConfigurationChanged(config);immersive();web.post(()->web.evaluateJavascript("if(typeof map!=='undefined' && map)map.invalidateSize();",null));}
  @Override public void onWindowFocusChanged(boolean hasFocus){super.onWindowFocusChanged(hasFocus);if(hasFocus) immersive();}
  @Override public void onBackPressed(){if(web.canGoBack())web.goBack();else super.onBackPressed();}
  @Override protected void onDestroy(){if(musicWeb!=null)musicWeb.destroy();if(musicDialog!=null)musicDialog.dismiss();web.destroy();super.onDestroy();}
}

