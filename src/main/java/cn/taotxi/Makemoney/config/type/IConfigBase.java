package cn.taotxi.Makemoney.config.type;

import java.util.function.BiConsumer;

public interface IConfigBase<T> {

    public String getKey();
    
    public T getDefaultValue();

    public T getValue();

    public void setValue(T value);
    
    public String getComment();
    
    public void resetValue();
    
    public void triggerConfigChange();
    
    public void triggerConfigChangeDefault();

    public boolean exists();

    public void onChange(BiConsumer<T, T> listener);
}
