package com.project.agroworld.manufacture.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.agroworld.databinding.ProductAdminLayoutBinding;
import com.project.agroworld.databinding.ProductItemLayoutBinding;
import com.project.agroworld.manufacture.viewholder.ProductAdminViewHolder;
import com.project.agroworld.manufacture.listener.ManufactureAdminListener;
import com.project.agroworld.shopping.listener.OnProductListener;
import com.project.agroworld.shopping.model.ProductModel;
import com.project.agroworld.manufacture.viewholder.ProductViewHolder;

import java.util.ArrayList;
import java.util.List;


public class ProductAdapter extends RecyclerView.Adapter {
    int layoutType;
    private OnProductListener listener;
    private List<ProductModel> productModelList;
    private ManufactureAdminListener adminListener;
    private Context context;

    public ProductAdapter(List<ProductModel> productModelList, OnProductListener clickListener, int layoutType) {
        this.productModelList = productModelList;
        this.listener = clickListener;
        this.layoutType = layoutType;
    }

    public ProductAdapter(Context context, List<ProductModel> productModelListAction, ManufactureAdminListener adminListener, int layoutType) {
        this.context = context;
        this.productModelList = productModelListAction;
        this.adminListener = adminListener;
        this.layoutType = layoutType;
    }

    @Override
    public int getItemViewType(int position) {
        return layoutType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return new ProductAdminViewHolder(ProductAdminLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        } else {
            return new ProductViewHolder(ProductItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ProductModel productModel = productModelList.get(position);
        if (holder instanceof ProductAdminViewHolder) {
            ((ProductAdminViewHolder) holder).setData(context, productModel, adminListener);
        } else {
            ((ProductViewHolder) holder).setData(productModel, listener);
        }
    }

    @Override
    public int getItemCount() {
        return productModelList.size();
    }

    public void searchInProductList(ArrayList<ProductModel> searchProductList) {
        productModelList = searchProductList;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        productModelList.remove(position);
        notifyItemRangeChanged(position, productModelList.size());
    }
}
