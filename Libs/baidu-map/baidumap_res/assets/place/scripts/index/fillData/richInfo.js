function richInfo(j,m){var v=j.rich_info||{},n=0,b=false,g=j.src_name,m=m||{},c=m.type||{},a=[],u=150,l=dataConfig[g]||dataConfig.defaul_t;if(l&&l.more_key&&l.more_key.length){for(var r=0;r<l.more_key.length;r++){if(v[l.more_key[r]]){b=true;break}}}if(l&&l.rich_key&&l.rich_key.length&&l.rich_info){if(v.reservation&&v.reservation.length){var h=[],f=false;for(var r=0;r<v.reservation.length;r++){var k=v.reservation[r],d=k.discount,q="http://map.baidu.com/fwmap/upload/place/icon/"+k.src+"/50.png",p=k.url;if(p){h.push('<li class="color_1 shop_icon" style="background:url(\''+q+'\') no-repeat 13px 10px;background-size:18px 18px;"><a href="'+p+'" onclick="">在线预订');if(d){h.push(" : <span>"+d*10+"</span>折")}h.push("</a></li>");f=true}}if(f){T.g("bookOnlineCon").innerHTML=h.join("");T.g("bookOnline").style.display=""}}for(var r=0;r<l.rich_key.length;r++){if(!l.rich_info[l.rich_key[r]]){continue}var w=l.rich_key[r],o=l.rich_info[w].className||"",t=" mt-10";if(n==0){t=""}if(v[w]&&l.rich_info[w]&&l.rich_info[w].name){var s=v[w],e=s.length>u?u:s.length;if(s.length>u){s=s.slice(0,e)+"..."}a.push('    <li class="h3'+t+'">'+l.rich_info[w].name+"</li>");a.push('    <li class="box_1 '+o+'"><p class="pd-5">'+s+"</p>");a.push();a.push("</li>");n++}}}if(c!="emptyData"&&!n){T.g("richCon").style.display="none";return}if(b){a[a.length-1]='<div class="bottom_nav bottom_nav_1"><div><span><a href="javascript:void(0)" onclick="addStat(\''+STAT_INDEX_CLICK_RICH+"');callAppFun('newwindow',{page:'more.html'})\">查看更多简介<em class=\"goto_icon_1\"></em></a></span></div></div>"}T.g("richInfo").innerHTML=a.join("");T.g("richCon").style.display=""}function showRecommend(b,a){return;callAppFun("newwindow",{page:"recommend.html"})};