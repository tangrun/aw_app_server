package cn.wildfirechat.app.tools;

public interface Invoker<T> {
    void onInvoke(T target);
}
