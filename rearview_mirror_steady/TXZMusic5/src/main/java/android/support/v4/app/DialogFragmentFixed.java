package android.support.v4.app;

/**
 * 04-13 22:05:30.925 15452-15452/com.txznet.music E/CrashCommonHandler: java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
 * at android.support.v4.app.FragmentManagerImpl.checkStateLoss(FragmentManager.java:2053)
 * at android.support.v4.app.FragmentManagerImpl.enqueueAction(FragmentManager.java:2079)
 * at android.support.v4.app.BackStackRecord.commitInternal(BackStackRecord.java:678)
 * at android.support.v4.app.BackStackRecord.commit(BackStackRecord.java:632)
 * at android.support.v4.app.DialogFragment.dismissInternal(DialogFragment.java:223)
 * at android.support.v4.app.DialogFragment.dismiss(DialogFragment.java:190)
 *
 * @author zackzhou
 * @date 2019/4/13,22:12
 */

public class DialogFragmentFixed extends DialogFragment {
    /**
     * Display the dialog, adding the fragment to the given FragmentManager.  This
     * is a convenience for explicitly creating a transaction, adding the
     * fragment to it with the given tag, and committing it.  This does
     * <em>not</em> add the transaction to the back stack.  When the fragment
     * is dismissed, a new transaction will be executed to remove it from
     * the activity.
     *
     * @param manager The FragmentManager this fragment will be added to.
     * @param tag     The tag for this fragment, as per
     *                {@link FragmentTransaction#add(Fragment, String) FragmentTransaction.add}.
     */
    @Override
    public void show(FragmentManager manager, String tag) {
        mDismissed = false;
        mShownByMe = true;
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        // 这里把原来的commit()方法换成了commitAllowingStateLoss()
        ft.commitAllowingStateLoss();
    }
}
