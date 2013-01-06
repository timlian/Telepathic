package com.telepathic.finder.sdk.network;

import java.util.ArrayList;

import org.ksoap2.serialization.SoapObject;

import com.telepathic.finder.sdk.BusLineListener;
import com.telepathic.finder.sdk.BusLineRoute;

public class BusLineRouteRequest extends RPCRequest {

    private static final String METHOD_NAME = "getBusLineRoute";

    private static final String RESPONSE_NAME = "getBusLineRouteResult";

    private BusLineListener mListener;

    public BusLineRouteRequest(String line, BusLineListener listener) {
        super(METHOD_NAME);
        addParameter("busLine", line);
        mListener = listener;
    }
 
	@Override
	protected String getResponseName() {
		return RESPONSE_NAME;
	}

	@Override
	protected void handleError(String errorMessage) {
		if (mListener != null) {
			mListener.onError(errorMessage);
		}
	}

	/*
     * Line route response data entry example:
     *
     * {lineName=111; departureTime=06:00; closeOffTime=22:00; type=����; stations=����վ�ۺϽ�ͨ��Ŧվ,ʢ��һ·��վ,�������վ,ͩ����С��վ,����·վ,�Ͼ���·վ,�Ͼ���·վ,�Ͼ���·��վ,�Ͼ���·��ҵ·��վ,��ҵ·����·����վ,������������վ,����·���Ķ�վ,����¥վ,����·��һ����վ,�������վ,����·����·��վ,��ˮ��վ,����·��һ��վ,����·�⻪�����վ,�³���·��վ,����·��վ,����·��վ,��������վ,��������վ,����С��վ,��·ͬ��·��վ,��·վ,��·��վ,��·��Ϭ�����Ŷ�վ,����·վ,�߼Ҵ�վ,���Ŵ�վ,�߼�С��վ; stationAliases= , , , , , , , , , , , , , , , , , , , , , , , , , , , , , , , , ; code=200; msg=�ɹ�; }
     *
     */
	@Override
	protected void handleResponse(SoapObject newDataSet) {
		SoapObject dataEntry = null;
		ArrayList<BusLineRoute> busLine = new ArrayList<BusLineRoute>();
		for (int i = 0; i < newDataSet.getPropertyCount(); i++) {
			dataEntry = (SoapObject) newDataSet.getProperty(i);
			busLine.add(new BusLineRoute(dataEntry));
		}
		if (mListener != null) {
			mListener.onSuccess(busLine);
		}
	}

}
