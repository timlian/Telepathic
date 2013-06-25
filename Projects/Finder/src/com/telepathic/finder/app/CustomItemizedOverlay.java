package com.telepathic.finder.app;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayItem;


public class CustomItemizedOverlay extends ItemizedOverlay<OverlayItem> {
    private ArrayList<OverlayItem> overlayItemList = new ArrayList<OverlayItem>();
    private Context context;
    private MapView mMapView;

    public CustomItemizedOverlay(Drawable defaultMarker, MapView mapView) {
        super(defaultMarker, mapView);
        mMapView = mapView;
    }

    public CustomItemizedOverlay(Drawable marker, Context context, MapView mapView) {
        super(marker, mapView);
        this.context = context;
        mMapView = mapView;
    }

    @Override
    public int size() {
        return overlayItemList.size();
    }

    public void addOverlay(OverlayItem overlayItem) {
        overlayItemList.add(overlayItem);
        this.addItem(overlayItem);
    }

    public void removeAllOverlay() {
        overlayItemList.removeAll(overlayItemList);
        this.removeAll();
    }

    @Override
    // 处理点击事件
    protected boolean onTap(int i) {
        Toast.makeText(this.context, overlayItemList.get(i).getSnippet(), Toast.LENGTH_SHORT).show();
        return true;
    }
}
