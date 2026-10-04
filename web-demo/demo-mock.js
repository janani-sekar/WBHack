// UI-only preview: replaces the local coach API with sample responses. No model runs here.
(()=>{
const F=window.DEMO_FIXTURES, sessions={}, lessons=[], batches=[];
let settings={...F.settings}, memory=JSON.parse(JSON.stringify(F.memory)), plan=F.plan, n=0;
const uid=()=>'demo'+(++n)+Date.now().toString(36), now=()=>new Date().toISOString();
const followUps={en:['Great — and is there a place to sit down there? My mother can’t walk far.','Perfect. Can I pay by card, or should I bring cash?','Thanks! Where exactly do we meet you when we arrive?'],
 es:['Perfecto, ¿y hay dónde sentarse? Mi mamá no puede caminar mucho.','Genial. ¿Puedo pagar con tarjeta o traigo efectivo?','¡Gracias! ¿Dónde exactamente nos encontramos al llegar?']};
const variants=[{s:[2,2,2,2,2],en:['You answered the guest’s question directly and used the confirmed facts.','You met all five criteria for this reply. Try a different situation next.'],es:['Respondiste directamente a la pregunta del huésped y usaste los datos confirmados.','Cumpliste los cinco criterios en esta respuesta. Prueba otra situación.']},
 {s:[2,2,1,2,2],en:['Warm tone and a clear next step for the guest.','One detail isn’t confirmed in your facts — say you will check it rather than promising it.'],es:['Tono cálido y un siguiente paso claro para el huésped.','Un detalle no está confirmado en tus datos: di que lo vas a verificar en lugar de prometerlo.']},
 {s:[2,1,2,1,2],en:['You were honest about what you don’t know yet.','Add a concrete next step, e.g. “I’ll confirm with the team and message you in 10 minutes.”'],es:['Fuiste honesto sobre lo que aún no sabes.','Agrega un siguiente paso concreto, p. ej. «Confirmo con el equipo y te escribo en 10 minutos».']}];
const dims=['answers_request','factual_accuracy','clarifies_unknowns','next_step','customer_tone'];
const firstSentence=t=>(t.match(/[^.!?¡¿]+[.!?]?/)||[t])[0].trim();
function caseFor(id){return F.curriculum.find(c=>c.id===id)||F.curriculum[0];}
function guestText(c,lang){const v=c.guest_variants&&c.guest_variants[lang];return (v&&(v[settings.style]||v.clear))||c.guest;}
function newSession(b){const c=caseFor(b.scenario_id),lesson=lessons.find(l=>l.id===b.lesson_id),lang=settings.guest_language||'en';
 const s={id:uid(),skill:c.skill,independent:!!b.independent,reason:'Operator-selected practice',lesson_id:b.lesson_id||null,turns:[],scenario_id:c.id,scenario:c,
  guest_message:guestText(c,lang),guest_language:lang,style:settings.style||'chat',question_index:0,coach_language:settings.coach_language||'en',support:settings.support||'brief',
  personalization:lesson?{review_sources:[{quote:lesson.evidence_quote}],review_focus:lesson.reason_local}:{},current_variants:{},created_at:now(),metrics:{provenance:'sample'},completed:false};
 sessions[s.id]=s;return s;}
function respond(s,text){const v=variants[s.turns.length%variants.length],a={uncertain:false,evidence_quote:firstSentence(text)};
 dims.forEach((d,i)=>a[d]=v.s[i]);const fv={en:{strength_local:v.en[0],improvement_local:v.en[1]},es:{strength_local:v.es[0],improvement_local:v.es[1]}};
 Object.assign(a,fv[s.coach_language]||fv.en);
 const t={id:uid(),response:text,guest_message:s.guest_message,assessment:a,status:'pending',metrics:{provenance:'sample'},assessment_issue:null,
  fact_check:{verdict:'supported',reason:'Checked against the confirmed facts.'},tone_check:null,coach_language:s.coach_language,guidance_sources:F.guidance,feedback_variants:fv,created_at:now(),question_index:s.question_index};
 s.turns.push(t);return t;}
function finish(s){s.completed=true;const ok=s.turns.filter(t=>t.status!=='rejected'&&!t.assessment.uncertain),means={};
 dims.forEach(d=>{if(ok.length)means[d]=Math.round(ok.reduce((x,t)=>x+t.assessment[d],0)/ok.length*10)/10;});
 const focus=dims.reduce((a,b)=>(means[a]??2)<=(means[b]??2)?a:b);
 s.summary={reply_count:s.turns.length,early_finish:s.turns.length<3,criterion_means:means,focus};return s;}
function approve(s,b){const t=s.turns.find(t=>t.id===b.turn_id);if(!t)return;t.status=b.approved?'accepted':'rejected';
 memory.skill_observations=memory.skill_observations.filter(o=>!(o.sources||[]).some(x=>x.turn_id===t.id));
 if(b.approved)memory.skill_observations.push({skill:s.skill,practice_focus:dims.find(d=>t.assessment[d]<2)||null,accepted_sessions:1,sources:[{quote:t.assessment.evidence_quote,session_id:s.id,turn_id:t.id}]});}
function reviewBatch(b){const es=b.language==='es',T=(e,s)=>es?s:e,groups=[];
 const text=b.reviews.join(' ').toLowerCase(),has=(...w)=>w.some(x=>text.includes(x));
 const add=(topic,skill,e,s,can,action)=>groups.push({id:uid(),topic,skill,can_practice:can,action,uncertain:false,sources:[{review_index:0,explanation_local:T(e,s)}]});
 if(has('park','estacion','lleg','find','encontr','direction','where','dónde','wait','esper'))add('arrival','directions','Guests weren’t sure where to go when they arrived.','Los visitantes no sabían a dónde ir al llegar.',true,'practice');
 if(has('price','precio','card','tarjeta','pay','pag','cost'))add('booking','expectations','Guests wanted to know the price and payment options before arriving.','Los visitantes querían saber el precio y las formas de pago antes de llegar.',true,'practice');
 if(has('long','larg','tired','cans','abur','bored','time','tiempo','hour','hora'))add('timing','duration','Some guests found the visit long — explain how long each part takes.','A algunos visitantes la visita les pareció larga: explica cuánto dura cada parte.',true,'practice');
 if(has('love','encant','delicious','delicios','beautiful','precios','great','paciencia','patient'))add('hospitality','duration','Guests praised the coffee and the host’s patience — keep doing this.','Los visitantes elogiaron el café y la paciencia del anfitrión: sigue así.',false,'keep');
 if(!groups.length)add('arrival','directions','Not enough detail to tell what the guest meant.','No hay suficiente detalle para saber qué quiso decir el visitante.',false,'improve'),groups[0].uncertain=true;
 const batch={id:uid(),language:b.language,created_at:now(),groups,reviews:b.reviews.map(r=>({original:r,analysis:{translation_local:T('(Sample preview — translation appears here in the local app.)','(Vista previa: aquí aparece la traducción en la app local.)')}}))};
 batches.push(batch);return batch;}
function batchLesson(b){const batch=batches.find(x=>x.id===b.batch_id),g=batch.groups.find(x=>x.id===b.group_id),c=F.curriculum.find(c=>c.skill===g.skill&&c.difficulty===1)||F.curriculum[0];
 const l={id:uid(),batch_id:batch.id,group_id:g.id,topic:g.topic,skill:g.skill,scenario_id:c.id,evidence_quote:batch.reviews[0].original.slice(0,160),reason_local:g.sources[0].explanation_local};lessons.push(l);return l;}
function route(method,path,b){let m;
 if(path==='/health')return{runtime:'ui-preview',model:'none',model_installed:true,deployment:'static-ui-preview',android_verified:false};
 if(path==='/curriculum')return F.curriculum; if(path==='/learning-plan')return plan; if(path==='/learner-memory')return{...memory,preferences:settings};
 if(path==='/learning-settings')return method==='POST'?(settings={guest_language:b.guest_language||settings.guest_language,style:b.style||settings.style,coach_language:b.coach_language||settings.coach_language,support:b.support||settings.support}):settings;
 if(path==='/lessons')return lessons; if(path==='/review-batches')return method==='POST'?reviewBatch(b):batches; if(path==='/batch-lessons')return batchLesson(b);
 if(path==='/practice-drafts'){const s=sessions[b.session_id],es=b.language==='es';return{id:uid(),text:(es?'Notas para el negocio (borrador de ejemplo):\n':'Business notes (sample draft):\n')+s.turns.map(t=>'• '+t.response).join('\n')};}
 if(path.startsWith('/drafts/'))return{ok:true};
 if(path==='/sessions'&&method==='POST')return newSession(b);
 if(m=path.match(/^\/sessions\/([^/]+)(?:\/(.*))?$/)){const s=sessions[m[1]];if(!s)throw 404;const a=m[2];
  if(!a)return s; if(a==='responses')return respond(s,b.response);
  if(a==='next'){s.question_index++;const f=followUps[s.guest_language]||followUps.en;s.guest_message=f[(s.question_index-1)%f.length];return s;}
  if(a==='finish')return finish(s); if(a==='assessment'){approve(s,b);return{ok:true};}
  if(a==='language'){s.guest_language=b.language;s.guest_message=s.question_index?(followUps[b.language]||followUps.en)[(s.question_index-1)%3]:guestText(s.scenario,b.language);return s;}
  if(a==='coach-language'){s.coach_language=b.language;return s;}}
 throw 404;}
const realFetch=window.fetch.bind(window);
window.fetch=async(url,opt={})=>{const u=new URL(url,location.href);if(u.origin!==location.origin||/\.(js|css|png|svg|ico)$/.test(u.pathname))return realFetch(url,opt);
 const method=opt.method||'GET',b=opt.body?JSON.parse(opt.body):undefined;
 await new Promise(r=>setTimeout(r,/responses|review-batches|next|finish|drafts/.test(u.pathname)?900:120));
 try{return new Response(JSON.stringify(route(method,u.pathname,b)),{status:200,headers:{'Content-Type':'application/json'}});}
 catch(e){return new Response(JSON.stringify({error:'Not available in this demo.'}),{status:404,headers:{'Content-Type':'application/json'}});}};
document.addEventListener('DOMContentLoaded',()=>{
 const samples=['La finca es preciosa y el café delicioso, pero al llegar nadie nos dijo dónde estacionar. Esperamos 20 minutos en la entrada.','Great tasting! It was hard to know the price before we arrived and we weren’t sure if we could pay by card.'];
 let tries=0;const fill=()=>{const f=()=>[...document.querySelectorAll('#reviewInputs textarea')];
  if((!f().length||typeof addReview!=='function')&&tries++<40)return setTimeout(fill,150);
  f().forEach((t,i)=>{if(!t.value&&samples[i])t.value=samples[i];});samples.slice(f().length).forEach(v=>addReview(v));};fill();
 const fmt=v=>Array.isArray(v)?v.join(', '):String(v);
 const sampleReply=()=>{const s=typeof session!=='undefined'&&session;if(!s)return '';const es=(s.guest_language||'en')==='es',k=(s.turns||[]).length;
  const facts=Object.entries((s.scenario&&s.scenario.facts)||{}).slice(0,2).map(([a,b])=>a.replace(/_/g,' ')+': '+fmt(b)).join('; ');
  const r=es?['¡Gracias por preguntar! Esto es lo que te puedo confirmar: '+facts+'. Si algo no está claro, lo verifico con el equipo y te escribo en 10 minutos.','Buena pregunta. No lo tengo confirmado todavía, así que lo verifico con el equipo y te confirmo en 10 minutos. Mientras tanto, te espero en la entrada principal.','¡Con gusto! Te mando la ubicación y el horario por mensaje para que lo tengas a mano. ¿Necesitas algo más?']
   :['Thanks for asking! Here is what I can confirm: '+facts+'. If anything is unclear, I will check with the team and message you within 10 minutes.','Good question. I don’t have that confirmed yet, so I’ll check with the team and get back to you in 10 minutes. Meanwhile, I’ll meet you at the main entrance.','Happy to help! I’ll send you the location and times by message so you have them handy. Anything else you need?'];
  return r[Math.min(k,2)];};
 let tries2=0;const addBtn=()=>{const box=document.getElementById('response');if(!box){if(tries2++<40)setTimeout(addBtn,150);return;}
  const b=document.createElement('button');b.type='button';b.className='btn';b.id='sampleReply';b.textContent='Use a sample reply';b.style.margin='8px 0';
  b.onclick=()=>{box.value=sampleReply();box.dispatchEvent(new Event('input',{bubbles:true}));box.focus();};box.insertAdjacentElement('afterend',b);};addBtn();
 const c=document.getElementById('connection');if(c)new MutationObserver(()=>{if(c.textContent!=='Coach ready')c.textContent='Coach ready';}).observe(c,{childList:true,characterData:true,subtree:true});});
})();
