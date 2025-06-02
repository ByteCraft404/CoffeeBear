package com.example.coffeebear;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);
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

        // Set up RecyclerView
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartItemList = new ArrayList<>();
        cartAdapter = new CartAdapter(cartItemList);
        cartAdapter.setOnItemClickListener(this); // Set the listener
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
    }

    private void fetchCartItems() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference cartItemsRef = mDatabase.child("carts").child(userId).child("cartItems");

            cartItemsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    cartItemList.clear();
                    for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                        String productId = itemSnapshot.getKey(); // Get the product ID
                        String name = itemSnapshot.child("name").getValue(String.class);
                        Double price = itemSnapshot.child("price").getValue(Double.class);
                        Integer quantity = itemSnapshot.child("quantity").getValue(Integer.class);
                        Integer imageResource = itemSnapshot.child("imageResource").getValue(Integer.class);

                        if (name != null && price != null && quantity != null && imageResource != null) {
                            CartItem cartItem = new CartItem(productId, name, price, quantity, imageResource); // Include productId
                            cartItemList.add(cartItem);
                        } else if (name != null && price != null && quantity != null) {
                            CartItem cartItem = new CartItem(productId, name, price, quantity, -1); // Include productId
                            cartItemList.add(cartItem);
                            Log.w("CartActivity", "Image resource not found for item: " + name);
                        }
                    }
                    cartAdapter.notifyDataSetChanged();
                    updateTotalPriceDisplay(); // Update total after items are loaded
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
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
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Double totalPrice = dataSnapshot.getValue(Double.class);
                    if (totalPrice != null) {
                        totalAmountTextView.setText(String.format("%.2f", totalPrice));
                    } else {
                        totalAmountTextView.setText("0.00");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
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
        // Optionally update the totalPrice in Firebase here as well
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
                // If quantity becomes 0, you might want to remove the item
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
            // If quantity is 1 and decrease is clicked, you might want to remove the item
            deleteCartItem(position);
        }
    }

    // Modified CartItem model class to include productId:
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