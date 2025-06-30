package com.example.coffeebear;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity2 extends AppCompatActivity {

    private HorizontalScrollView coffeeScrollView;
    private LinearLayout coffeeLinearLayout;
    private CardView latteCardView, espressoCardView, cappuccinoCardView;
    private ImageView addButtonLatte, addButtonEspresso, addButtonCappuccino, addButtonBurger, addButtonFrenchToast;
    private Button latteButton, espressoButton, cappuccinoButton;
    private EditText searchEditText;
    private LinearLayout homeButtonLayout, favoritesButtonLayout, cartButtonLayout, profileButtonLayout;
    private LinearLayout specialOfferLinearLayout;

    private Button currentSelectedCoffeeButton = null;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Define product details here (replace with your actual data source)
    private Map<String, Product> productMap = new HashMap<>();
    private Map<String, Integer> productImageMap = new HashMap<>(); // ADDED: Map for product images

    // TextViews for prices
    private TextView lattePriceTextView;
    private TextView espressoPriceTextView;
    private TextView cappuccinoPriceTextView;
    private TextView burgerPriceTextView;
    private TextView frenchToastPriceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
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

        // Get References
        coffeeScrollView = findViewById(R.id.coffeeScrollView);
        coffeeLinearLayout = findViewById(R.id.coffeeLinearLayout);
        latteCardView = findViewById(R.id.latteCardView);
        espressoCardView = findViewById(R.id.espressoCardView);
        cappuccinoCardView = findViewById(R.id.cappuccinoCardView);
        addButtonLatte = findViewById(R.id.addButtonLatte);
        Log.d("MainActivity2", "addButtonLatte: " + addButtonLatte);
        addButtonEspresso = findViewById(R.id.addButtonEspresso);
        Log.d("MainActivity2", "addButtonEspresso: " + addButtonEspresso);
        addButtonCappuccino = findViewById(R.id.addButtonCappuccino);
        Log.d("MainActivity2", "addButtonCappuccino: " + addButtonCappuccino);
        searchEditText = findViewById(R.id.searchEditText);
        latteButton = findViewById(R.id.latteButton);
        espressoButton = findViewById(R.id.espressoButton);
        cappuccinoButton = findViewById(R.id.cappuccinoButton);
        homeButtonLayout = findViewById(R.id.homeButton);
        favoritesButtonLayout = findViewById(R.id.favoritesButton);
        cartButtonLayout = findViewById(R.id.cartButton);
        profileButtonLayout = findViewById(R.id.profileButton);
        specialOfferLinearLayout = findViewById(R.id.specialOfferLinearLayout); // Get LinearLayout directly

        // Get references to price TextViews
        lattePriceTextView = findViewById(R.id.lattePriceTextView);
        espressoPriceTextView = findViewById(R.id.espressoPriceTextView);
        cappuccinoPriceTextView = findViewById(R.id.cappuccinoPriceTextView);
        burgerPriceTextView = findViewById(R.id.burgerPriceTextView);
        frenchToastPriceTextView = findViewById(R.id.frenchToastPriceTextView);

        // Initialize product map with names (prices will be fetched from layout)
        productMap.put("latte", new Product("Latte", 0.0));
        productMap.put("espresso", new Product("Espresso", 0.0));
        productMap.put("cappuccino", new Product("Cappuccino", 0.0));
        productMap.put("burger", new Product("Burger", 0.0));
        productMap.put("frenchToast", new Product("French Toast", 0.0));

        // Initialize the map of product IDs to their drawable resource IDs
        productImageMap.put("latte", R.drawable.latte); // Replace 'latte' with your actual drawable name
        productImageMap.put("espresso", R.drawable.espresso); // Replace 'espresso' with your actual drawable name
        productImageMap.put("cappuccino", R.drawable.cappuchino); // Replace 'cappuccino' with your actual drawable name
        productImageMap.put("burger", R.drawable.burger); // Replace 'burger' with your actual drawable name
        productImageMap.put("frenchToast", R.drawable.frenchtoast); // Replace 'french_toast' with your actual drawable name

        // Set Initial State (Espresso selected)
        selectCoffeeButton(espressoButton);
        centerScrollViewOnView(espressoCardView);

        // Set Listeners

        // Coffee Button Click Listeners
        latteButton.setOnClickListener(v -> {
            selectCoffeeButton(latteButton);
            centerScrollViewOnView(latteCardView);
        });

        espressoButton.setOnClickListener(v -> {
            selectCoffeeButton(espressoButton);
            centerScrollViewOnView(espressoCardView);
        });

        cappuccinoButton.setOnClickListener(v -> {
            selectCoffeeButton(cappuccinoButton);
            centerScrollViewOnView(cappuccinoCardView);
        });

        // Navigation Button Click Listeners
        homeButtonLayout.setOnClickListener(v -> navigateToDashboard(MainActivity2.class));
        favoritesButtonLayout.setOnClickListener(v -> navigateToDashboard(Favourites.class));
        cartButtonLayout.setOnClickListener(v -> navigateToDashboard(Cart.class));
        profileButtonLayout.setOnClickListener(v -> navigateToDashboard(profile.class));

        // Add Button Click Listeners (Special Offers - Corrected)
        ConstraintLayout burgerCardView = specialOfferLinearLayout.findViewById(R.id.burgerCardView);
        if (burgerCardView != null) {
            addButtonBurger = burgerCardView.findViewById(R.id.addButtonBurger);
            Log.d("MainActivity2", "addButtonBurger: " + addButtonBurger);
            if (addButtonBurger != null) {
                addButtonBurger.setOnClickListener(v -> handleAddToCart("burger"));
            } else {
                Log.e("MainActivity2", "addButtonBurger is null after finding burgerCardView");
            }
        } else {
            Log.e("MainActivity2", "burgerCardView is null");
        }

        ConstraintLayout frenchToastCardView = specialOfferLinearLayout.findViewById(R.id.frenchToastCardView);
        if (frenchToastCardView != null) {
            addButtonFrenchToast = frenchToastCardView.findViewById(R.id.addButtonFrenchToast);
            Log.d("MainActivity2", "addButtonFrenchToast: " + addButtonFrenchToast);
            if (addButtonFrenchToast != null) {
                addButtonFrenchToast.setOnClickListener(v -> handleAddToCart("frenchToast"));
            } else {
                Log.e("MainActivity2", "addButtonFrenchToast is null after finding frenchToastCardView");
            }
        } else {
            Log.e("MainActivity2", "frenchToastCardView is null");
        }

        // Add Button Click Listeners (Coffee - Already Correct)
        addButtonLatte.setOnClickListener(v -> handleAddToCart("latte"));
        addButtonEspresso.setOnClickListener(v -> handleAddToCart("espresso"));
        addButtonCappuccino.setOnClickListener(v -> handleAddToCart("cappuccino"));

        // Search Functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterItems(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });

        // Remove the OnScrollChangeListener for coffeeScrollView
    }

    private void handleAddToCart(String productId) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Product product = productMap.get(productId);
            if (product != null) {
                double price = 0.0;
                String name = product.name;
                Integer imageResource = productImageMap.get(productId); // Get the image resource ID

                switch (productId) {
                    case "latte":
                        price = parsePrice(lattePriceTextView.getText().toString());
                        break;
                    case "espresso":
                        price = parsePrice(espressoPriceTextView.getText().toString());
                        break;
                    case "cappuccino":
                        price = parsePrice(cappuccinoPriceTextView.getText().toString());
                        break;
                    case "burger":
                        price = parsePrice(burgerPriceTextView.getText().toString());
                        break;
                    case "frenchToast":
                        price = parsePrice(frenchToastPriceTextView.getText().toString());
                        break;
                }
                if (imageResource != null) {
                    addToCart(productId, name, price, imageResource); // Pass the image resource ID
                } else {
                    addToCart(productId, name, price, -1); // Or some default value if no image
                }
            } else {
                Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please log in to add to cart", Toast.LENGTH_SHORT).show();
            // Optionally redirect to login
        }
    }

    private void addToCart(String productId, String name, double price, int imageResource) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference cartItemsRef = mDatabase.child("carts").child(userId).child("cartItems").child(productId);

            cartItemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Integer currentQuantity = dataSnapshot.child("quantity").getValue(Integer.class);
                        if (currentQuantity != null) {
                            cartItemsRef.child("quantity").setValue(currentQuantity + 1);
                            // Consider updating image if needed, but likely not
                            updateCartTotal(userId);
                            Toast.makeText(MainActivity2.this, name + " quantity updated in Cart", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Map<String, Object> cartItem = new HashMap<>();
                        cartItem.put("name", name);
                        cartItem.put("price", price);
                        cartItem.put("quantity", 1);
                        cartItem.put("imageResource", imageResource); // Store the image resource ID
                        cartItemsRef.setValue(cartItem);
                        updateCartTotal(userId);
                        Toast.makeText(MainActivity2.this, name + " added to Cart", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(MainActivity2.this, "Failed to add to cart: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Please log in to add to cart", Toast.LENGTH_SHORT).show();
            // Optionally redirect to login
        }
    }

    private void updateCartTotal(String userId) {
        DatabaseReference cartRef = mDatabase.child("carts").child(userId).child("cartItems");
        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double total = 0;
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    Double price = itemSnapshot.child("price").getValue(Double.class);
                    Integer quantity = itemSnapshot.child("quantity").getValue(Integer.class);
                    if (price != null && quantity != null) {
                        total += price * quantity;
                    }
                }
                mDatabase.child("carts").child(userId).child("totalPrice").setValue(total);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void selectCoffeeButton(Button selectedButton) {
        if (currentSelectedCoffeeButton != null) {
            currentSelectedCoffeeButton.setBackgroundTintList(getResources().getColorStateList(R.color.coffee_dark_text));
            currentSelectedCoffeeButton.setTextColor(getResources().getColor(R.color.coffee_beige_light));
        }
        selectedButton.setBackgroundTintList(getResources().getColorStateList(R.color.coffee_brown_dark));
        selectedButton.setTextColor(getResources().getColor(android.R.color.white));
        currentSelectedCoffeeButton = selectedButton;
    }

    private void centerScrollViewOnView(View view) {
        final int screenWidth = getResources().getDisplayMetrics().widthPixels;
        final int scrollX = Math.max(0, view.getLeft() - (screenWidth - view.getWidth()) / 2);
        coffeeScrollView.post(() -> coffeeScrollView.smoothScrollTo(scrollX, 0));
    }

    private void navigateToDashboard(Class<?> dashboardClass) {
        Intent intent = new Intent(MainActivity2.this, dashboardClass);
        startActivity(intent);
    }

    private void filterItems(String searchText) {
        searchText = searchText.toLowerCase();
        filterCoffee(searchText);
        filterSpecialOffers(searchText);
    }

    private void filterCoffee(String searchText) {
        for (int i = 0; i < coffeeLinearLayout.getChildCount(); i++) {
            View cardView = coffeeLinearLayout.getChildAt(i);
            if (cardView instanceof CardView) {
                TextView nameTextView = null;
                if (cardView.getId() == R.id.latteCardView) {
                    nameTextView = cardView.findViewById(R.id.latteNameTextView);
                } else if (cardView.getId() == R.id.espressoCardView) {
                    nameTextView = cardView.findViewById(R.id.espressoNameTextView);
                } else if (cardView.getId() == R.id.cappuccinoCardView) {
                    nameTextView = cardView.findViewById(R.id.cappuccinoNameTextView);
                }

                if (nameTextView != null && nameTextView.getText().toString().toLowerCase().contains(searchText)) {
                    cardView.setVisibility(View.VISIBLE);
                } else {
                    cardView.setVisibility(View.GONE);
                }
            }
        }
    }

    private void filterSpecialOffers(String searchText) {
        for (int i = 0; i < specialOfferLinearLayout.getChildCount(); i++) {
            View cardView = specialOfferLinearLayout.getChildAt(i);
            if (cardView instanceof CardView) {
                TextView nameTextView = null;
                if (cardView.getId() == R.id.burgerCardView) {
                    nameTextView = cardView.findViewById(R.id.burgerNameTextView);
                } else if (cardView.getId() == R.id.frenchToastCardView) {
                    nameTextView = cardView.findViewById(R.id.frenchToastNameTextView);
                }

                if (nameTextView != null && nameTextView.getText().toString().toLowerCase().contains(searchText)) {
                    cardView.setVisibility(View.VISIBLE);
                } else {
                    cardView.setVisibility(View.GONE);
                }
            }
        }
    }

    private double parsePrice(String priceString) throws NumberFormatException {
        // Remove currency symbol and any other non-numeric characters
        String cleanedPrice = priceString.replaceAll("[^\\d.]", "");
        return Double.parseDouble(cleanedPrice);
    }

    // Simple Product class
    public static class Product {
        public String name;
        public double price;

        public Product(String name, double price) {
            this.name = name;
            this.price = price;
        }
    }
}