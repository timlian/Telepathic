function picInfo(g,a){resetTemp.picInfo=resetTemp.picInfo||{};var h=g.image||{},c=0,e=g.src_name,a=a||{},j=a.type||{},k=[],d=dataConfig[e]||dataConfig.defaul_t;for(var f=0;f<4;f++){var b=g.image[f];if(b&&b.imgUrl&&b.cn_name&&b.name){if(c<4){k.push("<li><a onclick=\"addStat(STAT_INDEX_CLICK_PIC_LIST);callAppFun('newwindow',{page:'pic.html',index:"+c+",imgUrl:'"+b.imgUrl+"',name:'"+b.name+"',cn_name:'"+b.cn_name+'\'})"><img src="http://map.baidu.com/maps/services/thumbnails?src='+encodeURIComponent(b.imgUrl)+'&quality=50&width=128&height=128&align=center" width="64" height="64" /></a></li>')}c++}else{k.push('<li><a onclick="callAppFun(\'openModule\',{\'name\':\'sbPhoto\'})"><img src="../img/add_img_bj.png" width="64" height="64" /></a></li>')}if(c>4){break}}k.push('<li class="bottom_nav"><div><span><a onclick="addStat(STAT_INDEX_CLICK_PHOTO);callAppFun(\'openModule\',{\'name\':\'sbPhoto\'})"><em class="photo"></em>我要上传</a></span><span><a href="javascript:void(0)" onclick="addStat(STAT_INDEX_CLICK_PIC_ALL);callAppFun(\'newwindow\',{page:\'picList.html\'})">查看更多图片<em class="goto_icon_1"></em></a></span></div></li>');if(c){resetTemp.picInfo.havePic=1;T.g("picInfo").innerHTML=k.join("");T.g("picCon").style.display=""}else{T.g("noPicCon").style.display=""}};