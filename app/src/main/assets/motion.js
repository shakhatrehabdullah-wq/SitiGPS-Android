(()=>{
 const stage=document.createElement('div');stage.className='ambient-motion';stage.setAttribute('aria-hidden','true');
 for(const side of ['left','right']){const wave=document.createElement('div');wave.className='ambient-wave '+side;for(let i=0;i<13;i++){const bar=document.createElement('i');bar.style.setProperty('--i',i);bar.style.setProperty('--height',(20+((i*37+19)%70))+'%');wave.append(bar)}stage.append(wave)}
 document.querySelector('.speed').prepend(stage);
})();

