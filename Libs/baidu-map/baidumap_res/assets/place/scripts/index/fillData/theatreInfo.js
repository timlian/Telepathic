function theatreInfo(m,a){resetTemp.theatreInfo=resetTemp.theatreInfo||{};var p=[],k={},q=getPicSideWid();var f="",n=0,e=[],c=[],b=l&&(l.length*76);p.push('<div class="h3">演出信息</div>');p.push('<div class="box_3" id="theatre_info_con" '+f+">");var l=m&&m.theatre_info&&m.theatre_info.time_table;b=l&&(l.length*76);if(!(l&&l.length)){p.push('<p class="theatre_nothing">暂无上映信息</p>');p.push("</div>");p.push('<div class="splitLine2"></div>');T.g("theatreCon").innerHTML=p.join("");T.g("theatreCon").style.display="";return}e.push('    <div id="wrapper_theatre" class="theatre_wrapper">');e.push('    <div id="scroller_theatre" class="theatre_pic_list_con" style="width:'+(b+q*2)+'px">');e.push('       <ul class="theatre_pic_list" id="theatre_pic_list">');c.push('    <div class="theatre_info_list_con" id="theatre_info_list_con">');theatreInfo.theatre_num=l.length;for(var i=0;i<l.length;i++){var o=l[i],h=' style="display:none"',d="";if(n==0){h="";d=' style="margin-left:'+q+'px"'}else{if(n==l.length){d=' style="margin-right:'+q+'px"'}}e.push("            <li"+d+'><img id="theatre_pic_list_'+n+'" src="'+o.opera_piture+'" /></li>');c.push("<div"+h+' id="theatre_info_list_'+n+'">');c.push('<ul class="theatre_info_list">');if(o.opera_name.length>31){c.push('<li class="theatre_title">'+o.opera_name.slice(0,30)+"...</li>")}else{c.push('<li class="theatre_title">'+o.opera_name+"</li>")}if(o.sub_head){c.push('<li class="theatre_sub_head">'+o.sub_head+"</li>")}if(o.show_time){c.push('<li class="theatre_show_time"><span>演出时间 : </span>'+o.show_time+"</li>")}else{c.push('<li class="theatre_show_time"><span>演出时间 : </span>暂无</li>')}c.push('<li class="theatre_price"><span>票价 :</span>￥'+o.min_price+"<span> 起</span></li>");c.push("</ul>");var g=o.order_url;if(g.length>0){c.push('<div class="theatre_tick_btn"><span ><a href="'+g[0].url+'" onclick="addStat(\''+STAT_INDEX_THEATRE_DAMAI+'\')" ><em class="damai_icon"></em>去大麦网购票<em class="goto_icon_1"></em></a></span></div>')}c.push("</div>");n++}e.push("        </ul>");e.push("    </div>");e.push("    </div>");c.push("        </ul>");c.push('<span class="cur_theatre_point"></span>');c.push("    </div>");p.push(e.join(""));p.push(c.join(""));p.push("</div>");p.push('<div class="splitLine2"></div>');T.g("theatreCon").innerHTML=p.join("");T.g("theatreCon").style.display="";resetTemp.theatreInfo.haveTheatre=true;addTheatreAnimation()}showTheatreInfo.prePage=100000;function addTheatreAnimation(){var e=T.g("wrapper_theatre")||"",d=T.g("theatre_pic_list")||"";if(!(e&&d)){return}var a=d.getElementsByTagName("li"),c=theatreInfo.theatre_num>1?1:theatreInfo.theatre_num-1;window.theatreScroll=new iScroll(wrapper_theatre,{snap:true,momentum:false,hScrollbar:false,scrollPart:false,onBeforeScrollStart:null,x:-(c*76),currPageX:c,wrapperW:76,noScrollWid:getPicSideWid()+100,onScrollEnd:function(){if(showTheatreInfo.prePage!=this.currPageX){addStat(STAT_INDEX_THEATRE_POSTER);showTheatreInfo.prePage=this.currPageX}showTheatreInfo(this.currPageX)}});showTheatreInfo(c);for(var b=0;b<a.length;b++){(function(){var f=b;a[f].onmouseup=function(){window.theatreScroll.scrollToPage(f)}})()}}function showTheatreInfo(b){var d=T.g("theatre_info_list_"+b),a=T.g("theatre_pic_list_"+b);if(!(d&&a)){return}for(var c=0;c<theatreInfo.theatre_num;c++){T.g("theatre_info_list_"+c).style.display="none"}d.style.display=""};