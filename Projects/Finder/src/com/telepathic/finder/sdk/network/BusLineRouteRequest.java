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
     * {lineName=111; departureTime=06:00; closeOffTime=22:00; type=����; stations=����վ�ۺϽ�ͨ��Ŧվ,ʢ��һ·��վ,�������վ,ͩ����С��վ,����·վ,�Ͼ���·վ,�Ͼ���·վ,�Ͼ���·��վ,�Ͼ���·��ҵ·��վ,��ҵ·����·����վ,������������վ,����·���Ķ�վ,����¥վ,����·��һ����վ,�������վ,����·����·��վ,��ˮ��վ,����·��һ��վ,����·�⻪�����վ,�³���·��վ,����·��վ,����·��վ,��������վ,��������վ,����С��վ,��·ͬ��·��վ,��·վ,��·��վ,����·��Ϭ�����Ŷ�վ,����·վ,�߼Ҵ�վ,���Ŵ�վ,�߼�С��վ; stationAliases= , , , , , , , , , , , , , , , , , , , , , , , , , , , , , , , , ; code=200; msg=�ɹ�; }
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
