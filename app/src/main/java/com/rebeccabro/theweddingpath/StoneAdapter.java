package com.rebeccabro.theweddingpath;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

/**
 * Adapter for the Wedding Path RecyclerView.
 * Binds vendor data to the UI and dynamically alternates card alignment
 * to create an organic, winding path aesthetic.
 */
public class StoneAdapter extends RecyclerView.Adapter<StoneAdapter.StoneViewHolder> {

    private final List<String> vendorList;
    private final OnStoneClickListener listener;

    /**
     * Interface definition for a callback to be invoked when a stone is clicked.
     */
    public interface OnStoneClickListener {
        /**
         * Called when a vendor stone has been clicked.
         *
         * @param vendorName The name of the vendor associated with the clicked stone.
         */
        void onStoneClick(String vendorName);
    }

    /**
     * Constructs a new StoneAdapter.
     *
     * @param vendorList The data set containing the vendor names to display.
     * @param listener   The listener for handling click events on the items.
     */
    public StoneAdapter(List<String> vendorList, OnStoneClickListener listener) {
        this.vendorList = vendorList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stone_card, parent, false);
        return new StoneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoneViewHolder holder, int position) {
        String currentItem = vendorList.get(position);
        holder.vendorName.setText(currentItem);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) holder.cardContainer.getLayoutParams();

        // Alternate alignment between Start and End to simulate a winding visual path
        if (position % 2 == 0) {
            params.gravity = Gravity.START;
        } else {
            params.gravity = Gravity.END;
        }
        holder.cardContainer.setLayoutParams(params);

        // Apply a translucent visual state to distinguish the placeholder action slot
        if (currentItem.equals("+ Add New Vendor")) {
            holder.cardContainer.setAlpha(0.4f);
        } else {
            holder.cardContainer.setAlpha(1.0f);
        }

        holder.itemView.setOnClickListener(v -> listener.onStoneClick(currentItem));
    }

    @Override
    public int getItemCount() {
        return vendorList.size();
    }

    /**
     * ViewHolder class that holds references to the UI components for individual vendor stones.
     */
    public static class StoneViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardContainer;
        TextView vendorName;

        public StoneViewHolder(@NonNull View itemView) {
            super(itemView);
            cardContainer = itemView.findViewById(R.id.stone_card_container);
            vendorName = itemView.findViewById(R.id.tv_vendor_name);
        }
    }
}