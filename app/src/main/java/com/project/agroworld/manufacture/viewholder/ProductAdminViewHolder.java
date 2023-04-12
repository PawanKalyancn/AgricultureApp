package com.project.agroworld.manufacture.viewholder;

import android.content.Context;
import android.widget.PopupMenu;

import androidx.recyclerview.widget.RecyclerView;

import com.project.agroworld.R;
import com.project.agroworld.databinding.ProductAdminLayoutBinding;
import com.project.agroworld.manufacture.listener.ManufactureAdminListener;
import com.project.agroworld.shopping.model.ProductModel;
import com.project.agroworld.utils.Constants;

public class ProductAdminViewHolder extends RecyclerView.ViewHolder {

    final ProductAdminLayoutBinding binding;

    public ProductAdminViewHolder(ProductAdminLayoutBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void setData(Context context, ProductModel productModel, ManufactureAdminListener adminListener) {
        binding.tvProductName.setText(productModel.getTitle());
        binding.tvProductPrice.setText("₹" + productModel.getPrice());
        Constants.bindImage(
                binding.ivProductImage,
                productModel.getImageUrl(),
                binding.ivProductImage
        );
        binding.crdProductView.setOnClickListener(v -> adminListener.performOnCardClickAction(productModel));

        binding.ivProductMoreOption.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, binding.ivProductMoreOption);
            popupMenu.getMenuInflater().inflate(R.menu.transport_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.editProductDetails:
                        adminListener.performEditAction(productModel, getAbsoluteAdapterPosition());
                        return true;
                    case R.id.deleteProductDetails:
                        adminListener.performDeleteAction(productModel, getAbsoluteAdapterPosition());
                        return true;
                }
                return true;
            });
            popupMenu.show();
        });

    }
}