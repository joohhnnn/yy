package com.txznet.rxflux;

import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

import com.txznet.music.util.Logger;

/**
 * 行为，描述一个操作的来源，意图，视实际需要会额外携带数据
 *
 * @author zackzhou
 */
public class RxAction {
    static final String EXTRA_KEY_THROWABLE = "$throwable$";
    static final String EXTRA_KEY_IS_DATA = "$data$";
    static final String EXTRA_KEY_IS_ERROR = "$error$";

    public String type;
    public Operation operation;
    public ArrayMap<String, Object> data = new ArrayMap<>(2);
    State state = State.IDLE;

    public enum State {
        IDLE, PENDING, SUCCESS, ERROR, END
    }

    RxAction() {
    }

    RxAction(String type, Operation operation, ArrayMap<String, Object> data) {
        this.type = type;
        this.operation = operation;
        this.data = data;
        Logger.w("RxAction", "new instance");
    }

    boolean isData() {
        return state == State.SUCCESS;
    }

    boolean isError() {
        return state == State.ERROR;
    }

    Throwable getThrowable() {
        return (Throwable) data.get(EXTRA_KEY_THROWABLE);
    }

    RxAction markPending() {
        state = State.PENDING;
        return this;
    }

    // 标记为数据
    RxAction markData() {
        state = State.SUCCESS;
        return this;
    }

    // 标记为错误
    RxAction markError(Throwable throwable) {
        state = State.ERROR;
        data.put(EXTRA_KEY_THROWABLE, throwable);
        return this;
    }

    public <T> T getExtra(String key) {
        return (T) data.get(key);
    }

    public String getKey() {
        return this.type + "_" + this.operation;
    }

    @Override
    public String toString() {
        return getKey();
    }

    public static Builder type(@NonNull String type) {
        return new Builder().with(type);
    }

    public static class Builder {
        private String type;
        private Operation operation = Operation.AUTO;
        private ArrayMap<String, Object> data;

        Builder with(@NonNull String type) {
            if (type == null) {
                throw new IllegalArgumentException("Type may not be null.");
            }
            this.type = type;
            this.data = new ArrayMap<>();
            return this;
        }

        public Builder operation(@NonNull Operation operation) {
            this.operation = operation;
            return this;
        }

        public Builder bundle(@NonNull String key, Object value) {
            data.put(key, value);
            return this;
        }

        public RxAction build() {
            if (type == null || type.isEmpty()) {
                throw new IllegalArgumentException("At least one key is required.");
            }
            RxAction action = new RxAction();
//            try {
//                action = RxActionPool.getPool().borrowObject(type);
//                Logger.d("Pool", "borrowObject ->" + action);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            action.type = type;
            action.operation = operation;
            action.data = data;
            action.state = State.IDLE;
            return action;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RxAction action = (RxAction) o;

        if (!type.equals(action.type)) return false;
        return operation == action.operation;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (operation != null ? operation.hashCode() : 0);
        return result;
    }
}
