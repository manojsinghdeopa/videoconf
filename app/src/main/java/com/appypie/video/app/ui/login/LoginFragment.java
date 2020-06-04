package com.appypie.video.app.ui.login;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.appypie.video.app.R;
import com.appypie.video.app.ViewModelFactory;
import com.appypie.video.app.base.BaseFragment;

import javax.inject.Inject;

import butterknife.BindView;


public class LoginFragment extends BaseFragment {


    @BindView(R.id.tv_error)
    TextView errorTextView;
    @BindView(R.id.loading_view)
    View loadingView;

    @Inject
    ViewModelFactory viewModelFactory;

    private LoginViewModel viewModel;

    @Override
    protected int layoutRes() {
        return R.layout.login_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this, viewModelFactory).get(LoginViewModel.class);

        observableViewModel();



    }


    private void observableViewModel() {


        viewModel.fetchRepos("Test","5");


        viewModel.getRepos().observe(getViewLifecycleOwner(), repos -> {
            if (repos != null) {
                Toast.makeText(getActivity(), repos, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), isError -> {
            if (isError != null) if (isError) {
                errorTextView.setVisibility(View.VISIBLE);
                errorTextView.setText("An Error Occurred While Loading Data!");
            } else {
                errorTextView.setVisibility(View.GONE);
                errorTextView.setText(null);
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                if (isLoading) {
                    errorTextView.setVisibility(View.GONE);
                }
            }
        });
    }
}
