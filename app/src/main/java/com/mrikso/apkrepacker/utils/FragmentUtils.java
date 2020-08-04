package com.mrikso.apkrepacker.utils;

import android.os.Bundle;

import androidx.annotation.AnimRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.mrikso.apkrepacker.R;

public class FragmentUtils {

    private static int enter = R.anim.q_enter;
    private static int exit = R.anim.q_exit;
    private static int popEnter = R.anim.q_pop_enter;
    private static int popExit = R.anim.q_pop_exit;

    private FragmentUtils() {
    }

    @NonNull
    public static BundleBuilder getArgumentsBuilder(@NonNull Fragment fragment) {
        Bundle arguments = fragment.getArguments();
        if (arguments == null) {
            arguments = new Bundle();
            fragment.setArguments(arguments);
        }
        return BundleBuilder.buildUpon(arguments);
    }

    @Deprecated
    @Nullable
    public static <T> T findById(@NonNull FragmentManager fragmentManager, @IdRes int id) {
        //noinspection unchecked
        return (T) fragmentManager.findFragmentById(id);
    }

    @Nullable
    public static <T> T findById(@NonNull FragmentActivity activity, @IdRes int id) {
        //noinspection deprecation
        return findById(activity.getSupportFragmentManager(), id);
    }

    @Nullable
    public static <T> T findById(@NonNull Fragment parentFragment, @IdRes int id) {
        //noinspection deprecation
        return findById(parentFragment.getChildFragmentManager(), id);
    }

    @Deprecated
    @Nullable
    public static <T> T findByTag(@NonNull FragmentManager fragmentManager, @NonNull String tag) {
        //noinspection unchecked
        return (T) fragmentManager.findFragmentByTag(tag);
    }

    @Nullable
    public static <T> T findByTag(@NonNull FragmentActivity activity, @NonNull String tag) {
        //noinspection deprecation
        return findByTag(activity.getSupportFragmentManager(), tag);
    }

    @Nullable
    public static <T> T findByTag(@NonNull Fragment parentFragment, @NonNull String tag) {
        //noinspection deprecation
        return findByTag(parentFragment.getChildFragmentManager(), tag);
    }

    @Deprecated
    public static void add(@NonNull Fragment fragment, @NonNull FragmentManager fragmentManager, @IdRes int containerViewId, @Nullable String tag, String back_stack, @AnimRes int anim_in, @AnimRes int anim_out, @AnimRes int anim_popIn, @AnimRes int anim_popOut) {
        fragmentManager.beginTransaction()
                .setCustomAnimations(anim_in, anim_out, anim_popIn, anim_popOut)
                .add(containerViewId, fragment, tag)
                .addToBackStack(back_stack)
                .commit();
    }

   // @Deprecated
    public static void add(@NonNull Fragment fragment, @NonNull FragmentManager fragmentManager, @IdRes int containerViewId) {
        //noinspection deprecation
        add(fragment, fragmentManager, containerViewId, null, null, enter, exit, popEnter, popExit);
    }

    public static void add(@NonNull Fragment fragment, @NonNull FragmentManager fragmentManager, @IdRes int containerViewId, @NonNull String tag) {
        //noinspection deprecation
        add(fragment, fragmentManager, containerViewId, tag, null, enter, exit, popEnter, popExit);
    }

    public static void add(@NonNull Fragment fragment, @NonNull FragmentActivity activity, @IdRes int containerViewId) {
        //noinspection deprecation
        add(fragment, activity.getSupportFragmentManager(), containerViewId);
    }

    public static void add(@NonNull Fragment fragment, @NonNull Fragment parentFragment, @IdRes int containerViewId) {
        //noinspection deprecation
        add(fragment, parentFragment.getChildFragmentManager(), containerViewId);
    }

    public static void add(@NonNull Fragment fragment, @NonNull Fragment parentFragment, @IdRes int containerViewId, @NonNull String tag) {
        //noinspection deprecation
        add(fragment, parentFragment.getChildFragmentManager(), containerViewId, tag);
    }

