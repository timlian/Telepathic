package com.telepathic.finder.sdk.network;

import java.util.ArrayList;

import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;

import com.telepathic.finder.sdk.BusLineListener;
import com.telepathic.finder.sdk.BusLineRoute;

public class BusLineRouteRequest extends RPCRequest {

    private static final String METHOD_NAME = "getBusLineRoute";

    private static final String KEY_RESPONSE = "getBusLineRouteResult";

    private BusLineListener mListener;

    public BusLineRouteRequest(String line, BusLineListener listener) {
        super(METHOD_NAME);
        addParameter("busLine", line);
        mListener = listener;
    }

    @Override
    public void onResponse(Object result, String errorMessage) {
        if (errorMessage != null) {
            if (mListener != null) {
                mListener.onError(errorMessage);
            }
            return ;
        }
        if (result instanceof SoapObject) {
            final SoapObject response = (SoapObject)((SoapObject)result).getProperty(KEY_RESPONSE);
            process(response);
        } else if (result instanceof SoapFault) {

        } else {
            throw new RuntimeException("Unknown Exception!!!");
        }
    }

    /*
     * Line route response data entry example:
     *
     * {lineName=111; departureTime=06:00; closeOffTime=22:00; type=上行; stations=火车南站综合交通枢纽站,盛和一路西站,新南天地站,桐梓林小区站,紫竹北路站,紫荆北路站,紫荆西路站,紫荆西路西站,紫荆西路创业路口站,创业路二环路口南站,永丰立交桥西站,二环路南四段站,红牌楼站,二环路西一段南站,武侯大道口站,二环路少陵路口站,清水河站,二环路西一段站,二环路光华大道口站,新成温路口站,锦西路东站,锦西路西站,青羊大道中站,青羊大道北站,黄忠小区站,蜀汉路同和路口站,蜀汉路站,蜀汉路西站,三环路羊犀立交桥东站,蜀汉西路站,高家村站,土桥村站,高家小区站; stationAliases= , , , , , , , , , , , , , , , , , , , , , , , , , , , , , , , , ; code=200; msg=成功; }
     *
     */
    private void process(SoapObject response) {
        if (response != null) {
            final SoapObject diffGram = (SoapObject) response.getProperty(KEY_DIFF_GRAM);
            if (diffGram != null) {
                final SoapObject newDataSet = (SoapObject) diffGram.getProperty(KEY_NEW_DATA_SET);
                if (newDataSet != null) {
                    final SoapObject firstDataEntry = (SoapObject) newDataSet.getProperty(0);
                    final String errorCode = firstDataEntry.getPrimitivePropertyAsString(KEY_ERROR_CODE);
                    final String errorMessage = firstDataEntry.getPrimitivePropertyAsString(KEY_ERROR_MESSAGE);
                    if (NO_ERROR == Integer.parseInt(errorCode)) {
                        SoapObject dataEntry = null;
                        ArrayList<BusLineRoute> busLine = new ArrayList<BusLineRoute>();
                        for(int i = 0; i < newDataSet.getPropertyCount(); i++) {
                            dataEntry = (SoapObject)newDataSet.getProperty(i);
                            busLine.add(new BusLineRoute(dataEntry));
                        }
                        if (mListener != null) {
                            mListener.onSuccess(busLine);
                        }
                    } else {
                        if (mListener != null) {
                            mListener.onError(errorMessage);
                        }
                    }
                }
            }
        }
    }

}
