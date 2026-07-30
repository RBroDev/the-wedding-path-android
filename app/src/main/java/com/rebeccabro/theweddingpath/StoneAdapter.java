package com.rebeccabro.theweddingpath;

/*
 * Name: Rebecca Scranton
 * Date: July 27, 2026
 * Description: Adapter for the Wedding Path RecyclerView. Binds vendor data
 * and alternates card alignment for a winding path UI.
 */

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
 * This class binds vendor data to the UI and alternates the card alignment
 * left and right to create a custom, organic winding path aesthetic.
 */
public class StoneAdapter extends RecyclerView.Adapter<StoneAdapter.StoneViewHolder> {
    private final List<String> vendorList;

    /**
     * Constructor for the StoneAdapter.
     *
     * @param vendorList A list of vendor names to populate the path.
     */
    public StoneAdapter(List<String> vendorList) {
        this.vendorList = vendorList;
    }

    @NonNull
    @Override
    public StoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflates the custom FrameLayout wrapper that allows for left/right alignment
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stone_card, parent, false);
        return new StoneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoneViewHolder holder, int position) {
        String currentItem = vendorList.get(position);
        holder.vendorName.setText(currentItem);

        // extract layout parameters to manipulate the card's gravity within the FrameLayout
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) holder.cardContainer.getLayoutParams();

        // Modulo operator determines if the row is even or odd to create the winding effect
        if (position % 2 == 0) {
            params.gravity = Gravity.START;
        } else {
            params.gravity = Gravity.END;
        }
        holder.cardContainer.setLayoutParams(params);

        // evaluate data state to dynamically shift visual weight and denote milestone completion
        if (currentItem.contains("(Booked)")) {
            // completed state: fully opaque to draw user focus and reward progression
            holder.cardContainer.setAlpha(1.0f);
        } else if (currentItem.equals("+ Add New Vendor")) {
            // action state: highly translucent to visually recede and act as an empty slot placeholder
            holder.cardContainer.setAlpha(0.4f);
        } else {
            // Pending state: Semi-translucent default for unpopulated milestones
            holder.cardContainer.setAlpha(0.7f);
        }
    }
    
    @Override
    public int getItemCount() {
        return vendorList.size();
    }

    /**
     * ViewHolder pattern to cache UI components, improving RecyclerView scroll performance
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
    
    
