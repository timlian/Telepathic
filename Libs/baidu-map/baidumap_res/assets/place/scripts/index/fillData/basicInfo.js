function basicInfo(q,t){resetTemp.basicInfo=resetTemp.basicInfo||{};var G=q.detail_info,l=q.src_name||(window.poiInfo&&window.poiInfo.src_name),t=t||{},e=t.type||{};var D=window.poiInfo,x=D.name||q&&q.name||"",J=D.addr||q&&q.addr||"",o=D.tel||q&&q.phone||"",k=D.comment_num||G&&G.comment_num||"",n=G&&G.overall_rating||(D&&D.overall_rating),j=G&&G.price||(D&&D.price),j=j?"&#165;"+j:"暂无",b=[],g=n||0,a="../img/loading.png",s=dataConfig[l]||dataConfig.defaul_t,d="",E=0,C=" tel_style",c="",v=T.g("telNumCon"),A=v.parentNode;if(G&&G.image){var m=window.PlaceDataMgr.getPlaceData(window.poiInfo.uid+"_img");if(m||G.image.indexOf("fantong")>-1){a=m||G.image;d=" have_pic";_return="";T.g("baseInfoPic").style.backgroundImage='url("'+a+'")'}else{getImgBase64({url:G.image,width:640,height:320,align:"center",quality:50})}}else{if(e!="emptyData"){var r=(D&&D.src_name);if(r&&bannerType[r]){T.g("baseInfoPic").style.backgroundImage='url("../img/'+r+'_banner.png")'}}}b.push('        <li class="name">'+x+"</li>");if(J){b.push('        <li class="address font_1">'+J+"</li>")}var p=Math.round((g*1)*(60/5)+parseInt(g*1)*2);p=isNaN(p)?0:p;b.push('        <li class="st1"><span class="star"><b style="width:'+p+'px"></b></span><span class="num">'+g+"</span>");if(k){b.push('<span class="num comm_num">'+k+"条评论</span>")}b.push("</li>");if(s&&s.base_info&&s.base_info.price){b.push('        <li class="st3">'+s.base_info.price.name+': <font class="red">'+j+"</font></li>")}if(G&&s&&s.base_key&&s.base_key.length&&s.base_info){for(var B=0;B<s.base_key.length;B++){var I=s.base_key[B],f="st2",h=G[I]||"-";if(I=="price"){continue}if(B==(s.base_key.length-1)){f="st4"}if(s.base_info[I]){b.push('        <li class="'+f+'">'+s.base_info[I].name+':<font class="color_2">'+h+"</font></li>");E++}}}if(T.g("basicInfo")){if(!E){T.addClass(T.g("basicInfo"),"noMoreStyle")}else{T.removeClass(T.g("basicInfo"),"noMoreStyle")}}T.g("basicInfo").innerHTML=b.join("");if(G&&G.ota_info&&G.ota_info[0]){resetTemp.basicInfo.haveOta=1;var F=G.ota_info[0],y=F.ota_phone||"",z=F.ota_tips||"",u=F.en_name||"";if(y&&z&&u){T.g("telNumYlCon").style.backgroundImage='url("http://map.baidu.com/fwmap/upload/place/icon/'+u+'/50.png")';T.g("telNumYlCon").innerHTML="<a href=\"javascript:void(0)\" onclick=\"addStat(STAT_INDEX_OTA_CLICK);callAppFun('tel',{tel:'"+y+'\'})" id="telNumYlCon" class="'+C+'">订房热线　 '+y+"</a>";T.g("telNumYlTipCon").innerHTML=z;T.g("telYlCon").style.display="";T.removeClass(A,C);C="";c="酒店电话　 "}}if(o){var H=o.split(","),y="";for(var B=0;B<H.length;B++){y+="<span telNum="+H[B]+">"+H[B]+"</span>,"}y=y.replace(/,$/,"");T.addClass(A,C);v.innerHTML=c+y;T.g("telCon").style.display=""}};