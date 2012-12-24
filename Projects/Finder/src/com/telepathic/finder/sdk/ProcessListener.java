package com.telepathic.finder.sdk;

import com.baidu.mapapi.MKStep;

public abstract interface ProcessListener {
    
    public abstract interface BusLocationListener {
        /**
         * 
         * @param lineNumber
         * @param errorMessage
         */
        public void onSuccess(MKStep station);

        /**
         * 
         * @param errorMessage
         */
        public void onError(String errorMessage);
    }

}
