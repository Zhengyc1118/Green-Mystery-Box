package comp5216.sydney.edu.au.greenmysterybox;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;
    private double userLatitude, userLongitude;
    private View.OnClickListener onItemClickListener;

    public OrderAdapter(Context context, List<Order> orderList, double userLatitude, double userLongitude) {
        this.context = context;
        this.orderList = orderList;
        this.userLatitude = userLatitude;
        this.userLongitude = userLongitude;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        if (order.getImageURL() != null) {
            Glide.with(context).load(order.getImageURL()).into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.image1);
        }

        holder.restaurantName.setText(order.getRestaurantName());

        double distance = calculateDistance(userLatitude, userLongitude, order.getLatitude(), order.getLongitude());
        holder.distance.setText(String.format("%.2f km", distance));

        holder.boxName.setText(order.getBoxName());
        holder.totalPrice.setText("$" + order.getTotalPrice());
        holder.pickupTime.setText("Pickup Time: " + order.getPickupTime());
        holder.pickupId.setText("Pickup ID: " + order.getPickupId());
        holder.status.setText("Pickup status: " + order.getStatus());
        holder.pickupAddress.setText("Pickup Address: " + order.getPickupAddress());
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        onItemClickListener = itemClickListener;
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView restaurantName, distance, boxName, totalPrice, pickupTime, pickupId, status, pickupAddress;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            restaurantName = itemView.findViewById(R.id.restaurantName);
            distance = itemView.findViewById(R.id.distance);
            boxName = itemView.findViewById(R.id.boxName);
            totalPrice = itemView.findViewById(R.id.totalPrice);
            pickupTime = itemView.findViewById(R.id.pickupTime);
            pickupId = itemView.findViewById(R.id.pickupId);
            status = itemView.findViewById(R.id.status);
            itemView.setTag(this);
            itemView.setOnClickListener(onItemClickListener);
            pickupAddress = itemView.findViewById(R.id.pickupAddress);
        }
    }

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the Earth in kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // returns the distance in kilometers
    }

    public void setUserLocation(double latitude, double longitude) {
        this.userLatitude = latitude;
        this.userLongitude = longitude;
    }

    public double getUserLatitude() {
        return userLatitude;
    }

    public double getUserLongitude() {
        return userLongitude;
    }



}



