package com.telepathic.finder.app;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.baidu.mapapi.ItemizedOverlay;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.OverlayItem;
import com.baidu.mapapi.Projection;

public class CustomItemizedOverlay extends ItemizedOverlay<OverlayItem> {  
    
    private ArrayList<OverlayItem> overlayItemList = new ArrayList<OverlayItem>();  
    private Context context;  
  
    public CustomItemizedOverlay(Drawable defaultMarker) {  
        super(boundCenterBottom(defaultMarker));  
    }  
  
    public CustomItemizedOverlay(Drawable marker, Context context) {  
        super(boundCenterBottom(marker));  
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
  
    @Override  
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {  
        super.draw(canvas, mapView, shadow);  
        // Projection�ӿ�������Ļ���ص�����ϵͳ�͵�����澭γ�ȵ�����ϵͳ֮��ı任   
        Projection projection = mapView.getProjection();  
        // �������е�OverlayItem   
        for (int index = this.size() - 1; index >= 0; index--) {  
            // �õ�����������item   
            OverlayItem overLayItem = getItem(index);  
  
            // �Ѿ�γ�ȱ任�������MapView���Ͻǵ���Ļ��������   
            Point point = projection.toPixels(overLayItem.getPoint(), null);  
  
            Paint paintText = new Paint();  
            paintText.setColor(Color.RED);  
            paintText.setTextSize(13);  
            // �����ı�   
            canvas.drawText(overLayItem.getTitle(), point.x + 10, point.y - 15, paintText);  
        }  
    }  
  
    @Override  
    // �������¼�   
    protected boolean onTap(int i) {  
        setFocus(overlayItemList.get(i));  
        Toast.makeText(this.context, overlayItemList.get(i).getSnippet(), Toast.LENGTH_SHORT).show();  
        return true;  
    }  
}  