/* Local Arabic keyboard; works without a device language pack. */
(()=>{
 const box=document.createElement('section');box.className='virtual-keyboard';box.hidden=true;box.setAttribute('aria-label','لوحة مفاتيح البحث');
 document.body.append(box);let active=null,english=false;
 const layouts={ar:['١ ٢ ٣ ٤ ٥ ٦ ٧ ٨ ٩ ٠','ض ص ث ق ف غ ع ه خ ح ج','ش س ي ب ل ا ت ن م ك ط','ئ ء ؤ ر لا ى ة و ز ظ د ذ'],en:['1 2 3 4 5 6 7 8 9 0','q w e r t y u i o p','a s d f g h j k l','z x c v b n m , . -']};
 function close(){box.hidden=true;document.body.classList.remove('keyboard-open');active=null}
 function refresh(){const display=box.querySelector('.keyboard-value');if(display)display.textContent=active?.value||'اكتب للبحث…'}
 function insert(text){if(!active)return;let a=active.selectionStart??active.value.length,b=active.selectionEnd??a;
  if(text==='DELETE'){if(a===b&&a>0)a--;text=''}
  active.setRangeText(text,a,b,'end');active.dispatchEvent(new Event('input',{bubbles:true}));refresh();
 }
 function key(row,label,action,cls=''){const b=document.createElement('button');b.type='button';b.textContent=label;b.className=cls;b.onpointerdown=e=>e.preventDefault();b.onclick=action;row.append(b);return b}
 function render(){box.replaceChildren();const head=document.createElement('div');head.className='keyboard-top';const display=document.createElement('div');display.className='keyboard-value';display.dir='auto';head.append(display);key(head,'إخفاء',close);box.append(head);
  for(const letters of layouts[english?'en':'ar']){const row=document.createElement('div');row.className='keyboard-row';row.dir=english?'ltr':'rtl';for(const letter of letters.split(' '))key(row,letter,()=>insert(letter));box.append(row)}
  const controls=document.createElement('div');controls.className='keyboard-row';key(controls,english?'عربي':'English',()=>{english=!english;render()});key(controls,'مسافة',()=>insert(' '),'space-key');key(controls,'⌫',()=>insert('DELETE'));key(controls,'بحث',()=>{const target=active?.id==='destination'?'searchBtn':'playBtn';close();document.getElementById(target).click()},'search-key');box.append(controls);refresh();
 }
 for(const id of ['destination','scUrl']){const field=document.getElementById(id);field.setAttribute('inputmode','none');field.setAttribute('autocomplete','off');field.addEventListener('focus',()=>{active=field;box.hidden=false;document.body.classList.add('keyboard-open');render();setTimeout(()=>field.scrollIntoView({block:'center',behavior:'smooth'}),80)});field.addEventListener('input',refresh);field.addEventListener('click',()=>{active=field;box.hidden=false;document.body.classList.add('keyboard-open');render()})}
 document.addEventListener('keydown',e=>{if(e.key==='Escape')close()});
})();

