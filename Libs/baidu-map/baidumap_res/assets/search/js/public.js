(function(){window.mobileType="android"})();function scriptRequest(b,d,e){var c=/msie/i.test(window.navigator.userAgent);if(c&&document.getElementById("_script_"+e)){var a=document.getElementById("_script_"+e)}else{if(document.getElementById("_script_"+e)){document.getElementById("_script_"+e).parentNode.removeChild(document.getElementById("_script_"+e))}var a=document.createElement("script");a.charset="utf-8";a.setAttribute("type","text/javascript");document.body.appendChild(a)}a.setAttribute("src",b,"_bd_place_js_")}function callAppFun(c,e){return;var e=e||{};var h={relayout:{comm:"relayout",param:"height="+document.body.clientHeight},refresh:{comm:"refresh"}};if(!h[c]){return}var d=h[c],f=d.agreement||"bdapi://",g=d.param||"",a=f+d.comm;for(var b in e){g+=b+"="+e[b]+"&"}g=g.replace(/&$/,"");if(g.length>0){a+="?"+g}window.location.href=a}var config={base:{restaurant:"餐馆",hotel:"酒店",bus:"公交站",gas:"加油站",bank:"银行",atm:"ATM",movie:"电影院",train:"火车站",market:"商场",internet:"网吧",ktv:"KTV",kfc:"肯德基",mcDonald:"麦当劳",icbc:"工商银行",ccb:"建设银行",supermarket:"超市",cmb:"招商银行",boc:"中国银行",bath:"洗浴",abc:"农业银行",cafe:"咖啡厅",viewpoint:"景点",hospital:"医院",more:"更多分类",food:"美食",snack:"小吃快餐",lnn:"快捷酒店",starhotel:"星级酒店",waimai:"外卖",groupon:"团购",subway:"地铁图"},android1_beijing:["food","snack","lnn","starhotel","bus","waimai","bank","ktv","groupon","subway"],android2_beijing:["supermarket","atm","movie","internet","bath","market","gas","icbc","ccb","more"],android1_sgs:["food","snack","lnn","starhotel","bus","bank","supermarket","ktv","groupon","subway"],android2_sgs:["atm","movie","internet","bath","market","gas","icbc","ccb","cmb","more"],android1_hongkong:["food","snack","lnn","starhotel","bus","bank","supermarket","ktv","atm","subway"],android2_hongkong:["movie","internet","bath","market","gas","kfc","icbc","ccb","cmb","more"],android1_aomen:["food","snack","lnn","starhotel","bus","bank","supermarket","ktv","atm","movie"],android2_aomen:["internet","bath","market","gas","kfc","mcDonald","icbc","ccb","cmb","more"],android1_othercity:["food","snack","lnn","starhotel","bus","bank","supermarket","ktv","groupon","atm"],android2_othercity:["movie","internet","bath","market","gas","kfc","icbc","ccb","cmb","more"],android1_un_location:["food","snack","lnn","starhotel","bus","bank","supermarket","ktv","groupon","subway"],android2_un_location:["atm","movie","internet","bath","market","gas","kfc","icbc","ccb","more"]};function makeList(e,d){var h=[];var c=config[mobileType+e+"_"+d];h.push('<ul class="list">');for(var f=0;f<c.length;f++){var b="";var a=c[f];var g="";if(a=="more"){g=config.base[a];b="bdapi://moresearch"}else{if(a=="waimai"){g=config.base[a];b="http://waimai.baidu.com/?type=search"}else{g=config.base[a];b="bdapi://search?keyword="+g}}h.push('<li><a  class="'+a+'" href="'+b+'">'+g+"</a></li>")}h.push("</ul>");document.getElementById("content").innerHTML=h.join("");addEvent();setTimeout(function(){scriptRequest("http://client.map.baidu.com/search/v3/js/version.js?t="+new Date().getTime())},200)}var temp={};function addEvent(){var a=document.getElementById("content").getElementsByTagName("a");for(var b=0;b<a.length;b++){a[b].ontouchstart=function(){clearHover();this.parentNode.className="hover";temp.curTag=this}}}function getQueryStringArgs(){var b=(location.search.length>0?location.search.substring(1):""),e={},c=b.length?b.split("&"):[],g=null,d=null,h=null,f=0,a=c.length;for(f=0;f<a;f++){g=c[f].split("=");d=decodeURIComponent(g[0]);h=decodeURIComponent(g[1]);if(d.length){e[d]=h}}return e}function setInfo(a){var b=JSON.parse(a);window.args.cityid=b.cityid||"0";showPage(window.args.pageid)}function showPage(a){var c=args.cityid;var b={"131":"beijing","289":"shanghai","257":"guangzhou","340":"shenzhen","2912":"hongkong","2911":"aomen","2915":"aomen","2916":"aomen","2917":"aomen","2918":"aomen"};if(b[c]=="beijing"){makeList(a,"beijing")}else{if(b[c]=="shanghai"||b[c]=="guangzhou"||b[c]=="shenzhen"){makeList(a,"sgs")}else{if(b[c]=="hongkong"){makeList(a,"hongkong")}else{if(b[c]=="aomen"){makeList(a,"aomen")}else{if(c=="0"){makeList(a,"un_location")}else{makeList(a,"othercity")}}}}}}document.ontouchend=clearHover;document.ontouchmove=function(b){var a=b.target;if(a==temp.curTag){return}clearHover()};document.onscroll=clearHover;document.onclick=clearHover;function clearHover(){var a=document.getElementById("content").getElementsByTagName("li");for(var b=0;b<a.length;b++){a[b].className=""}temp.curTag=null};