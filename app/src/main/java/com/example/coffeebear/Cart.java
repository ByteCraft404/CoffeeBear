package com.example.coffeebear;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ismaeldivita.chipnavigation.ChipNavigationBar; // Import ChipNavigationBar
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Cart extends AppCompatActivity implements CartAdapter.OnItemClickListener {

    private ImageView backButton;
    private RecyclerView cartRecyclerView;
    private TextView totalAmountTextView;
    private Button checkoutButton;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItemList;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Chip Navigation Bar
    private ChipNavigationBar bottomNavigationBar; // Use ChipNavigationBar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Get references to UI elements
        backButton = findViewById(R.id.backButton);
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        totalAmountTextView = findViewById(R.id.totalAmount);
        checkoutButton = findViewById(R.id.checkoutButton);

        // Initialize Chip Navigation Bar
        bottomNavigationBar = findViewById(R.id.bottom_navigation_bar); // Match the ID from XML

        // Set up RecyclerView
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartItemList = new ArrayList<>();
        cartAdapter = new CartAdapter(cartItemList);
        cartAdapter.setOnItemClickListener(this);
        cartRecyclerView.setAdapter(cartAdapter);

        // Back button functionality
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(Cart.this, MainActivity2.class);
            startActivity(intent);
            finish();
        });

        // Fetch cart items and total price from Firebase
        fetchCartItems();
        fetchTotalPrice();

        // Checkout button click listener
        checkoutButton.setOnClickListener(v -> {
            Toast.makeText(this, "Checkout clicked", Toast.LENGTH_SHORT).show();
            // Implement your checkout logic here
        });

        // Set up ChipNavigationBar listener
        bottomNavigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                if (i == R.id.home) { // Make sure these match your @menu/bottom_nav_menu IDs
                    navigateTo(MainActivity2.class);
                } else if (i == R.id.favorites) {
                    navigateTo(Favourites.class);
                } else if (i == R.id.cart) {
                    // Already on Cart Activity, do nothing or provide feedback
                    // Toast.makeText(Cart.this, "You are already in Cart!", Toast.LENGTH_SHORT).show();
                } else if (i == R.id.profile) {
                    navigateTo(profile.class);
                }
            }
        });

        // Set Cart item as selected by default
        bottomNavigationBar.setItemSelected(R.id.cart, true); // Select the Cart item in the navigation bar
    }

    private void navigateTo(Class<?> destination) {
        Intent intent = new Intent(Cart.this, destination);
        startActivity(intent);
        finish();
    }

    private void fetchCartItems() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference cartItemsRef = mDatabase.child("carts").child(userId).child("cartItems");

            cartItemsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    cartItemList.clear();
                    for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                        String productId = itemSnapshot.getKey();
                        String name = itemSnapshot.child("name").getValue(String.class);
                        Double price = itemSnapshot.child("price").getValue(Double.class);
                        Integer quantity = itemSnapshot.child("quantity").getValue(Integer.class);
                        // Using Long for imageResource from Firebase to avoid potential casting issues
                        // and then converting to int if necessary
                        Long imageResourceLong = itemSnapshot.child("imageResource").getValue(Long.class);
                        int imageResource = (imageResourceLong != null) ? imageResourceLong.intValue() : -1;

                        if (name != null && price != null && quantity != null) { // imageResource can be -1
                            CartItem cartItem = new CartItem(productId, name, price, quantity, imageResource);
                            cartItemList.add(cartItem);
                        } else {
                            Log.w("CartActivity", "Missing data for cart item: " + productId);
                        }
                    }
                    cartAdapter.notifyDataSetChanged();
                    updateTotalPriceDisplay();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(Cart.this, "Failed to fetch cart items: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void fetchTotalPrice() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference totalPriceRef = mDatabase.child("carts").child(userId).child("totalPrice");

            totalPriceRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Double totalPrice = dataSnapshot.getValue(Double.class);
                    if (totalPrice != null) {
                        totalAmountTextView.setText(String.format("%.2f", totalPrice));
                    } else {
                        totalAmountTextView.setText("0.00");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(Cart.this, "Failed to fetch total price: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateTotalPriceDisplay() {
        double total = 0;
        for (CartItem item : cartItemList) {
            total += item.price * item.quantity;
        }
        totalAmountTextView.setText(String.format("%.2f", total));
        updateCartTotalInFirebase();
    }

    private void updateCartItemQuantity(int position, int newQuantity) {
        if (position >= 0 && position < cartItemList.size()) {
            CartItem item = cartItemList.get(position);
            if (newQuantity > 0) {
                item.quantity = newQuantity;
                mDatabase.child("carts").child(mAuth.getCurrentUser().getUid()).child("cartItems").child(item.productId).child("quantity").setValue(newQuantity);
                cartAdapter.notifyItemChanged(position);
                updateTotalPriceDisplay();
            } else {
                deleteCartItem(position);
            }
        }
    }

    private void deleteCartItem(int position) {
        if (position >= 0 && position < cartItemList.size()) {
            CartItem itemToRemove = cartItemList.get(position);
            mDatabase.child("carts").child(mAuth.getCurrentUser().getUid()).child("cartItems").child(itemToRemove.productId).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        cartItemList.remove(position);
                        cartAdapter.notifyItemRemoved(position);
                        updateTotalPriceDisplay();
                        Toast.makeText(Cart.this, itemToRemove.name + " removed from cart", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Cart.this, "Failed to remove " + itemToRemove.name + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateCartTotalInFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            double total = 0;
            for (CartItem item : cartItemList) {
                total += item.price * item.quantity;
            }
            mDatabase.child("carts").child(userId).child("totalPrice").setValue(total);
        }
    }

    @Override
    public void onDeleteClick(int position) {
        deleteCartItem(position);
    }

    @Override
    public void onIncreaseClick(int position) {
        int currentQuantity = cartItemList.get(position).quantity;
        updateCartItemQuantity(position, currentQuantity + 1);
    }

    @Override
    public void onDecreaseClick(int position) {
        int currentQuantity = cartItemList.get(position).quantity;
        if (currentQuantity > 1) {
            updateCartItemQuantity(position, currentQuantity - 1);
        } else {
            deleteCartItem(position);
        }
    }

    public static class CartItem {
        public String productId;
        public String name;
        public double price;
        public int quantity;
        public int imageResource;

        public CartItem() {
            // Default constructor required for Firebase
        }

        public CartItem(String productId, String name, double price, int quantity, int imageResource) {
            this.productId = productId;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.imageResource = imageResource;
        }
    }
}