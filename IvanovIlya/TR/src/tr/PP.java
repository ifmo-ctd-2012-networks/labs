package tr;

import tr.core.PayloadProcessor;

public class PP implements PayloadProcessor {
    @Override
    public Object getInitial() {
        return "";
    }

    @Override
    public Object process(Object payload) {
        String result = payload + "0";
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        return result;
    }
}
