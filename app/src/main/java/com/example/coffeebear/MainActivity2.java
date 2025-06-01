package com.example.coffeebear;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

public class MainActivity2 extends AppCompatActivity {

    private HorizontalScrollView coffeeScrollView;
    private LinearLayout coffeeLinearLayout;
    private CardView latteCardView, espressoCardView, cappuccinoCardView;
    private ImageView addButtonLatte, addButtonEspresso, addButtonCappuccino, addButtonBurger, addButtonFrenchToast;
    private Button latteButton, espressoButton, cappuccinoButton;
    private EditText searchEditText;
    private LinearLayout homeButtonLayout, favoritesButtonLayout, cartButtonLayout, profileButtonLayout; // Changed to LinearLayout
    private LinearLayout specialOfferLinearLayout;

    private Button currentSelectedCoffeeButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get References
        coffeeScrollView = findViewById(R.id.coffeeScrollView);
        coffeeLinearLayout = findViewById(R.id.coffeeLinearLayout);
        latteCardView = findViewById(R.id.latteCardView);
        espressoCardView = findViewById(R.id.espressoCardView);
        cappuccinoCardView = findViewById(R.id.cappuccinoCardView);
        addButtonLatte = findViewById(R.id.addButtonLatte);
        addButtonEspresso = findViewById(R.id.addButtonEspresso);
        addButtonCappuccino = findViewById(R.id.addButtonCappuccino);
        searchEditText = findViewById(R.id.searchEditText); // Using EditText
        latteButton = findViewById(R.id.latteButton);
        espressoButton = findViewById(R.id.espressoButton);
        cappuccinoButton = findViewById(R.id.cappuccinoButton);
        homeButtonLayout = findViewById(R.id.homeButton); // LinearLayout
        favoritesButtonLayout = findViewById(R.id.favoritesButton); // LinearLayout
        cartButtonLayout = findViewById(R.id.cartButton); // LinearLayout
        profileButtonLayout = findViewById(R.id.profileButton); // LinearLayout
        ConstraintLayout burgerCardView = findViewById(R.id.specialOfferScrollView).findViewById(R.id.burgerCardView);
        ConstraintLayout frenchToastCardView = findViewById(R.id.specialOfferScrollView).findViewById(R.id.frenchToastCardView);
        addButtonBurger = findViewById(R.id.specialOfferScrollView).findViewById(R.id.addButtonBurger);
        addButtonFrenchToast = findViewById(R.id.specialOfferScrollView).findViewById(R.id.addButtonFrenchToast);
        specialOfferLinearLayout = findViewById(R.id.specialOfferScrollView).findViewById(R.id.specialOfferLinearLayout);

        // Set Initial State (Espresso selected)
        selectCoffeeButton(espressoButton);
        adjustCardSizes(espressoCardView);
        centerScrollViewOnView(espressoCardView);

        // Set Listeners

        // Coffee Button Click Listeners
        latteButton.setOnClickListener(v -> {
            selectCoffeeButton(latteButton);
            centerScrollViewOnView(latteCardView);
            adjustCardSizes(latteCardView);
        });

        espressoButton.setOnClickListener(v -> {
            selectCoffeeButton(espressoButton);
            centerScrollViewOnView(espressoCardView);
            adjustCardSizes(espressoCardView);
        });

        cappuccinoButton.setOnClickListener(v -> {
            selectCoffeeButton(cappuccinoButton);
            centerScrollViewOnView(cappuccinoCardView);
            adjustCardSizes(cappuccinoCardView);
        });

        // Navigation Button Click Listeners (on LinearLayouts)
        homeButtonLayout.setOnClickListener(v -> navigateToDashboard(MainActivity2.class));
        favoritesButtonLayout.setOnClickListener(v -> navigateToDashboard(Favourites.class));
        cartButtonLayout.setOnClickListener(v -> navigateToDashboard(Cart.class));
        profileButtonLayout.setOnClickListener(v -> navigateToDashboard(profile.class));

        // Add Button Click Listeners (Coffee)
        addButtonLatte.setOnClickListener(v -> showAddToCartToast("Latte"));
        addButtonEspresso.setOnClickListener(v -> showAddToCartToast("Espresso"));
        addButtonCappuccino.setOnClickListener(v -> showAddToCartToast("Cappuccino"));
        addButtonBurger.setOnClickListener(v -> showAddToCartToast("Burger"));
        addButtonFrenchToast.setOnClickListener(v -> showAddToCartToast("French Toast"));

        // Search Functionality (on EditText)
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

        // HorizontalScrollView OnScrollChangeListener (Coffee)
        coffeeScrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            adjustCoffeeCardSizesOnScroll(scrollX);
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

