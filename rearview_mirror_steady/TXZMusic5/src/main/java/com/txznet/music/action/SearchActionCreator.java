package com.txznet.music.action;

import com.txznet.rxflux.Dispatcher;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.RxAction;

import static com.txznet.music.action.ActionType.ACTION_SEARCH_KEY_JUST_INVOKE;
import static com.txznet.music.action.ActionType.ACTION_SEARCH_KEY_KEYWORD;
import static com.txznet.music.action.ActionType.ACTION_SEARCH_KEY_SEARCH_CHOICE;

public class SearchActionCreator {

    /**
     * 单例对象
     */
    private volatile static SearchActionCreator singleton;

    private SearchActionCreator() {
    }

    public static SearchActionCreator getInstance() {
        if (singleton == null) {
            synchronized (SearchActionCreator.class) {
                if (singleton == null) {
                    singleton = new SearchActionCreator();
                }
            }
        }
        return singleton;
    }


    //{"field":1,"text":"我要听小苹果","title":"小苹果","keywords":[],"artist":[],"album":""}
    public void getSearchData(Operation operation, String keyword, boolean justInvoke) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_SEARCH_GET_DATA).operation(operation)
                .bundle(ACTION_SEARCH_KEY_KEYWORD, keyword)
                .bundle(ACTION_SEARCH_KEY_JUST_INVOKE, justInvoke).build());
    }


    public void choiceSearchData(Operation operation, int index) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_SEARCH_EVENT_CHOICE_SEARCH_RESULT).operation(operation).bundle(ACTION_SEARCH_KEY_SEARCH_CHOICE, index).build());
    }

    public void cancelSearch(Operation operation) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_SEARCH_CANCEL).operation(operation).build());
    }
}
