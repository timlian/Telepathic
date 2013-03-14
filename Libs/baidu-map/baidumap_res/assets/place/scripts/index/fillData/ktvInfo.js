function ktvInfo(h,a){resetTemp.ktvInfo=resetTemp.ktvInfo||{};var k=h.price_table||{},r=[];window._ktvData=k;if(!(k&&k.length)){return}var s=new Date().getDay(),q=["周日","周一","周二","周三","周四","周五","周六"],m=q[s],b=new Array(),p=false,g=new Array(),o=false,n=new Array(),d=false;for(var f=0;f<k.length;f++){for(var e=0;e<b.length;e++){if(b[e]==k[f].price_day){p=true}}if(!p){b[b.length]=k[f].price_day}else{p=false}for(var e=0;e<g.length;e++){if(g[e]==k[f].price_box){o=true}}if(!o){g[g.length]=k[f].price_box}else{o=false}}r.push('<div class="ktv_menu">');r.push('    <div class="menu_btn">');r.push("        <div>");r.push('            <select id="ktv_select_1" onchange="updateKtvRoom();">');var c=false;for(var f=0;f<b.length;f++){if(b[f]==m){r.push('        <option value="'+m+'" selected="selected">'+m+"</option>");c=true}else{r.push('        <option value="'+b[f]+'">'+b[f]+"</option>")}}r.push("            </select>");r.push("        </div>");r.push('        <div class="ktv_block"></div>');r.push("    </div>");r.push('    <div class="menu_btn">');r.push('        <select id="ktv_select_2" onchange="updateKtvDay();">');for(var f=0;f<g.length;f++){r.push('        <option value="'+g[f]+'">'+g[f]+"</option>")}var l=[];for(var f=0;f<k.length;f++){if(c){if(k[f].price_day==m&&k[f].price_box==g[0]){l[l.length]=k[f];for(var e=0;e<n.length;e++){if(n[e]==k[f].price_unit){d=true}}if(!d){n[n.length]=k[f].price_unit}else{d=false}}}else{if(k[f].price_day==b[0]&&k[f].price_box==g[0]){l[l.length]=k[f];for(var e=0;e<n.length;e++){if(n[e]==k[f].price_unit){d=true}}if(!d){n[n.length]=k[f].price_unit}else{d=false}}}}r.push("        </select>");r.push("    </div>");r.push("</div>");r.push('<div class="content" id="ktv_price_content">');for(var e=0;e<n.length;e++){if(e){r.push('<div class="dushed_line"><div class="lack_ball left"></div><div class="lack_ball right"></div><div class="overlay_div overlay_left"></div><div class="overlay_div overlay_right"></div></div>')}r.push('<div class="content_info">');r.push('    <div class="price_per">计价单位:<span>'+n[e]+"<span></div>");r.push('    <ul class="price_table">');r.push('        <div><li class="ktv_list">时段</li></div>');r.push('        <div><li class="ktv_list">价格</li></div>');for(var f=0;f<l.length;f++){if(l[f].price_num&&l[f].price_unit==n[e]){r.push('        <div><li class="ktv_list" id="ktv_time_1_'+f+'">'+l[f].price_time+"</li></div>");r.push('        <div><li class="ktv_list" id="ktv_price_1_'+f+'">'+l[f].price_num+"</li></div>")}else{if(l[f].price_unit==n[e]){r.push('        <div><li class="ktv_list" id="ktv_time_1_'+f+'">'+l[f].price_time+"</li></div>");r.push('        <div><li class="ktv_list" id="ktv_price_1_'+f+'">-</li></div>')}}}r.push("    </ul>");r.push("</div>")}r.push('<div class="content_ahead_clear"></div>');r.push("</div>");if(1){T.g("ktvInfo").innerHTML=r.join("");T.g("ktvCon").style.display="";resetTemp.ktvInfo.haveKTV=true}}function updateKtvRoom(){var a=document.getElementById("ktv_select_1").value,g=document.getElementById("ktv_select_2").value;var d=[],e=false;for(var c=0;c<window._ktvData.length;c++){if(window._ktvData[c].price_day==a){for(var b=0;b<d.length;b++){if(d[b]==window._ktvData[c].price_box){e=true}}if(!e){d[d.length]=window._ktvData[c].price_box}else{e=false}}}var h=document.getElementById("ktv_select_2"),f=[];for(var c=0;c<d.length;c++){if(d[c]==g){f.push('<option value="'+d[c]+'" selected="selected">'+d[c]+"</option>")}else{f.push('<option value="'+d[c]+'">'+d[c]+"</option>")}}h.innerHTML=f.join("");addStat(STAT_INDEX_CLICK_KTV_DAY);updateKtvPrice()}function updateKtvDay(){var g=document.getElementById("ktv_select_2").value,c=document.getElementById("ktv_select_1").value;var b=[],a=false;for(var e=0;e<window._ktvData.length;e++){if(window._ktvData[e].price_box==g){for(var d=0;d<b.length;d++){if(b[d]==window._ktvData[e].price_day){a=true}}if(!a){b[b.length]=window._ktvData[e].price_day}else{a=false}}}var h=document.getElementById("ktv_select_1"),f=[];for(var e=0;e<b.length;e++){if(b[e]==c){f.push('<option value="'+b[e]+'" selected="selected">'+b[e]+"</option>")}else{f.push('<option value="'+b[e]+'">'+b[e]+"</option>")}}h.innerHTML=f.join("");addStat(STAT_INDEX_CLICK_KTV_BOX);updateKtvPrice()}function updateKtvPrice(){var h=document.getElementById("ktv_select_2").value,b=document.getElementById("ktv_select_1").value,g=[],f=[],a=new Array(),d=false;for(var e=0;e<window._ktvData.length;e++){if(window._ktvData[e].price_day==b&&window._ktvData[e].price_box==h){f[f.length]=window._ktvData[e];for(var c=0;c<a.length;c++){if(a[c]==window._ktvData[e].price_unit){d=true}}if(!d){a[a.length]=window._ktvData[e].price_unit}else{d=false}}}for(var c=0;c<a.length;c++){if(c){g.push('<div class="dushed_line"><div class="lack_ball left"></div><div class="lack_ball right"></div><div class="overlay_div overlay_left"></div><div class="overlay_div overlay_right"></div></div>')}g.push('<div class="content_info">');g.push('    <div class="price_per">计价单位:<span>'+a[c]+"<span></div>");g.push('    <ul class="price_table">');g.push('        <div><li class="ktv_list">时段</li></div>');g.push('        <div><li class="ktv_list">价格</li></div>');for(var e=0;e<f.length;e++){if(f[e].price_num&&f[e].price_unit==a[c]){g.push('        <div><li class="ktv_list" id="ktv_time_1_'+e+'">'+f[e].price_time+"</li></div>");g.push('        <div><li class="ktv_list" id="ktv_price_1_'+e+'">'+f[e].price_num+"</li></div>")}else{if(f[e].price_unit==a[c]){g.push('        <div><li class="ktv_list" id="ktv_time_1_'+e+'">'+f[e].price_time+"</li></div>");g.push('        <div><li class="ktv_list" id="ktv_price_1_'+e+'">-</li></div>')}}}g.push("    </ul>");g.push("</div>")}document.getElementById("ktv_price_content").innerHTML=g.join("")};