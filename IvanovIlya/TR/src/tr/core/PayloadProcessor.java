package tr.core;

public interface PayloadProcessor {
    Object getInitial();

    Object process(Object payload);
}
