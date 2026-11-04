/* Siti GPS live UI delivery. First boot uses bundled assets; HTTPS updates from
 * the official repository are loaded in the same Android WebView. UI only.
 * Native Android permissions and Java require a normally installed APK. */
(function(){
 var base='https://raw.githubusercontent.com/shakhatrehabdullah-wq/SitiGPS-Android/main/app/src/main/assets/';
 var stamp=String(Date.now());
 var status='جارٍ فحص تحديث الواجهة';
 window.SitiLive={status:status,updated:false,check:function(){location.reload();}};
 var css=document.createElement('link');css.rel='stylesheet';css.href='siti-layout.css';document.head.appendChild(css);
 // The fresh remote stylesheet overlays the bundled look; on network failure
 // the bundled sheet above remains valid.
 var remoteCss=document.createElement('link');remoteCss.rel='stylesheet';
 remoteCss.href=base+'siti-layout.css?fresh='+stamp;
 remoteCss.onload=function(){window.SitiLive.updated=true;window.SitiLive.status='تم تحميل أحدث ألوان وتصميم';};
 remoteCss.onerror=function(){window.SitiLive.status='واجهة الجهاز المحفوظة — لا يوجد اتصال';};
 document.head.appendChild(remoteCss);
 // IMPORTANT: execute only one copy of the dynamic UI. Remote when available;
 // bundled local JS on failure.
 var remoteJs=document.createElement('script');
 remoteJs.src=base+'siti-layout.js?fresh='+stamp;
 remoteJs.onload=function(){window.SitiLive.updated=true;window.SitiLive.status='آخر إصدار للواجهة';};
 remoteJs.onerror=function(){
   var local=document.createElement('script');local.src='siti-layout.js';
   document.body.appendChild(local);
 };
 document.body.appendChild(remoteJs);
})();