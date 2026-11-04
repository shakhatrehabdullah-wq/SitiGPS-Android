/* Siti GPS BYD dashboard layout: preserves legacy GPS, map and SoundCloud handlers. */
(function(){
 var board=document.querySelector('.board'),gauges=document.querySelector('.gauges'),mapcol=document.querySelector('.mapcol');
 if(!board||!gauges||!mapcol)return;
 // Safe re-render when an updated UI script arrives: preserve map/GPS and music.
 var oldSong=document.querySelector('.siti-song');
 if(oldSong){var player=oldSong.querySelector('.music');var footer=document.querySelector('.footer');if(player&&footer)footer.insertBefore(player,footer.firstChild);}
 ['.siti-ambience','.siti-dj-stage','.siti-left-tools','.siti-gps-row','.siti-song','.siti-vehicle','.siti-update'].forEach(function(q){var node=document.querySelector(q);if(node&&node.parentNode)node.parentNode.removeChild(node)});

 var speed=document.querySelector('.speed'),mini=document.querySelector('.minigauges');
 var wave=document.createElement('div');wave.className='siti-ambience';wave.setAttribute('aria-hidden','true');
 ['left','right'].forEach(function(side){var w=document.createElement('div');w.className='siti-waves '+side;for(var i=0;i<12;i++){var b=document.createElement('i');b.style.setProperty('--h',(30+(i*19+13)%67)+'%');b.style.setProperty('--delay',(-i*.11)+'s');w.appendChild(b)}wave.appendChild(w)});
 speed.insertBefore(wave,speed.firstChild);
 // Dedicated large side bars, outside the tiny speed value; use non-audio ambient animation.
 var visuals=document.createElement('div');visuals.className='siti-dj-stage';visuals.setAttribute('aria-hidden','true');
 ['left','right'].forEach(function(side){var bank=document.createElement('div');bank.className='siti-dj-bank '+side;for(var k=0;k<16;k++){var bar=document.createElement('span');bar.style.setProperty('--i',String(k));bar.style.setProperty('--delay',(-.07*k)+'s');bar.style.setProperty('--bar-height',(28+(k*23)%67)+'%');bank.appendChild(bar)}visuals.appendChild(bank)});
 speed.appendChild(visuals);
 var tools=document.createElement('div');tools.className='siti-left-tools';gauges.insertBefore(tools,speed);
 var t=document.createElement('span');t.className='siti-status';t.textContent='';t.setAttribute('aria-hidden','true');tools.appendChild(t);
 // Style palette changes the complete gauge appearance, not just the label color.
 var palettes=[
  {name:'أزرق',color:'#19c8f2',rgb:'25,200,242',second:'#277bff'},
  {name:'بنفسجي',color:'#b07cff',rgb:'176,124,255',second:'#e462fd'},
  {name:'ذهبي',color:'#ffb342',rgb:'255,179,66',second:'#ef657a'}
 ];
 function applyPalette(index){
  index=Math.max(0,Math.min(palettes.length-1,index));
  var p=palettes[index];
  document.body.style.setProperty('--siti-accent',p.color);
  document.body.style.setProperty('--siti-accent-rgb',p.rgb);
  document.body.style.setProperty('--siti-accent-2',p.second);
  document.body.setAttribute('data-siti-palette',String(index));
  var dots=tools.querySelectorAll('.siti-color-dot');
  for(var j=0;j<dots.length;j++)dots[j].setAttribute('aria-pressed',String(j===index));
  try{localStorage.setItem('sitiPalette',String(index));}catch(e){}
 }
 palettes.forEach(function(p,index){
  var b=document.createElement('button');b.type='button';
  b.className='siti-color-dot';b.setAttribute('aria-label','لون العداد '+p.name);
  b.style.background='linear-gradient(135deg,'+p.color+','+p.second+')';
  b.onclick=function(){applyPalette(index)};
  tools.appendChild(b);
 });
 var saved=0;try{saved=parseInt(localStorage.getItem('sitiPalette')||'0',10)||0}catch(e){}
 var djEnabled=true;
 var dj=document.createElement('button');dj.type='button';dj.className='pill siti-dj-toggle';dj.textContent='DJ ON';
 dj.setAttribute('aria-pressed','true');
 dj.onclick=function(){djEnabled=!djEnabled;document.body.classList.toggle('siti-dj-off',!djEnabled);dj.setAttribute('aria-pressed',String(djEnabled));dj.textContent=djEnabled?'DJ ON':'DJ OFF'};
 tools.appendChild(dj);
 applyPalette(saved);
 var gpsrow=document.createElement('div');gpsrow.className='siti-gps-row';
 var nav=document.createElement('button');nav.className='primary';nav.textContent='Navigate · الملاحة';nav.onclick=function(){var input=document.getElementById('destination');if(input){input.focus();input.scrollIntoView({block:'nearest'})}};
 var fix=document.createElement('button');fix.className='pill';fix.textContent='GPS ↗';fix.onclick=function(){if(typeof follow!=='undefined'){follow=true;var b=document.getElementById('followBtn');if(b)b.textContent='تتبّع ✓'}if(typeof map!=='undefined'&&map&&typeof loc!=='undefined'&&loc){map.setView([loc.lat,loc.lng],16)}};
 gpsrow.appendChild(nav);gpsrow.appendChild(fix);gauges.appendChild(gpsrow);
 if(mini){mini.innerHTML='';for(var k=0;k<2;k++){var p=document.createElement('div');p.className='panel mini siti-rpm';p.innerHTML='<div class="siti-rpm-ring"><span class="siti-rpm-needle"></span><strong id="sitiRpm'+k+'">0</strong></div><div class="title">RPM · محاكاة '+(k+1)+'</div><div class="tiny">تقديري · ليس من السيارة</div>'; mini.appendChild(p)}}
 var song=document.createElement('div');song.className='panel siti-song';song.innerHTML='<h3>♫ SoundCloud · الموسيقى</h3>';var music=document.querySelector('.footer .music');if(music)song.appendChild(music);mapcol.appendChild(song);
 var vehicle=document.createElement('div');vehicle.className='panel siti-vehicle';vehicle.innerHTML='<h3>🚘 BYD Dolphin · بيانات السيارة</h3><div class="siti-vehicle-values"><div><small>البطارية</small><strong>غير متاح</strong></div><div><small>المدى المتبقي</small><strong>غير متاح</strong></div><div><small>المسافة المقطوعة</small><strong id="sitiTrip">0.00 km (GPS)</strong></div></div><div class="siti-vehicle-note">بيانات البطارية والمدى تحتاج واجهة قراءة معتمدة من نظام السيارة؛ لا نعرض أرقاماً افتراضية.</div>';mapcol.appendChild(vehicle);
 var btn=document.createElement('button');btn.className='pill siti-update';btn.textContent='↻ تحديث الواجهة';btn.onclick=function(){var yes=confirm('تحميل أحدث تصميم للواجهة الآن؟ سيُعاد فتح الشاشة دون حذف التطبيق.');if(yes){if(window.SitiLive)SitiLive.check();else location.reload()}};document.querySelector('.status').appendChild(btn);
 var count=0;var dial=document.querySelector('.speed .dial');if(dial){dial.removeAttribute('style');}
 function telemetry(){var v=parseFloat((document.getElementById('speed')||{}).textContent)||0;var a=document.getElementById('accel');var accel=a?parseFloat(a.textContent)||0:0;var rpm1=Math.max(0,Math.min(9000,Math.round(v*26+Math.abs(accel)*190)));var rpm2=Math.max(0,Math.min(9000,Math.round(v*18+Math.abs(accel)*240)));var r1=document.getElementById('sitiRpm0'),r2=document.getElementById('sitiRpm1');if(r1)r1.textContent=rpm1;if(r2)r2.textContent=rpm2;var rings=document.querySelectorAll('.siti-rpm-ring');if(rings[0])rings[0].style.setProperty('--rpm',Math.min(1,rpm1/9000));if(rings[1])rings[1].style.setProperty('--rpm',Math.min(1,rpm2/9000));var d=document.getElementById('distance'),trip=document.getElementById('sitiTrip');if(d&&trip)trip.textContent=d.textContent+' (GPS)';}
 var observed=document.getElementById('speed');if(window.MutationObserver&&observed)new MutationObserver(telemetry).observe(observed,{childList:true,subtree:true});telemetry();
 // Stable on-screen scaling. Do not alter shell, map or column dimensions.
 var zoomFactor=1;
 try {var stored=Number(localStorage.getItem('sitiDialScale'));if(isFinite(stored)&&stored>=.85&&stored<=1.2)zoomFactor=stored;}catch(e){}
 function fitDial(){
  var panel=document.querySelector('.speed'),d=document.querySelector('.speed .dial');
  if(!panel||!d)return;
  var usable=Math.max(96,panel.clientHeight-83);
  var base=Math.max(92,Math.min(panel.clientWidth*.82,usable*.91,355));
  var size=Math.min(base*zoomFactor,Math.max(92,usable),panel.clientWidth*.93);
  size=Math.round(size);
  d.style.setProperty('width',size+'px','important');
  d.style.setProperty('height',size+'px','important');
  d.style.setProperty('min-width','0px','important');
  d.style.setProperty('min-height','0px','important');
  d.style.setProperty('max-width',size+'px','important');
  d.style.setProperty('max-height',size+'px','important');
  d.style.setProperty('aspect-ratio','1','important');
 }
 function safeResize(){
  var shell=document.querySelector('.shell');
  if(shell){shell.style.zoom='1';shell.style.width='100%';shell.style.height='100%';shell.style.transform='none';}
  fitDial();
  try{if(typeof map!=='undefined'&&map)map.invalidateSize({pan:false});}catch(e){}
 }
 function wireSafeZoom(){
  var buttons=document.querySelectorAll('.status .scale-btn');
  for(var k=0;k<buttons.length;k++){
   (function(button){
    button.onclick=function(){
     var delta=button.textContent.indexOf('+')>=0?.05:-.05;
     zoomFactor=Math.max(.85,Math.min(1.2,Math.round((zoomFactor+delta)*100)/100));
     try{localStorage.setItem('sitiDialScale',String(zoomFactor));}catch(e){}
     safeResize();
    };
    button.title='تكبير/تصغير عداد السرعة بدون تغيير توزيع الشاشة';
   })(buttons[k]);
  }
 }
 // Legacy v1.6 uses fitDashboard() on orientation changes; replace its body too.
 try{window.fitDashboard=safeResize;}catch(e){}
 wireSafeZoom();
 window.addEventListener('resize',function(){setTimeout(safeResize,130)});
 safeResize();setTimeout(safeResize,180);setTimeout(safeResize,900);
})();
