package com.marmu.handprint.sales_man.landing.activity.billing.view_delete_edit;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.marmu.handprint.R;
import com.marmu.handprint.z_common.Constants;
import com.marmu.handprint.z_common.Permissions;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public class ViewDeleteEditBillingActivity extends AppCompatActivity {

    TableLayout tableLayout;
    DatabaseReference priceDBRef = Constants.DATABASE.getReference(Constants.ADMIN_PRODUCT_PRICE);
    DatabaseReference takenDBRef = Constants.DATABASE.getReference(Constants.ADMIN_TAKEN);
    DatabaseReference billingDBRef = Constants.DATABASE.getReference(Constants.SALES_MAN_BILLING);

    ProgressDialog progressDialog;

    private String salesKey;
    private String salesMan;
    private String salesRoute;

    Spinner sp_party;

    HashMap<String, Object> partyDetails = new HashMap<>();
    HashMap<String, Object> localPartyDetails = new HashMap<>();
    HashMap<String, Object> productDetails = new HashMap<>();
    List<String> partyList = new ArrayList<>();

    HashMap<String, Object> leftQty = new HashMap<>();
    HashMap<String, Object> remainingQty = new HashMap<>();
    HashMap<String, Object> priceProd = new HashMap<>();
    HashMap<String, Object> billItem = new HashMap<>();

    private int total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_man_view_delete_edit_billing);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));

        tableLayout = findViewById(R.id.table_layout);
        progressDialog = new ProgressDialog(ViewDeleteEditBillingActivity.this);
        progressDialog.setTitle("Loading...");

        sp_party = findViewById(R.id.sp_party_name);
        sp_party.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tableLayout.removeAllViews();

                EditText partyName = findViewById(R.id.et_party_name);
                partyName.setText(sp_party.getSelectedItem().toString());
                partyName.setSelection(partyName.getText().length());

                updateTableHeader();
                updateTableBody();

                TextView billTotal = findViewById(R.id.tv_billing_total);
                try {
                    billTotal.setText(localPartyDetails.get("total").toString());
                    total = Integer.parseInt(localPartyDetails.get("total").toString());
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        salesKey = getIntent().getStringExtra("sales_key");
        salesMan = getIntent().getStringExtra("sales_man_name");
        salesRoute = getIntent().getStringExtra("sales_route");

        fetchPriceOfProd();
        setParty();

    }

    private void fetchPriceOfProd() {
        priceDBRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                priceProd = (HashMap<String, Object>) dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Error", databaseError.getMessage());
            }
        });
    }

    private void setParty() {
        billingDBRef.child(salesKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LinearLayout container = findViewById(R.id.view_delete_edit_layout);
                TextView noText = findViewById(R.id.no_layout);
                container.setVisibility(View.GONE);
                noText.setVisibility(View.GONE);
                if (dataSnapshot.getValue() != null) {
                    container.setVisibility(View.VISIBLE);
                    partyDetails = (HashMap<String, Object>) dataSnapshot.getValue();
                    for (String key : partyDetails.keySet()) {
                        partyList.add(key);
                    }

                    //sp_route
                    ArrayAdapter<String> nameAdapter = new ArrayAdapter<>(ViewDeleteEditBillingActivity.this,
                            android.R.layout.simple_spinner_item, partyList);
                    nameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_party.setAdapter(nameAdapter);
                } else {
                    noText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Error", databaseError.getMessage());
            }
        });
    }

    private void leftQTY() {
        takenDBRef.child(salesKey).child("sales_order_qty_left").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    leftQty = (HashMap<String, Object>) dataSnapshot.getValue();
                    for (String key : productDetails.keySet()) {
                        HashMap<String, Object> localProdDetails = (HashMap<String, Object>) productDetails.get(key);
                        int isThere = Integer.parseInt(localProdDetails.get("prod_qty").toString());
                        int isLeft = Integer.parseInt(leftQty.get(key).toString());
                        String total = String.valueOf(isLeft + isThere);
                        leftQty.put(key, total);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Error", databaseError.getMessage());
            }
        });
    }

    private void updateTableHeader() {
        /* Create a TableRow dynamically */
        TableRow tr = new TableRow(this);
        tr.setBackground(getResources().getDrawable(R.drawable.box_white_thick_border_ripple));
        tr.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT));
        tr.setWeightSum(3);

            /*Params*/
        TableRow.LayoutParams params = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 1.0f;


        /* Product Name --> TextView */
        TextView productNameHead = new TextView(this);
        productNameHead.setLayoutParams(params);

        productNameHead.setTextColor(getResources().getColor(R.color.colorBlack));
        productNameHead.setPadding(16, 16, 16, 16);
        productNameHead.setText("Product");
        productNameHead.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        productNameHead.setTypeface(null, Typeface.BOLD);
        productNameHead.setGravity(Gravity.CENTER);
        tr.addView(productNameHead);

        /* Product Price --> TextView */
        TextView productQTYHead = new TextView(this);
        productQTYHead.setLayoutParams(params);

        productQTYHead.setTextColor(getResources().getColor(R.color.colorBlack));
        productQTYHead.setPadding(16, 16, 16, 16);
        productQTYHead.setText("QTY");
        productQTYHead.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        productQTYHead.setTypeface(null, Typeface.BOLD);
        productQTYHead.setGravity(Gravity.CENTER);
        productQTYHead.setInputType(InputType.TYPE_CLASS_NUMBER);
        tr.addView(productQTYHead);

        /* Product Price --> TextView */
        TextView productPriceHead = new TextView(this);
        productPriceHead.setLayoutParams(params);

        productPriceHead.setTextColor(getResources().getColor(R.color.colorBlack));
        productPriceHead.setPadding(16, 16, 16, 16);
        productPriceHead.setText("Sub Total");
        productPriceHead.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        productPriceHead.setTypeface(null, Typeface.BOLD);
        productPriceHead.setGravity(Gravity.CENTER);
        productPriceHead.setInputType(InputType.TYPE_CLASS_NUMBER);
        tr.addView(productPriceHead);

        // Add the TableRow to the TableLayout
        tableLayout.addView(tr);
    }

    private void updateTableBody() {
        try {
            localPartyDetails = (HashMap<String, Object>) partyDetails.get(sp_party.getSelectedItem().toString());
            productDetails = (HashMap<String, Object>) localPartyDetails.get("product");


            for (String prodKey : productDetails.keySet()) {
            /* Create a TableRow dynamically */
                TableRow tr = new TableRow(this);
                tr.setBackground(getResources().getDrawable(R.drawable.box_white_thick_border_ripple));
                tr.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT));
                tr.setWeightSum(3);

            /*Params*/
                TableRow.LayoutParams params = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                params.weight = 1.0f;

                HashMap<String, Object> localProdDetails = (HashMap<String, Object>) productDetails.get(prodKey);

            /* Product Name --> TextView */
                TextView productName = new TextView(this);
                productName.setLayoutParams(params);

                productName.setTextColor(getResources().getColor(R.color.colorAccent));
                productName.setPadding(16, 16, 16, 16);
                productName.setText(prodKey);
                productName.setGravity(Gravity.CENTER);
                tr.addView(productName);

            /* Product QTY --> TextView */
                TextView productQTY = new TextView(this);
                productQTY.setLayoutParams(params);

                productQTY.setTextColor(getResources().getColor(R.color.colorAccent));
                productQTY.setPadding(16, 16, 16, 16);
                productQTY.setText(localProdDetails.get("prod_qty").toString());
                productQTY.setGravity(Gravity.CENTER);
                productQTY.setInputType(InputType.TYPE_CLASS_NUMBER);
                tr.addView(productQTY);

            /* Product Price --> TextView */
                TextView productPrice = new TextView(this);
                productPrice.setLayoutParams(params);

                productPrice.setTextColor(getResources().getColor(R.color.colorAccent));
                productPrice.setPadding(16, 16, 16, 16);
                productPrice.setText(localProdDetails.get("prod_sub_total").toString());
                productPrice.setGravity(Gravity.CENTER);
                productPrice.setInputType(InputType.TYPE_CLASS_NUMBER);
                tr.addView(productPrice);

                // Add the TableRow to the TableLayout
                tableLayout.addView(tr);
            }
            leftQTY();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }


    }

    @SuppressLint("SimpleDateFormat")
    public void saveBill(View view) {
        final EditText partyName = findViewById(R.id.et_party_name);

        if (TextUtils.isEmpty(partyName.getText())) {
            partyName.setError("Party Name is Mandatory");
            partyName.requestFocus();
        } else {
            if (billItem.size() > 0) {
                billItem.put("total", String.valueOf(total));
                billItem.put("sales_key", salesKey);
                billItem.put("sales_man_name", salesMan);
                billItem.put("sales_route", salesRoute);
                billItem.put("party_name", partyName.getText().toString());
                billItem.put("date", new SimpleDateFormat("dd/MM/yyyy")
                        .format(new Date(System.currentTimeMillis())));
                billingDBRef.child(salesKey).child(sp_party.getSelectedItem().toString()).removeValue();
                billingDBRef.child(salesKey).child(partyName.getText().toString()).updateChildren(billItem);
                takenDBRef.child(salesKey).child("sales_order_qty_left").updateChildren(remainingQty);
                finish();
            }
        }
    }

    public void deleteBill(View view) {
        if (leftQty.size() > 0) {
            takenDBRef.child(salesKey).child("sales_order_qty_left").updateChildren(leftQty);
            billingDBRef.child(salesKey).child(sp_party.getSelectedItem().toString()).removeValue();
            finish();
        }
    }

    public void printBill(View view) {
        Permissions.EXTERNAL_STORAGE(ViewDeleteEditBillingActivity.this);
        new PDF(productDetails);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_printer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*switch (item.getItemId()) {
            case R.id.action_connect:

                try {
                    findBT();
                    openBT();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return true;
            case R.id.action_disconnect:
                try {
                    closeBT();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
        }*/
        return true;
    }

    public void backPress(View view) {
        onBackPressed();
    }

}
