function showOilPrices(d,a){resetTemp.showOilPrices=resetTemp.showOilPrices||{};var e=d.detail_info.oril_info.oril_detail,h=d.detail_info,f=[],j=[],b="",k="<li><div></div></li>";resetTemp.showOilPrices.oril_price=1;T.g("oril_info").style.display="block";for(var c=0,g=e.length;c<g;c++){switch(e[c].oril_type){case"gasoline_89":b="89#";break;case"gasoline_90":b="90#";break;case"gasoline_92":b="92#";break;case"gasoline_93":b="93#";break;case"gasoline_95":b="95#";break;case"gasoline_97":b="97#";break;case"gasoline_98":b="98#";break;case"derv_0":b="0#柴";break;case"derv_negative10":b="负10#柴";break;case"derv_negative20":b="负20#柴";break;default:b=""}f.push('<li><div><span class="oril_type"><em>'+b+'油</em></span><span class="oril_price"><em>￥'+e[c].oril_price+"</em></span></div></li>")}if((e.length)%2!=0){f.push(k)}T.g("oril_info_list").innerHTML=f.join("")};