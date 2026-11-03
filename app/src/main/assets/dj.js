/* Opt-in, local-only microphone analysis. Never records or uploads audio. */
(()=>{
 const button=document.createElement('button');button.className='pill';button.textContent='DJ صوت';button.setAttribute('aria-pressed','false');button.title='إضاءة تتفاعل مع الصوت عبر الميكروفون';document.querySelector('.neon-tools').append(button);
 const label=document.createElement('span');label.className='tiny dj-status';label.setAttribute('role','status');document.querySelector('.neon-tools').append(label);
 let stream=null,context=null,frame=0,starting=false,generation=0;
 function stop(){generation++;cancelAnimationFrame(frame);if(stream)stream.getTracks().forEach(t=>t.stop());stream=null;if(context)context.close().catch(()=>{});context=null;document.body.classList.remove('dj-active');button.setAttribute('aria-pressed','false');button.textContent='DJ صوت';label.textContent='';}
 button.onclick=async()=>{
   if(stream||starting){starting=false;stop();return}
   const id=++generation;starting=true;button.textContent='إلغاء DJ';label.textContent='السماح بالميكروفون مطلوب';
   try{
     const Audio=window.AudioContext||window.webkitAudioContext;if(!Audio||!navigator.mediaDevices?.getUserMedia)throw Error('الميكروفون غير متاح في هذا المتصفح');
     context=new Audio();await context.resume();
     const captured=await navigator.mediaDevices.getUserMedia({audio:{echoCancellation:false,noiseSuppression:false,autoGainControl:false},video:false});
     if(id!==generation){captured.getTracks().forEach(t=>t.stop());return}stream=captured;
     const analyser=context.createAnalyser();analyser.fftSize=256;analyser.smoothingTimeConstant=.7;context.createMediaStreamSource(stream).connect(analyser);
     const waveform=new Uint8Array(analyser.fftSize),bins=new Uint8Array(analyser.frequencyBinCount);let last=0,hue=190;
     document.body.classList.add('dj-active');button.textContent='إيقاف DJ';button.setAttribute('aria-pressed','true');label.textContent='المايك فعّال · تحليل محلي';
     function tick(time){frame=requestAnimationFrame(tick);if(time-last<33)return;last=time;analyser.getByteTimeDomainData(waveform);analyser.getByteFrequencyData(bins);let energy=0,bass=0;for(const sample of waveform)energy+=((sample-128)/128)**2;for(let i=1;i<12;i++)bass+=bins[i];const level=Math.min(1,Math.sqrt(energy/waveform.length)*7),beat=bass/(11*255);hue=(hue+.2+beat*4)%360;document.body.style.setProperty('--dj-color','hsl('+hue+',100%,60%)');document.body.style.setProperty('--dj-glow',(5+level*42)+'px');document.body.style.setProperty('--dj-level',String(.12+level*.88));document.body.style.setProperty('--dj-scale',String(.25+level*.75));}
     frame=requestAnimationFrame(tick);stream.getTracks()[0].onended=stop;
   }catch(e){stop();label.textContent=e.name==='NotAllowedError'?'لم يُسمح بالميكروفون':(e.message||'تعذّر تشغيل إضاءة الصوت')}
   finally{if(id===generation)starting=false}
 };
 document.addEventListener('visibilitychange',()=>{if(document.hidden){starting=false;stop()}});
 window.addEventListener('pagehide',stop);
})();