  //  @Deprecated
    public static void add(@NonNull Fragment fragment, @NonNull FragmentManager fragmentManager, @NonNull String tag, String back_stack) {
        // Pass 0 as in {@link android.support.v4.app.BackStackRecord#add(Fragment, String)}.
        //noinspection deprecation
        add(fragment, fragmentManager, 0, tag, back_stack, 0, 0, 0, 0);
    }

    public static void add(@NonNull Fragment fragment, @NonNull FragmentActivity activity, @NonNull String tag, String back_stack) {
        //noinspection deprecation
        add(fragment, activity.getSupportFragmentManager(), tag, back_stack);
    }

    public static void add(@NonNull Fragment fragment, @NonNull Fragment parentFragment, @NonNull String tag, String back_stack) {
        //noinspection deprecation
        add(fragment, parentFragment.getChildFragmentManager(), tag, back_stack);
    }

    /**
     * @deprecated Always use an id or tag for restoration.
     */
    public static void add(@NonNull Fragment fragment, @NonNull FragmentActivity activity) {
        //noinspection deprecation
        add(fragment, activity.getSupportFragmentManager(), 0, null, null, enter, exit, popEnter, popExit);
    }

    /**
     * @deprecated Always use an id or tag for restoration.
     */
    public static void add(@NonNull Fragment fragment, @NonNull Fragment parentFragment) {
        //noinspection deprecation
        add(fragment, parentFragment.getChildFragmentManager(), 0, null, null, enter, exit, popEnter, popExit);
    }

    public static void remove(@NonNull Fragment fragment) {
        /*if (fragment.isRemoving()) {
            return;
        }

        fragment.getParentFragmentManager().beginTransaction().remove(fragment).commit()*/;
        fragment.getActivity().getSupportFragmentManager().popBackStack();
    }


    @Deprecated
    public static void replace(@NonNull Fragment fragment, @NonNull FragmentManager fragmentManager, @IdRes int containerViewId, @Nullable String tag, String back_stack, @AnimRes int enter, @AnimRes int exit, @AnimRes int popEnter, @AnimRes int popExit) {
        fragmentManager.beginTransaction()
                .setCustomAnimations(enter, exit, popEnter, popExit)
                .replace(containerViewId, fragment, tag)
                .addToBackStack(back_stack)
                .commit();
    }

    //@Deprecated
    public static void replace(@NonNull Fragment fragment, @NonNull FragmentManager fragmentManager, @IdRes int containerViewId, @Nullable String tag, boolean addToBackStack) {
        fragmentManager.beginTransaction()
                .setCustomAnimations(enter, exit, popEnter, popExit)
                .replace(containerViewId, fragment, tag)
                .commit();
    }

    @Deprecated
    public static void replace(@NonNull Fragment fragment, @NonNull FragmentManager fragmentManager, @IdRes int containerViewId, @Nullable String tag, @AnimRes int enter, @AnimRes int exit) {
        //noinspection deprecation
        replace(fragment, fragmentManager, containerViewId, tag, null, enter, exit, enter, exit);
    }

    @Deprecated
    public static void replace(@NonNull Fragment fragment, @NonNull FragmentManager fragmentManager, @IdRes int containerViewId, @Nullable String tag, @AnimRes int enter, @AnimRes int exit, @AnimRes int popEnter, @AnimRes int popExit) {
        //noinspection deprecation
        replace(fragment, fragmentManager, containerViewId, tag, null, enter, exit, popEnter, popExit);
    }

    @Deprecated
    public static void replace(@NonNull Fragment fragment, @NonNull FragmentManager fragmentManager, @IdRes int containerViewId, @AnimRes int enter, @AnimRes int exit) {
        //noinspection deprecation
        replace(fragment, fragmentManager, containerViewId, null, null, enter, exit, enter, exit);
    }

