package com.telepathic.finder.sdk;

public interface ICompletionListener {
    /**
     *
     * @param result
     */
    void onSuccess(Object result);

    /**
     *
     * @param errorCode
     * @param errorText
     */
    void onFailure(int errorCode, String errorText);
}
