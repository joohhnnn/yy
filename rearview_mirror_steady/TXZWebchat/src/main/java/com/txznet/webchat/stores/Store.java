package com.txznet.webchat.stores;

import android.os.Bundle;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;
import com.txznet.loader.AppLogic;
import com.txznet.webchat.actions.Action;
import com.txznet.webchat.dispatcher.Dispatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * 1. Cache data
 * 2. Expose public getters to access data (never have public setters)
 * 3. Respond to specific actions from the dispatcher
 * 4. Always emit a change when their data changes
 * 5. Only emit changes during a dispatch
 */
public abstract class Store {
    public interface OnStoreDispatchListener {
        void onDispatch(Action action);
    }

    protected Bus dataBus;
    protected String dispatchToken;
    protected Dispatcher dispatcher;
    protected List<OnStoreDispatchListener> listeners;

    /**
     * Constructs and registers an instance of this mWrapper with the given dispatcher.
     */
    Store(Dispatcher dispatcher) {
        dataBus = new Bus(ThreadEnforcer.ANY);
        dispatchToken = dispatcher.register(this);
        this.dispatcher = dispatcher;
        listeners = new ArrayList<OnStoreDispatchListener>();
    }

    /**
     * Adds a listener to the mWrapper, when the mWrapper changes the given callback will be called.
     */
    public void addListener(OnStoreDispatchListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove the listener from the mWrapper.
     */
    public void removeListener(OnStoreDispatchListener listener) {
        listeners.remove(listener);
    }


    public void register(Object obj) {
        dataBus.register(obj);
    }

    /**
     * Removes a callback from the mWrapper.
     */
    public void unregister(Object obj) {
        dataBus.unregister(obj);
    }

    /**
     * Returns the dispatcher this mWrapper is registered with.
     */
    public Dispatcher getDispatcher() {
        return this.dispatcher;
    }

    /**
     * Returns the dispatch token that the dispatcher recognizes this mWrapper by. Can be used to waitFor() this mWrapper.
     */
    public String getDispatchToken() {
        return dispatchToken;
    }

    /**
     * Emit an event notifying all listeners that this mWrapper has changed. This can only be invoked when dispatching.
     * Changes are de-duplicated and resolved at the end of this mWrapper's onDispatch function.
     */
    void emitChange(final String type) {
        emitChange(new StoreChangeEvent(type));
    }

    void emitChange(final StoreChangeEvent event) {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                dataBus.post(event);
            }
        }, 0);
    }

    /**
     * Subclasses must override this method. This is how the mWrapper receives actions from the dispatcher. All state mutation logic must be done during this method.
     */
    public abstract void onDispatch(Action action);

    public class StoreChangeEvent {
        private final String type;
        private Bundle data;

        public StoreChangeEvent(String type) {
            this(type, null);
        }

        public StoreChangeEvent(String type, Bundle data) {
            this.type = type;
            this.data = data;
        }

        public String getType() {
            return this.type;
        }

        public void setData(Bundle data) {
            this.data = data;
        }

        public Bundle getData() {
            return this.data;
        }
    }
}
