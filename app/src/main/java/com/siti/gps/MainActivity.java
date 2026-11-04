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
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import androidx.core.content.FileProvider;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import android.view.WindowManager;
import androidx.webkit.WebViewAssetLoader;

public class MainActivity extends Activity {
  private WebView web;
  private WebView musicWeb;
  private android.app.Dialog musicDialog;
  private static final int LOCATION_REQUEST = 7;
  private long activeDownloadId=-1;
  private String activeApkName="";
  private BroadcastReceiver installReceiver;
  private final String releasesApi="https://api.github.com/repos/shakhatrehabdullah-wq/SitiGPS-Android/releases/latest";
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
        if("sitiupdate".equals(uri.getScheme()) && view.getUrl()!=null && view.getUrl().startsWith("https://appassets.androidplatform.net/")){ checkForUpdates(); return true; }
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
    installReceiver=new BroadcastReceiver(){@Override public void onReceive(Context context,Intent intent){long id=intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,-1);if(id==activeDownloadId)openUpdateInstaller();}};
    if(Build.VERSION.SDK_INT>=33)registerReceiver(installReceiver,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),Context.RECEIVER_NOT_EXPORTED);
    else registerReceiver(installReceiver,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
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

  private void checkForUpdates(){
    new Thread(()->{
      HttpURLConnection conn=null;
      try {
        conn=(HttpURLConnection)new URL(releasesApi).openConnection();
        conn.setRequestProperty("Accept","application/vnd.github+json");
        conn.setRequestProperty("User-Agent","SitiGPS-Android-UpdateCheck");
        conn.setConnectTimeout(12000);conn.setReadTimeout(12000);
        if(conn.getResponseCode()!=200)throw new Exception("HTTP "+conn.getResponseCode());
        StringBuilder sb=new StringBuilder();
        try(BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"))){String line;while((line=br.readLine())!=null)sb.append(line);}
        JSONObject json=new JSONObject(sb.toString());
        String tag=json.optString("tag_name","");
        String current=getPackageManager().getPackageInfo(getPackageName(),0).versionName;
        int remote=parseVersion(tag),local=parseVersion(current);
        String url=null;
        org.json.JSONArray assets=json.optJSONArray("assets");
        if(assets!=null)for(int i=0;i<assets.length();i++){
          JSONObject item=assets.getJSONObject(i);
          if(item.optString("name","").endsWith(".apk")){url=item.optString("browser_download_url",null);break;}
        }
        final String apkUrl=url,ver=tag;
        runOnUiThread(()->{
          if(remote<=local){
            new AlertDialog.Builder(this).setTitle("تحديثات Siti GPS").setMessage("التطبيق محدّث إلى آخر إصدار متوفر ("+current+").").setPositiveButton("حسناً",null).show();
          }else if(apkUrl==null){
            new AlertDialog.Builder(this).setTitle("تحديث جديد").setMessage("إصدار "+ver+" متوفر، لكن رابط APK غير متاح حالياً.").setPositiveButton("حسناً",null).show();
          }else{
            new AlertDialog.Builder(this).setTitle("تحديث جديد متوفر")
              .setMessage("الإصدار "+ver+" متوفر. هل تريد تنزيل التحديث الآن؟ سيطلب أندرويد موافقتك على التثبيت. يجب أن تكون النسختان بنفس توقيع التطبيق.")
              .setPositiveButton("Download now",(dialog,which)->downloadUpdate(apkUrl))
              .setNegativeButton("لاحقاً",null).show();
          }
        });
      }catch(Exception e){
        runOnUiThread(()->new AlertDialog.Builder(this).setTitle("تعذّر فحص التحديث")
          .setMessage("تحقق من اتصال الإنترنت وحاول مرة أخرى.\n"+e.getMessage()).setPositiveButton("حسناً",null).show());
      }finally{if(conn!=null)conn.disconnect();}
    }).start();
  }
  private int parseVersion(String name){
    java.util.regex.Matcher m=java.util.regex.Pattern.compile("(\\d+)\\.(\\d+)(?:\\.(\\d+))?").matcher(name);
    if(!m.find())return 0;
    int major=Integer.parseInt(m.group(1)),minor=Integer.parseInt(m.group(2));
    int patch=m.group(3)==null?0:Integer.parseInt(m.group(3));
    return major*1000000+minor*1000+patch;
  }
  private void downloadUpdate(String url){
    try{
      DownloadManager.Request request=new DownloadManager.Request(Uri.parse(url));
      activeApkName="siti-update-"+System.currentTimeMillis()+".apk";
      request.setTitle("Siti GPS update");
      request.setDescription("تنزيل تحديث التطبيق");
      request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
      request.setDestinationInExternalFilesDir(this,Environment.DIRECTORY_DOWNLOADS,activeApkName);
      DownloadManager dm=(DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
      activeDownloadId=dm.enqueue(request);
      new AlertDialog.Builder(this).setTitle("بدأ تنزيل التحديث").setMessage("جاري التنزيل. بعد الانتهاء ستظهر شاشة تثبيت أندرويد.")
        .setPositiveButton("حسناً",null).show();
    }catch(Exception e){new AlertDialog.Builder(this).setMessage("لم يبدأ التنزيل: "+e.getMessage()).setPositiveButton("حسناً",null).show();}
  }
  private void openUpdateInstaller(){
    try{
      if(Build.VERSION.SDK_INT>=26 && !getPackageManager().canRequestPackageInstalls()){
        new AlertDialog.Builder(this).setTitle("إذن التثبيت")
          .setMessage("اسمح لهذا التطبيق بتثبيت التحديثات ثم أعد طلب التحديث. لا تحذف النسخة المثبتة.")
          .setPositiveButton("فتح الإعدادات",(d,w)->{
            Intent i=new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,Uri.parse("package:"+getPackageName()));
            startActivity(i);
          }).setNegativeButton("لاحقاً",null).show();return;
      }
      File apk=new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),activeApkName);
      if(!apk.isFile()||apk.length()<50000)throw new Exception("ملف التحديث لم يكتمل");
      Uri content=FileProvider.getUriForFile(this,getPackageName()+".fileprovider",apk);
      Intent intent=new Intent(Intent.ACTION_VIEW);
      intent.setDataAndType(content,"application/vnd.android.package-archive");
      intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(intent);
    }catch(Exception e){
      new AlertDialog.Builder(this).setTitle("تعذّر فتح التحديث").setMessage(e.getMessage()).setPositiveButton("حسناً",null).show();
    }
  }

  String pendingOrigin; GeolocationPermissions.Callback pendingCallback;
  @Override public void onRequestPermissionsResult(int req,String[] perms,int[] results){super.onRequestPermissionsResult(req,perms,results);
   if(req==LOCATION_REQUEST && pendingCallback!=null){pendingCallback.invoke(pendingOrigin,results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED,false);pendingCallback=null;}
  }
  @Override public void onConfigurationChanged(android.content.res.Configuration config){super.onConfigurationChanged(config);immersive();web.post(()->web.evaluateJavascript("if(typeof map!=='undefined' && map)map.invalidateSize();",null));}
  @Override public void onWindowFocusChanged(boolean hasFocus){super.onWindowFocusChanged(hasFocus);if(hasFocus) immersive();}
  @Override public void onBackPressed(){if(web.canGoBack())web.goBack();else super.onBackPressed();}
  @Override protected void onDestroy(){if(installReceiver!=null){try{unregisterReceiver(installReceiver);}catch(Exception ignored){}}if(musicWeb!=null)musicWeb.destroy();if(musicDialog!=null)musicDialog.dismiss();web.destroy();super.onDestroy();}
}

