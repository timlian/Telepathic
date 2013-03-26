package com.telepathic.finder.sdk.traffic.request;

import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;

import com.telepathic.finder.sdk.IErrorCode;

public abstract class RPCBaseRequest {

    private static final String NAMESPACE = "http://tempuri.org/";

    private static final String KEY_DIFF_GRAM = "diffgram";
    private static final String KEY_NEW_DATA_SET = "NewDataSet";
    private static final String KEY_DOCUMENT_ELEMENT = "DocumentElement";
    private static final String KEY_ERROR_CODE = "code";
    private static final String KEY_ERROR_MESSAGE = "msg";

    private static long COUNT = 0;
    private static final long mRequestId = COUNT++;

    private static final int NO_ERROR = 200;

    private String mRpcMethodName;
    private SoapObject mRpc;

    protected RequestCallback mCallback;

    public RPCBaseRequest(String name) {
        mRpcMethodName = name;
        mRpc = new SoapObject(NAMESPACE, mRpcMethodName);
    }

    abstract void handleError(int errorCode, String errorMessage);

    abstract void handleResponse(SoapObject dataSet);

    public void setCallback(RequestCallback callback) {
        mCallback = callback;
    }

    public long getId() {
        return mRequestId;
    }

    public String getName() {
        return mRpcMethodName;
    }

    void addParameter(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("The parameter key is null.");
        }
        mRpc.addProperty(key, value);
    }

    protected void setParameter(String key, Object value) {
        Object property = mRpc.getProperty(key);
        if (property != null) {
            for(int idx = 0; idx < mRpc.getPropertyCount(); idx++) {
                if (property.equals(mRpc.getProperty(idx))) {
                    mRpc.setProperty(idx, value);
                    break;
                }
            }
        } else {
            mRpc.addProperty(key, value);
        }
    }

    public SoapObject getSoapMessage() {
        return mRpc;
    }

    public String getSoapAction() {
        return mRpc.getNamespace() + mRpc.getName();
    }

    public String getMethodName() {
        return mRpc.getName();
    }

    protected boolean isComplete() {
        return true;
    }

    public void onResponse(Object result) {
        if (result instanceof SoapObject) {
            process((SoapObject)result);
        } else if (result instanceof SoapFault) {

        } else {
            throw new RuntimeException("Unknown Exception!!!");
        }
    }

    private void process(SoapObject result) {
        final SoapObject response = (SoapObject) result.getProperty(getRpcResultTag());
        if (response != null) {
            final SoapObject diffGram = (SoapObject) response.getProperty(KEY_DIFF_GRAM);
            if (diffGram != null) {
                SoapObject dataSet = null;
                try {
                    dataSet = (SoapObject) diffGram.getProperty(KEY_NEW_DATA_SET);
                } catch (RuntimeException e) {
                    dataSet = (SoapObject) diffGram.getProperty(KEY_DOCUMENT_ELEMENT);
                }
                if (dataSet != null) {
                    final SoapObject firstDataEntry = (SoapObject) dataSet.getProperty(0);
                    final int errorCode = Integer.parseInt(firstDataEntry.getPrimitivePropertyAsString(KEY_ERROR_CODE));
                    final String errorMessage = firstDataEntry.getPrimitivePropertyAsString(KEY_ERROR_MESSAGE);
                    if (NO_ERROR == errorCode) {
                        if (isValidDataEntry(firstDataEntry)) {
                            handleResponse(dataSet);
                        } else {
                        	handleError(IErrorCode.ERROR_NO_VALID_DATA, "no valid data");
                        }
                    } else {
                        handleError(errorCode, errorMessage);
                    }
                }
            }
        }
    }

    private String getRpcResultTag() {
        return mRpcMethodName + "Result";
    }

    private static boolean isValidDataEntry(SoapObject dataEntry) {
        boolean isValid = false;
        if (dataEntry.getPropertyCount() <= 2) {
            return isValid;
        }
        Object propertyObject = dataEntry.getProperty(0);
        if (propertyObject instanceof SoapPrimitive) {
            isValid = true;
            return isValid;
        }
        for(int idx = 0; idx < dataEntry.getPropertyCount() - 2; idx++) {
            propertyObject = dataEntry.getProperty(idx);
            if (((SoapObject)propertyObject).getPropertyCount() > 0) {
                isValid = true;
                break;
            }
        }
        return isValid;
    }

}
