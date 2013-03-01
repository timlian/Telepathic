package com.telepathic.finder.app;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.OverlayItem;


public class CustomItemizedOverlay extends ItemizedOverlay<OverlayItem> {

    private ArrayList<OverlayItem> overlayItemList = new ArrayList<OverlayItem>();
    private Context context;

    public CustomItemizedOverlay(Drawable defaultMarker) {
        super(defaultMarker);
    }

    public CustomItemizedOverlay(Drawable marker, Context context) {
        super(marker);
        this.context = context;
    }

    @Override
    protected OverlayItem createItem(int i) {
        return overlayItemList.get(i);
    }

    @Override
    public int size() {
        return overlayItemList.size();
    }

    public void addOverlay(OverlayItem overlayItem) {
        overlayItemList.add(overlayItem);
        this.populate();
    }

    public void removeAllOverlay() {
        overlayItemList.removeAll(overlayItemList);
        this.populate();
    }

    @Override
    // 处理点击事件
    protected boolean onTap(int i) {
        Toast.makeText(this.context, overlayItemList.get(i).getSnippet(), Toast.LENGTH_SHORT).show();
        return true;
    }
}
