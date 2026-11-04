/* Siti GPS UI updates: local-first, then refresh remote visual assets.
   No APK permission required to update UI; native app code still needs a new APK. */
(function(){
 var base='https://raw.githubusercontent.com/shakhatrehabdullah-wq/SitiGPS-Android/main/app/src/main/assets/';
 var updated=false;
 window.SitiLive={
  status:'الواجهة المحلية جاهزة',updated:false,
  check:function(){refresh(true);}
 };
 function message(m,manual){window.SitiLive.status=m;if(manual)alert(m);}
 function refresh(manual){
  var stamp=Date.now();
  var css=document.createElement('link');css.rel='stylesheet';
  css.href=base+'siti-layout.css?ts='+stamp;
  css.onload=function(){updated=true;window.SitiLive.updated=true;};
  css.onerror=function(){if(manual)message('تعذّر تنزيل تصميم الواجهة، تم الاحتفاظ بالتصميم الموجود.',true);};
  document.head.appendChild(css);
  var js=document.createElement('script');js.async=true;
  js.src=base+'siti-layout.js?ts='+stamp;
  js.onload=function(){updated=true;window.SitiLive.updated=true;message('تم تحديث الواجهة من الإنترنت بنجاح',manual);};
  js.onerror=function(){if(manual)message('فشل تحديث عناصر الواجهة. التصميم المحفوظ ما زال يعمل.',true);};
  document.body.appendChild(js);
 }
 // The bundled dashboard is loaded synchronously by index.html first. Network
 // failure or slow load never prevents the dashboard from appearing.
 setTimeout(function(){refresh(false);},900);
})();