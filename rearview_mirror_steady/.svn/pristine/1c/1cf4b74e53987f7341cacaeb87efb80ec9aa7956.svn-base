EasyRecyclerView  有毒,慎用padding属性
发生的经历是:
在写订阅界面的时候,右上角的星星按钮,一直点击没有反应,各种找原因,发现是位置点击的不对.
在各种排除法之后,发现是不能对recycleView进行设置padding

错误的写法:
```
    <com.jude.easyrecyclerview.EasyRecyclerView xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:app="http://schemas.android.com/apk/res-auto"
        android:id="@+id/rv_data"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_empty="@layout/subscribe_empty_view"
        app:layout_error="@layout/view_error"
        app:layout_progress="@layout/view_progress"
        android:PaddingLeft="@dimen/m40"
        android:PaddingRight="@dimen/m40" />
``

正确的写法:
```
    <com.jude.easyrecyclerview.EasyRecyclerView xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:app="http://schemas.android.com/apk/res-auto"
        android:id="@+id/rv_data"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_empty="@layout/subscribe_empty_view"
        app:layout_error="@layout/view_error"
        app:layout_progress="@layout/view_progress"
        app:recyclerPaddingLeft="@dimen/m40"
        app:recyclerPaddingRight="@dimen/m40" />
```
