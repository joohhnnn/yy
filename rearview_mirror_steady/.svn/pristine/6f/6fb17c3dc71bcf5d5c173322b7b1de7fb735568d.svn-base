package com.txznet.webchat.dispatcher;

import android.os.Handler;
import android.os.Looper;

import com.txznet.webchat.actions.Action;
import com.txznet.webchat.log.L;
import com.txznet.webchat.stores.Store;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dispatcher is used to broadcast actions to registered callbacks. This is
 * different from generic pub-sub systems in two ways:
 * <p/>
 * 1) Callbacks are not subscribed to particular events. Every action is
 * dispatched to every registered callback.
 * 2) Callbacks can be deferred in whole or part until other callbacks have
 * been executed.
 * <p/>
 * For example, consider this hypothetical flight destination form, which
 * selects a default city when a country is selected:
 * <p/>
 * var flightDispatcher = new Dispatcher();
 * <p/>
 * // Keeps track of which country is selected
 * var CountryStore = {country: null};
 * <p/>
 * // Keeps track of which city is selected
 * var CityStore = {city: null};
 * <p/>
 * // Keeps track of the base flight price of the selected city
 * var FlightPriceStore = {price: null}
 * <p/>
 * When a user changes the selected city, we dispatch the action:
 * <p/>
 * flightDispatcher.dispatch({
 * actionType: 'city-update',
 * selectedCity: 'paris'
 * });
 * <p/>
 * This action is digested by `CityStore`:
 * <p/>
 * flightDispatcher.register(function(action) {
 * if (action.actionType === 'city-update') {
 * CityStore.city = action.selectedCity;
 * }
 * });
 * <p/>
 * When the user selects a country, we dispatch the action:
 * <p/>
 * flightDispatcher.dispatch({
 * actionType: 'country-update',
 * selectedCountry: 'australia'
 * });
 * <p/>
 * This action is digested by both stores:
 * <p/>
 * CountryStore.dispatchToken = flightDispatcher.register(function(action) {
 * if (action.actionType === 'country-update') {
 * CountryStore.country = action.selectedCountry;
 * }
 * });
 * <p/>
 * When the callback to update `CountryStore` is registered, we save a reference
 * to the returned token. Using this token with `waitFor()`, we can guarantee
 * that `CountryStore` is updated before the callback that updates `CityStore`
 * needs to query its data.
 * <p/>
 * CityStore.dispatchToken = flightDispatcher.register(function(action) {
 * if (action.actionType === 'country-update') {
 * // `CountryStore.country` may not be updated.
 * flightDispatcher.waitFor([CountryStore.dispatchToken]);
 * // `CountryStore.country` is now guaranteed to be updated.
 * <p/>
 * // Select the default city for the new country
 * CityStore.city = getDefaultCityForCountry(CountryStore.country);
 * }
 * });
 * <p/>
 * The usage of `waitFor()` can be chained, for example:
 * <p/>
 * FlightPriceStore.dispatchToken =
 * flightDispatcher.register(function(action) {
 * switch (action.actionType) {
 * case 'country-update':
 * case 'city-update':
 * flightDispatcher.waitFor([CityStore.dispatchToken]);
 * FlightPriceStore.price =
 * getFlightPriceStore(CountryStore.country, CityStore.city);
 * break;
 * }
 * });
 * <p/>
 * The `country-update` action will be guaranteed to invoke the stores'
 * registered callbacks in order: `CountryStore`, `CityStore`, then
 * `FlightPriceStore`.
 */
public class Dispatcher {
    private static Dispatcher sInstance;
    private boolean isDispatching;
    private ConcurrentHashMap<String, Store> stores;
    private HashMap<String, Boolean> isHandled;
    private HashMap<String, Boolean> isPending;
    private long lastID;
    private Action pendingAction;
    private Handler mUiHandler;

    Dispatcher() {
        stores = new ConcurrentHashMap<>();
        isHandled = new HashMap<>();
        isPending = new HashMap<>();
        mUiHandler = new Handler(Looper.getMainLooper());
    }

    public static Dispatcher get() {
        if (sInstance == null) {
            sInstance = new Dispatcher();
        }
        return sInstance;
    }

    /**
     * Registers a callback to be invoked with every dispatched action. Returns
     * a token that can be used with `waitFor()`.
     */
    public synchronized String register(Store store) {
        String prefix = "ID_";
        String id = prefix + this.lastID++;
        stores.put(id, store);
        return id;
    }

    /**
     * Removes a callback based on its token.
     */
    public synchronized void unregister(String dispatchToken) {
        stores.remove(dispatchToken);
        isPending.remove(dispatchToken);
        isHandled.remove(dispatchToken);
    }

    /**
     * Waits for the callbacks specified to be invoked before continuing execution
     * of the current callback. This method should only be used by a callback in
     * response to a dispatched action.
     */
    public void waitFor(final String... dispatchTokens) {
        for (String dispatchToken : dispatchTokens) {
            if (isPending.get(dispatchToken) != null && isPending.get(dispatchToken)) {
                continue;
            }
            invokeDispatch(dispatchToken);
        }
    }

    /**
     * Dispatches a action to all registered callbacks.
     */
    public void dispatch(final Action action) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    _dispatch(action);
                }
            });
        } else {
            _dispatch(action);
        }
    }

    private void _dispatch(final Action action) {
        if (isDispatching) {
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    _dispatch(action);
                }
            });
            return;
        }
//        L.i("Dispatcher", "action[type=" + action.getType() + ", data=" + (action.getData() == null ? null :
//                (action.getData() instanceof JSONObject || action.getData() instanceof JSONArray) ? "@Json" : action.getData()) + "]");
        L.d("action[type=" + action.getType() + ", data=" + (action.getData() == null ? null :
                (action.getData() instanceof JSONObject || action.getData() instanceof JSONArray) ? "@Json" : action.getData()) + "]");
        startDispatching(action);
        try {
            for (String dispatchToken : stores.keySet()) {
                if (isPending.get(dispatchToken) != null && isPending.get(dispatchToken)) {
                    continue;
                }
                invokeDispatch(dispatchToken);
            }
        } finally {
            stopDispatching();
        }
    }


    /**
     * Is this Dispatcher currently dispatching.
     */
    public boolean isDispatching() {
        return this.isDispatching;
    }

    /**
     * Call the callback stored with the given id. Also do some internal
     * bookkeeping.
     */
    private void invokeDispatch(String dispatchToken) {
        if (stores.get(dispatchToken) != null) {
            isPending.put(dispatchToken, true);
            stores.get(dispatchToken).onDispatch(pendingAction);
            isHandled.put(dispatchToken, true);
        }
    }

    /**
     * Set up bookkeeping needed when dispatching.
     */
    private void startDispatching(Action action) {
        for (String dispatchToken : stores.keySet()) {
            isPending.put(dispatchToken, false);
            isHandled.put(dispatchToken, false);
        }
        pendingAction = action;
        isDispatching = true;
    }

    /**
     * Clear bookkeeping used for dispatching.
     */
    private void stopDispatching() {
        pendingAction = null;
        isDispatching = false;
    }
}
