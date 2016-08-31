/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.core.object.attribute;

import name.huliqing.core.data.AttributeData;

/**
 * @author huliqing
 */
public class LongAttribute extends NumberAttribute<Long, AttributeData> {

    private long value;
    
    @Override
    public void setData(AttributeData data) {
        super.setData(data);
        value = data.getAsLong("value", value);
    }
    
    // 更新data值，以避免在外部使用data时获取不到实时的数据
    protected void updateData() {
        data.setAttribute("value", value);
    }
    
    @Override
    public final int intValue() {
        return (int) value;
    }

    @Override
    public final float floatValue() {
        return value;
    }

    @Override
    public final long longValue() {
        return value;
    }

    @Override
    public final double doubleValue() {
        return value;
    }
    
    @Override
    public final Long getValue() {
        return value;
    }

    @Override
    public final void setValue(final Number value) {
        setAndNotify(value.longValue());
    }
    
    @Override
    public final void add(final int other) {
        setAndNotify(value + other);
    }

    @Override
    public final void add(final float other) {
        setAndNotify((int)(value + other));
    }

    @Override
    public final void subtract(final int other) {
        setAndNotify(value - other);
    }

    @Override
    public final void subtract(final float other) {
        setAndNotify((int)(value - other));
    }

    @Override
    public final boolean isEqualTo(final int other) {
        return value == other;
    }

    @Override
    public final boolean isEqualTo(final float other) {
        return this.value == other;
    }

    @Override
    public final boolean greaterThan(final int other) {
        return value > other;
    }

    @Override
    public final boolean greaterThan(final float other) {
        return value > other;
    }

    @Override
    public final boolean lessThan(final int other) {
        return value < other;
    }

    @Override
    public final boolean lessThan(final float other) {
        return value < other;
    }
   
    @Override
    public final boolean match(final Object other) {
        if (other instanceof Number) {
            return this.doubleValue() == ((Number) other).doubleValue();
        }
        if (other instanceof String) {
            return this.longValue() == Long.parseLong((String) other);
        }
        return super.match(other);
    }
    
    /**
     * 设置当前属性的值，如果设置后属性值发生改变，则通知监听器(只有在值发生改变才通知监听器).
     * @param value 
     */
    protected void setAndNotify(long value) {
        long oldValue = this.value;
        this.value = value;
        updateData();
        if (oldValue != this.value) {
            notifyValueChangeListeners(oldValue, this.value);
        }
    }
    
}