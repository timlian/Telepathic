package com.telepathic.finder.sdk;

public abstract interface ProcessListener {
    
    public abstract interface BusLocationListener {
        /**
         * 
         * @param lineNumber
         * @param errorMessage
         */
        public void onSuccess(String lineNumber, int distance);

        /**
         * 
         * @param errorMessage
         */
        public void onError(String errorMessage);
    }

}
