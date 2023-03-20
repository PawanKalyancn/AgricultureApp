package com.project.agroworld.payment.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.project.agroworld.R;
import com.project.agroworld.databinding.ActivityPaymentDetailsBinding;
import com.project.agroworld.payment.model.PaymentModel;
import com.project.agroworld.shopping.model.ProductModel;
import com.project.agroworld.utils.Constants;
import com.project.agroworld.viewmodel.AgroViewModel;
import com.razorpay.Checkout;
import com.razorpay.ExternalWalletListener;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PaymentDetailsActivity extends AppCompatActivity implements PaymentResultWithDataListener, ExternalWalletListener {

    ActivityPaymentDetailsBinding binding;
    private ArrayList<ProductModel> productCartList = new ArrayList<>();
    String address;
    StringBuilder stringBuilder = new StringBuilder();
    private AlertDialog.Builder alertDialogBuilder;
    private AgroViewModel viewModel;
    FirebaseAuth auth;
    FirebaseUser user;
    int countBackPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_payment_details);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        viewModel = ViewModelProviders.of(this).get(AgroViewModel.class);
        viewModel.init();
        Checkout.preload(getApplicationContext());
        Intent intent = getIntent();
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        productCartList = (ArrayList<ProductModel>) intent.getSerializableExtra("productItemList");
        address = intent.getStringExtra("address");

        String totalAmount = intent.getStringExtra("totalAmount");
        String[] amountArr = totalAmount.split(" ");
        double amount = Double.parseDouble(amountArr[1]);

        for (int product = 0; product < productCartList.size(); product++) {
            stringBuilder.append(productCartList.get(product).getTitle()).append(" ₹ ").append(productCartList.get(product).getPrice()).append(" Q ").append(productCartList.get(product).getQuantity()).append("\n");
        }
        binding.tvProductNameAndPrice.setText("Product list- \n" + stringBuilder);
        binding.tvAddress.setText("Address- " + address);
        binding.tvPaymentDate.setText("Date- " + date);
        binding.tvTotalPayAmt.setText("Total- " + totalAmount);

        alertDialogBuilder = new AlertDialog.Builder(PaymentDetailsActivity.this);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setTitle("Payment Result");
        alertDialogBuilder.setPositiveButton("Ok", (dialog, which) -> {
            finishActivity(289);
        });
        alertDialogBuilder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });

        viewModel.checkLoadingStatus().observe(this, s -> {
            Constants.showToast(this, s);
        });

        binding.btnProceed.setOnClickListener(v -> {
            startPayment(amount * 100);
        });
    }

    public void startPayment(double amount) {
        final Activity activity = this;
        final Checkout checkout = new Checkout();
        checkout.setKeyID(Constants.RAZORPAY_KEY_ID);
        try {
            JSONObject options = new JSONObject();
            options.put("name", "Agro World");
            options.put("description", "Seeds shopping");
            options.put("send_sms_hash", true);
            options.put("allow_rotation", true);
            //You can omit the image option to fetch the image from dashboard
            options.put("image", Constants.APP_ICON_LINK);
            options.put("currency", "INR");
            options.put("amount", amount);
            options.put("payment_capture", 1);
            JSONObject preFill = new JSONObject();
            preFill.put("email", user.getEmail());
            preFill.put("contact", user.getPhoneNumber());
            options.put("prefill", preFill);
            checkout.open(activity, options);
        } catch (Exception e) {
            Toast.makeText(activity, "Error in payment: " + e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
            e.printStackTrace();
        }
    }

    @Override
    public void onExternalWalletSelected(String s, PaymentData paymentData) {
        try {
            printLog(paymentData.getData().toString());
            uploadTransactionDetailToFirebase(paymentData);
            showAlertMessageWithStatus(paymentData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPaymentSuccess(String s, PaymentData paymentData) {
        try {
            printLog(paymentData.getData().toString());
            uploadTransactionDetailToFirebase(paymentData);
            showAlertMessageWithStatus(paymentData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPaymentError(int i, String s, PaymentData paymentData) {
        try {
            printLog(paymentData.getData().toString());
            uploadTransactionDetailToFirebase(paymentData);
            showAlertMessageWithStatus(paymentData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    private void uploadTransactionDetailToFirebase(PaymentData paymentData) {
        PaymentModel paymentModel;
        if (paymentData.getPaymentId() != null) {
            for (int product = 0; product < productCartList.size(); product++) {
                paymentModel = new PaymentModel(
                        productCartList.get(product).getTitle(),
                        productCartList.get(product).getImageUrl(),
                        productCartList.get(product).getPrice(),
                        "false",
                        paymentData.getPaymentId() + "-" + product
                );
                viewModel.uploadTransaction(paymentModel, Constants.plainStringEmail(user.getEmail()));
            }
        }
    }

    public void showAlertMessageWithStatus(PaymentData paymentData) {
        if (paymentData.getPaymentId() != null) {
            viewModel.deleteCartData(Constants.plainStringEmail(user.getEmail()));
            alertDialogBuilder.setMessage(
                    "Payment Success"
                            + "\nPayment ID: " + paymentData.getPaymentId()
                            + "\nSignature: " + paymentData.getSignature()
                            + "\nContact: " + paymentData.getUserContact()
                            + "\nEmail: " + paymentData.getUserEmail()
                            + "\nExternalWallet: " + paymentData.getExternalWallet()
                            + "\nPayment Data: " + paymentData.getData()
            );
            alertDialogBuilder.show();
        } else {
            alertDialogBuilder.setMessage(
                    "Payment Failed"
                            + "\nPayment ID: " + paymentData.getPaymentId()
                            + "\nSignature: " + paymentData.getSignature()
                            + "\nPayment Data: " + paymentData.getData()

            );
            alertDialogBuilder.show();
        }
    }

    @Override
    public void onBackPressed() {
        countBackPressed++;
        if (countBackPressed == 1) {
            Constants.showToast(this, "Please press again to exit.");
        } else {
            finish();
            countBackPressed = 0;
            super.onBackPressed();
        }
    }

    private void printLog(String message) {
        Log.d("paymentActivity", message);
    }
}