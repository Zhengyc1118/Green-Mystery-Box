package comp5216.sydney.edu.au.greenmysterybox;

import java.util.List;

public class Order {

    private String id;
    private String uid;
    private String imageURL;
    private String restaurantName;
    private double longitude;
    private double latitude;
    private String boxName;
    private int pickupId;
    private String pickupTime;
    private int quantity;
    private double totalPrice;
    private String status;
    private List<String> contents;
    private String pickupAddress;

    public Order() {
        // Default constructor required for calls to DataSnapshot.getValue(MysteryBox.class)
    }

    public Order(String id,
                 String uid,
                 String imageURL,
                 String restaurantName,
                 String boxName,
                 String pickupAddress,
                 List<String> contents,
                 double longitude,
                 double latitude,
                 String pickupTime,
                 int pickupId,
                 int quantity,
                 double totalPrice,
                 String status) {
        this.id = id;
        this.uid = uid;
        this.imageURL = imageURL;
        this.restaurantName = restaurantName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.boxName = boxName;
        this.pickupAddress = pickupAddress;
        this.pickupTime = pickupTime;
        this.pickupId = pickupId;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.status = status;
        this.contents = contents;
    }

    public String getId() {
        return id;
    }
    public String getUid() {
        return uid;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public String getBoxName() {
        return boxName;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public int getPickupId() {
        return pickupId;
    }

    public String getPickupTime() {
        return pickupTime;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public List<String> getContents() {
        return contents;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPickupAddress(){return pickupAddress; }
}
