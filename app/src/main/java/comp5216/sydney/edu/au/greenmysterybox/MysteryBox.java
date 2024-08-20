package comp5216.sydney.edu.au.greenmysterybox;

import java.io.Serializable;
import java.util.List;


public class MysteryBox implements Serializable {

    private String id;

    private String imageURL;
    private String restaurantName;
    private double longitude;
    private double latitude;
    private String boxName;
    private int originalPrice;
    private int currentPrice;
    private List<String> contents;
    private String pickupTime;
    private String category;
    private int stock;
    private String pickupAddress;

    public MysteryBox() {
        // Default constructor required for calls to DataSnapshot.getValue(MysteryBox.class)
    }

    public MysteryBox(String imageURL, String restaurantName, double longitude, double latitude, String boxName, int originalPrice, int currentPrice, List<String> contents, int stock, String pickupTime) {
        this.imageURL = imageURL;
        this.restaurantName = restaurantName;
        this.longitude = longitude;
        this.latitude = latitude;
        this.boxName = boxName;
        this.originalPrice = originalPrice;
        this.currentPrice = currentPrice;
        this.contents = contents;
        this.pickupTime = pickupTime;
        this.stock = stock;
        this.category = category;
        this.pickupAddress = pickupAddress;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getBoxName() {
        return boxName;
    }

    public int getOriginalPrice() {
        return originalPrice;
    }

    public int getCurrentPrice() {
        return currentPrice;
    }

    public List<String> getContents() {
        return contents;
    }

    public String getPickupTime() {
        return pickupTime;
    }
    public String getCategory() {return category; }

    public int getStock() {
        return stock;
    }

    public void setStock(int newStock) {
        this.stock = newStock;
    }
    public String getPickupAddress() {return this.pickupAddress; }


}