    private void showAddToCartToast(String itemName) {
        Toast.makeText(MainActivity2.this, "Added " + itemName + " to Cart", Toast.LENGTH_SHORT).show();
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

    private void adjustCardSizes(CardView selectedCard) {
        // Define sizes for selected and unselected states (Coffee)
        int selectedWidth = (int) (180 * getResources().getDisplayMetrics().density);
        int unselectedWidth = (int) (140 * getResources().getDisplayMetrics().density);
        int selectedHeight = LinearLayout.LayoutParams.WRAP_CONTENT;
        int unselectedHeight = LinearLayout.LayoutParams.WRAP_CONTENT;
        float selectedTextSize = 18f;
        float unselectedTextSize = 16f;
        int selectedImageHeight = (int) (120 * getResources().getDisplayMetrics().density);
        int unselectedImageHeight = (int) (100 * getResources().getDisplayMetrics().density);
        float selectedButtonScale = 1.1f;
        float unselectedButtonScale = 1.0f;

        scaleCoffeeCard(latteCardView, (selectedCard == latteCardView) ? selectedWidth : unselectedWidth,
                (selectedCard == latteCardView) ? selectedHeight : unselectedHeight,
                (selectedCard == latteCardView) ? selectedTextSize : unselectedTextSize,
                (selectedCard == latteCardView) ? selectedImageHeight : unselectedImageHeight,
                (selectedCard == latteCardView) ? selectedButtonScale : unselectedButtonScale,
                R.id.latteNameTextView, R.id.latteImageView, R.id.addButtonLatte);

        scaleCoffeeCard(espressoCardView, (selectedCard == espressoCardView) ? selectedWidth : unselectedWidth,
                (selectedCard == espressoCardView) ? selectedHeight : unselectedHeight,
                (selectedCard == espressoCardView) ? selectedTextSize : unselectedTextSize,
                (selectedCard == espressoCardView) ? selectedImageHeight : unselectedImageHeight,
                (selectedCard == espressoCardView) ? selectedButtonScale : unselectedButtonScale,
                R.id.espressoNameTextView, R.id.espressoImageView, R.id.addButtonEspresso);

        scaleCoffeeCard(cappuccinoCardView, (selectedCard == cappuccinoCardView) ? selectedWidth : unselectedWidth,
                (selectedCard == cappuccinoCardView) ? selectedHeight : unselectedHeight,
                (selectedCard == cappuccinoCardView) ? selectedTextSize : unselectedTextSize,
                (selectedCard == cappuccinoCardView) ? selectedImageHeight : unselectedImageHeight,
                (selectedCard == cappuccinoCardView) ? selectedButtonScale : unselectedButtonScale,
                R.id.cappuccinoNameTextView, R.id.cappuccinoImageView, R.id.addButtonCappuccino);
    }

    private void scaleCoffeeCard(CardView cardView, int width, int height, float textSize, int imageHeight, float buttonScale, int nameId, int imageId, int buttonId) {
        cardView.getLayoutParams().width = width;
        cardView.getLayoutParams().height = height;
        TextView nameTextView = cardView.findViewById(nameId);
        ImageView imageView = cardView.findViewById(imageId);
        ImageView addButton = cardView.findViewById(buttonId);

        if (nameTextView != null) nameTextView.setTextSize(textSize);
        if (imageView != null) {
            imageView.getLayoutParams().height = imageHeight;
        }
        if (addButton != null) {
            addButton.setScaleX(buttonScale);
            addButton.setScaleY(buttonScale);
        }
        cardView.requestLayout();
    }

    private void adjustCoffeeCardSizesOnScroll(int scrollX) {
        int centerOfScrollView = scrollX + getResources().getDisplayMetrics().widthPixels / 2;

        adjustCoffeeCardSizeOnScroll(latteCardView, centerOfScrollView);
        adjustCoffeeCardSizeOnScroll(espressoCardView, centerOfScrollView);
        adjustCoffeeCardSizeOnScroll(cappuccinoCardView, centerOfScrollView);
    }

    private void adjustCoffeeCardSizeOnScroll(View cardView, int centerOfScrollView) {
        int cardCenter = cardView.getLeft() + cardView.getWidth() / 2;
        float distanceToCenter = Math.abs(cardCenter - centerOfScrollView);

        float maxScaleDistance = getResources().getDisplayMetrics().widthPixels / 2;
        float minScale = 0.8f;
        float maxScale = 1.2f;

        float scale = 1f;
        if (distanceToCenter < maxScaleDistance) {
            scale = maxScale - (maxScale - minScale) * (distanceToCenter / maxScaleDistance);
        }

        cardView.setScaleX(scale);
        cardView.setScaleY(scale);
        cardView.setElevation(scale * 8);
    }
}