    @Deprecated
    public static void replace(@NonNull Fragment fragment, @NonNull FragmentManager fragmentManager, @IdRes int containerViewId, @AnimRes int enter, @AnimRes int exit, @AnimRes int popEnter, @AnimRes int popExit) {
        //noinspection deprecation
        replace(fragment, fragmentManager, containerViewId, null, null, enter, exit, popEnter, popExit);
    }

    public static void replace(@NonNull Fragment fragment, @NonNull FragmentActivity activity, @IdRes int containerViewId, @Nullable String tag, @AnimRes int enter, @AnimRes int exit, @AnimRes int popEnter, @AnimRes int popExit) {
        //noinspection deprecation
        replace(fragment, activity.getSupportFragmentManager(), containerViewId, tag, enter, exit, popEnter, popExit);
    }

    public static void replace(@NonNull Fragment fragment, @NonNull Fragment parentFragment, @IdRes int containerViewId, @Nullable String tag, @AnimRes int enter, @AnimRes int exit, @AnimRes int popEnter, @AnimRes int popExit) {
        //noinspection deprecation
        replace(fragment, parentFragment.getChildFragmentManager(), containerViewId, tag, enter, exit, popEnter, popExit);
    }

    @Deprecated
    public static void replace(@NonNull Fragment fragment, @NonNull FragmentManager fragmentManager, @NonNull String tag, String back_stack) {
        // Pass 0 as in {@link android.support.v4.app.BackStackRecord#replace(Fragment, String)}.
        //noinspection deprecation
        replace(fragment, fragmentManager, 0, tag, back_stack, enter, exit, popEnter, popExit);
    }

    public static void replace(@NonNull Fragment fragment, @NonNull FragmentManager fragmentManager, @IdRes int containerViewId, @NonNull String tag) {
        //noinspection deprecation
        replace(fragment, fragmentManager, containerViewId, tag, enter, exit, popEnter, popExit);
    }

    public static void replace(@NonNull Fragment fragment, @NonNull FragmentManager fragmentManager, @IdRes int containerViewId) {
        //noinspection deprecation
        replace(fragment, fragmentManager, containerViewId, null, enter, exit, popEnter, popExit);
    }

    public static void replace(@NonNull Fragment fragment, @NonNull FragmentActivity activity, @IdRes int containerViewId) {
        //noinspection deprecation
        replace(fragment, activity.getSupportFragmentManager(), containerViewId, null, enter, exit, popEnter, popExit);
    }

    public static void replace(@NonNull Fragment fragment, @NonNull FragmentActivity activity, @NonNull String tag, String back_stack) {
        //noinspection deprecation
        replace(fragment, activity.getSupportFragmentManager(), tag, back_stack);
    }

    public static void replace(@NonNull Fragment fragment, @NonNull Fragment parentFragment, @NonNull String tag, String back_stack) {
        //noinspection deprecation
        replace(fragment, parentFragment.getChildFragmentManager(), tag, back_stack);
    }

    public static void replace(@NonNull Fragment fragment, @NonNull FragmentActivity activity) {
        //noinspection deprecation
        replace(fragment, activity.getSupportFragmentManager(), 0, null, null, enter, exit, popEnter, popExit);
    }

    public static void replace(@NonNull Fragment fragment, @NonNull Fragment parentFragment) {
        //noinspection deprecation
        replace(fragment, parentFragment.getChildFragmentManager(), 0, null, null, enter, exit, popEnter, popExit);
    }

    public static void executePendingTransactions(@NonNull FragmentActivity activity) {
        activity.getSupportFragmentManager().executePendingTransactions();
    }

    public static void executePendingTransactions(@NonNull Fragment fragment) {
        fragment.getFragmentManager().executePendingTransactions();
    }

    @Nullable
    public static <T> T getParentAs(Fragment fragment, Class<T> asClass) {
        Object parent = fragment.getParentFragment();
        if (parent == null)
            parent = fragment.getActivity();

        if (asClass.isInstance(parent))
            return asClass.cast(parent);

        return null;
    }
}
