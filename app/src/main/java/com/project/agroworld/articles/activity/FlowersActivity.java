package com.project.agroworld.articles.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.project.agroworld.R;
import com.project.agroworld.articles.adapter.FlowersAdapter;
import com.project.agroworld.articles.listener.FlowerClickListener;
import com.project.agroworld.articles.model.FlowersResponse;
import com.project.agroworld.databinding.ActivityFlowersBinding;
import com.project.agroworld.utils.CustomMultiColorProgressBar;
import com.project.agroworld.viewmodel.AgroViewModel;

import java.util.ArrayList;

public class FlowersActivity extends AppCompatActivity implements FlowerClickListener {
    private final ArrayList<FlowersResponse> flowersResponsesList = new ArrayList<>();
    private ActivityFlowersBinding binding;
    private CustomMultiColorProgressBar progressBar;
    private AgroViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_flowers);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.flowers));
        viewModel = new ViewModelProvider(this).get(AgroViewModel.class);
        progressBar = new CustomMultiColorProgressBar(this, getString(R.string.loader_message));
        viewModel.init(this);
        getFlowersListFromApi();
    }

    private void getFlowersListFromApi() {
        progressBar.showProgressBar();
        viewModel.getFlowersResponseLivedata().observe(this, resource -> {
            switch (resource.status) {
                case ERROR:
                    progressBar.hideProgressBar();
                    binding.rvFlowers.setVisibility(View.GONE);
                    binding.tvNoFlowersDataFound.setVisibility(View.VISIBLE);
                    binding.tvNoFlowersDataFound.setText(resource.message);
                    break;
                case LOADING:
                    progressBar.showProgressBar();
                    break;
                case SUCCESS:
                    if (resource.data != null) {
                        flowersResponsesList.clear();
                        flowersResponsesList.addAll(resource.data);
                        progressBar.hideProgressBar();
                        binding.rvFlowers.setVisibility(View.VISIBLE);
                        setRecyclerView();
                    } else {
                        binding.tvNoFlowersDataFound.setVisibility(View.VISIBLE);
                        binding.tvNoFlowersDataFound.setText(getString(R.string.no_data_found));
                    }
                    break;
            }
        });
    }

    private void setRecyclerView() {
        FlowersAdapter flowersAdapter = new FlowersAdapter(flowersResponsesList, this);
        binding.rvFlowers.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvFlowers.setAdapter(flowersAdapter);
    }

    @Override
    public void onFlowersClick(FlowersResponse response) {
        Intent intent = new Intent(FlowersActivity.this, ArticleDetailsActivity.class);
        intent.putExtra("flowerItemResponse", response);
        intent.putExtra("isFlowersResponse", true);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}