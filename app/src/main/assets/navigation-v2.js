/* Heading-up uses GPS course over ground, never the screen/device orientation. */
(()=>{
  let course=null,lastFix=null;
  headingUp=true;
  function syncHeading(){
    if(!map||!map.setHeading)return;
    if(headingUp&&course!==null)map.setHeading(course,{ease:.15,deadzone:1});
    else if(!headingUp){map.stopHeadingUp();map.setBearing(0)}
    el('northBtn').textContent=headingUp?'اتجاه السيارة ↑':'الشمال ↑';
    el('northBtn').setAttribute('aria-pressed',String(headingUp));
  }
  function syncIcon(){
    if(marker)marker.setIcon(markerIcon(course===null?heading:course));
    const compass=document.querySelector('.compass');
    if(compass&&map)compass.style.transform='rotate('+map.getBearing()+'deg)';
  }
  if(map)map.on('rotate',syncIcon);
  el('northBtn').onclick=()=>{headingUp=!headingUp;syncHeading();syncIcon()};
  const compass=document.querySelector('.compass');
  compass.setAttribute('role','button');compass.setAttribute('tabindex','0');compass.setAttribute('aria-label','تثبيت الخريطة إلى الشمال');
  compass.onclick=()=>{headingUp=false;syncHeading();syncIcon()};
  compass.onkeydown=e=>{if(e.key==='Enter'||e.key===' '){e.preventDefault();compass.click()}};
  const previousUpdate=update;
  update=function(p){
    const c=p.coords,kmh=Number.isFinite(c.speed)?c.speed*3.6:0;
    if(c.accuracy<=60&&kmh>=4){
      if(Number.isFinite(c.heading)&&c.heading>=0)course=c.heading%360;
      else if(lastFix&&lastFix.accuracy<=60&&p.timestamp>lastFix.time&&p.timestamp-lastFix.time<15000&&meters(lastFix,{lat:c.latitude,lng:c.longitude})>Math.max(10,c.accuracy)){
        const phi1=lastFix.lat*RAD,phi2=c.latitude*RAD,dl=(c.longitude-lastFix.lng)*RAD;
        course=(Math.atan2(Math.sin(dl)*Math.cos(phi2),Math.cos(phi1)*Math.sin(phi2)-Math.sin(phi1)*Math.cos(phi2)*Math.cos(dl))/RAD+360)%360;
      }
    }
    lastFix={lat:c.latitude,lng:c.longitude,accuracy:c.accuracy,time:p.timestamp};
    if(course!==null){heading=course;el('heading').textContent=Math.round(course)+'°'}
    previousUpdate(p);syncHeading();syncIcon();
  };
  const previousRoute=route;
  route=async function(recalculation=false){if(!recalculation)headingUp=true;await previousRoute(recalculation);syncHeading()};
  el('routeBtn').onclick=()=>route(false);
  const resize=()=>{if(map)map.invalidateSize({pan:false})};
  window.addEventListener('resize',()=>setTimeout(resize,150));
  if(window.ResizeObserver)new ResizeObserver(resize).observe(document.querySelector('.mapframe'));
  syncHeading();
  if(!navigator.userAgent.includes('SitiGPSAndroid/')){
    const hint=document.createElement('div');hint.className='sound-hint';hint.textContent='بحث SoundCloud يفتح صفحة الخدمة؛ الصق رابط أغنية لتشغيلها هنا';document.querySelector('.music').append(hint);
    const frame=el('soundFrame');const stop=document.createElement('button');stop.className='pill';stop.textContent='إيقاف الصوت';stop.onclick=()=>{el('scFrame').src='about:blank';frame.classList.remove('visible')};frame.prepend(stop);
    el('playBtn').onclick=()=>{
      const q=el('scUrl').value.trim();if(!q)return;
      let url;try{url=new URL(q)}catch(e){}
      if(url&&url.protocol==='https:'&&(url.hostname==='soundcloud.com'||url.hostname.endsWith('.soundcloud.com'))){el('scFrame').src='https://w.soundcloud.com/player/?url='+encodeURIComponent(url.href)+'&auto_play=true&hide_related=true&show_comments=false';frame.classList.add('visible')}
      else window.open('https://soundcloud.com/search/sounds?q='+encodeURIComponent(q),'siti-soundcloud','noopener');
    };
    el('scUrl').onkeydown=e=>{if(e.key==='Enter')el('playBtn').click()};el('musicToggle').onclick=()=>frame.classList.toggle('visible');
  }
})();

