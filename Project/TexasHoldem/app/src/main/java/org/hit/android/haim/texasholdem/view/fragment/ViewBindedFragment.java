package org.hit.android.haim.texasholdem.view.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * A fragment that works with view-binding, to allow cleaner access to views
 * in a fragment, without duplicating the usage over and over in every fragment.<br/>
 * The class can be constructed with some layout identifier, to use as the content root of the fragment,
 * and the view binding class, to which we should bind.
 * @author Haim Adrian
 * @since 11-Jun-21
 */
public abstract class ViewBindedFragment<T> extends Fragment {
    private T binding;
    private final Function<View, T> bindSupplier;

    /**
     * Constructs a new {@link ViewBindedFragment}.
     * @param contentLayoutId Resource identifier of fragment content
     * @param bindSupplier The view binding supplier. Accepts a View and returns the view binding ref. e.g. FragmentGameBinding::bind
     */
    public ViewBindedFragment(@LayoutRes int contentLayoutId, Function<View, T> bindSupplier) {
        super(contentLayoutId);
        this.bindSupplier = bindSupplier;
    }

    /**
     * Constructs a new {@link ViewBindedFragment}.
     * @param contentLayoutId Resource identifier of fragment content
     * @param viewBindingClass Binding class, at which we will lookup for the "bind" method
     */
    @SuppressWarnings("unchecked")
    public ViewBindedFragment(@LayoutRes int contentLayoutId, Class<T> viewBindingClass) {
        this(contentLayoutId, view -> {
            try {
                // Method is static, hence we send null.
                Method bindMethod = viewBindingClass.getDeclaredMethod("bind", View.class);
                return (T)bindMethod.invoke(null, view);
            } catch (Exception e) {
                throw new RuntimeException("Error has occurred while invoking bind(View)", e);
            }
        });

        // Make sure the method exists, here in the constructor, and not in the function above which
        // might fail at onViewCreated step only, instead of here, during construction.
        try {
            viewBindingClass.getDeclaredMethod("bind", View.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("bind(View) method is missing. Class: " + viewBindingClass.getName());
        }
    }

    protected T getBinding() {
        return binding;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = bindSupplier.apply(view);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
