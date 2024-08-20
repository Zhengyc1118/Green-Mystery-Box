package comp5216.sydney.edu.au.greenmysterybox;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.util.Comparator;
import java.util.List;

public class MysteryBoxAdapter extends RecyclerView.Adapter<MysteryBoxAdapter.MysteryBoxViewHolder> {

    private Context context;
    private List<MysteryBox> mysteryBoxList;
    private double userLatitude, userLongitude;
    private View.OnClickListener onItemClickListener;


    public MysteryBoxAdapter(Context context, List<MysteryBox> mysteryBoxList, double userLatitude, double userLongitude) {
        this.context = context;
        this.mysteryBoxList = mysteryBoxList;
        this.userLatitude = userLatitude;
        this.userLongitude = userLongitude;
    }

    @NonNull
    @Override
    public MysteryBoxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.mystery_box_item, parent, false);
        return new MysteryBoxViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MysteryBoxViewHolder holder, int position) {
        MysteryBox box = mysteryBoxList.get(position);
        Glide.with(context).load(box.getImageURL()).into(holder.imageView);
        holder.restaurantName.setText(box.getRestaurantName());

        double distance = calculateDistance(userLatitude, userLongitude, box.getLatitude(), box.getLongitude());
        holder.distance.setText(String.format("%.2f km", distance));

        holder.boxName.setText(box.getBoxName());
        holder.originalPrice.setText("$" + box.getOriginalPrice());
        holder.currentPrice.setText("$" + box.getCurrentPrice());
        holder.contents.setText(String.join(", ", box.getContents()));
        holder.pickupTime.setText("Pickup Time:" + box.getPickupTime());
    }

    @Override
    public int getItemCount() {
        return mysteryBoxList.size();
    }

    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        onItemClickListener = itemClickListener;
    }

    class MysteryBoxViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView restaurantName, distance, boxName, originalPrice, currentPrice, contents, pickupTime;

        public MysteryBoxViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            restaurantName = itemView.findViewById(R.id.restaurantName);
            distance = itemView.findViewById(R.id.distance);
            boxName = itemView.findViewById(R.id.boxName);
            originalPrice = itemView.findViewById(R.id.originalPrice);
            originalPrice.setPaintFlags(originalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            currentPrice = itemView.findViewById(R.id.currentPrice);
            contents = itemView.findViewById(R.id.contents);
            pickupTime = itemView.findViewById(R.id.pickupTime);
            itemView.setTag(this);
            itemView.setOnClickListener(onItemClickListener);
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

    public static class DistanceComparator implements Comparator<MysteryBox> {
        private double userLatitude, userLongitude;

        public DistanceComparator(double userLatitude, double userLongitude) {
            this.userLatitude = userLatitude;
            this.userLongitude = userLongitude;
        }

        @Override
        public int compare(MysteryBox box1, MysteryBox box2) {
            double distance1 = calculateDistance(userLatitude, userLongitude, box1.getLatitude(), box1.getLongitude());
            double distance2 = calculateDistance(userLatitude, userLongitude, box2.getLatitude(), box2.getLongitude());
            return Double.compare(distance1, distance2);
        }
    }



}




