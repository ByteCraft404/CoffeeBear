package com.example.coffeebear;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private Button checkoutButton; // Fixed: Changed to Button
    private CartAdapter cartAdapter;
    private List<CartItem> cartItemList;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Bottom Navigation Views
    private LinearLayout homeButton;
    private LinearLayout favoritesButton;
    private LinearLayout cartButton;
    private LinearLayout profileButton;

    private ImageView homeIcon;
    private ImageView favoritesIcon;
    private ImageView cartIcon;
    private ImageView profileIcon;

    private TextView homeText;
    private TextView favoritesText;
    private TextView cartText;
    private TextView profileText;

    private int colorCoffeeBeigeLight;
    private int colorA0A0A0;
    private int colorCoffeeDarkText;


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
        checkoutButton = findViewById(R.id.checkoutButton); // Assigned the Button here


        // Initialize Bottom Navigation Views using the provided IDs
        homeButton = findViewById(R.id.homeButton);
        favoritesButton = findViewById(R.id.favoritesButton);
        cartButton = findViewById(R.id.cartButton);
        profileButton = findViewById(R.id.profileButton);

        // Find ImageView and TextView within each LinearLayout
        homeIcon = homeButton.findViewById(R.id.homeIcon); // Ensure these IDs exist in your XML
        homeText = homeButton.findViewById(R.id.homeText);   // Ensure these IDs exist in your XML
        favoritesIcon = favoritesButton.findViewById(R.id.favoritesIcon); // Ensure these IDs exist in your XML
        favoritesText = favoritesButton.findViewById(R.id.favoritesText);   // Ensure these IDs exist in your XML
        cartIcon = cartButton.findViewById(R.id.cartIcon); // Ensure these IDs exist in your XML
        cartText = cartButton.findViewById(R.id.cartText);   // Ensure these IDs exist in your XML
        profileIcon = profileButton.findViewById(R.id.profileIcon); // Ensure these IDs exist in your XML
        profileText = profileButton.findViewById(R.id.profileText);   // Ensure these IDs exist in your XML

        // Get color values from resources
        colorCoffeeBeigeLight = getResources().getColor(R.color.coffee_beige_light, getTheme());
        colorA0A0A0 = getResources().getColor(android.R.color.darker_gray, getTheme()); // Using darker_gray as a close alternative to #A0A0A0
        colorCoffeeDarkText = getResources().getColor(R.color.coffee_dark_text, getTheme());

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

        // Set up bottom navigation click listeners
        homeButton.setOnClickListener(v -> navigateTo(MainActivity2.class));
        favoritesButton.setOnClickListener(v -> navigateTo(Favourites.class)); // Replace with your Favorites Activity
        cartButton.setOnClickListener(v -> {}); // Already on Cart Activity
        profileButton.setOnClickListener(v -> navigateTo(profile.class)); // Replace with your Profile Activity

        // Set default selected button to Cart and change its color
        setDefaultSelected();
    }

    private void setDefaultSelected() {
        // Reset colors of all buttons
        if (homeIcon != null && homeText != null) {
            homeIcon.setColorFilter(colorCoffeeDarkText);
            homeText.setTextColor(colorCoffeeBeigeLight);
        }

        if (favoritesIcon != null && favoritesText != null) {
            favoritesIcon.setColorFilter(colorCoffeeDarkText);
            favoritesText.setTextColor(colorA0A0A0);
        }

        if (cartIcon != null && cartText != null) {
            cartIcon.setColorFilter(colorCoffeeBeigeLight);
            cartText.setTextColor(colorCoffeeBeigeLight);
        }

        if (profileIcon != null && profileText != null) {
            profileIcon.setColorFilter(colorCoffeeDarkText);
            profileText.setTextColor(colorA0A0A0);
        }
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