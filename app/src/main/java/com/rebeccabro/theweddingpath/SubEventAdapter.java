package com.rebeccabro.theweddingpath;

/*
 * Name: Rebecca Scranton
 * Date: August 13, 2026
 * Description: Adapter for rendering the sub-event grid on the Vendor Detail screen.
 * Handles the UI interactions and routes Update and Delete commands back to the host Activity.
 */

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SubEventAdapter extends RecyclerView.Adapter<SubEventAdapter.ViewHolder> {

    private final List<SubEvent> eventList;
    private final SubEventClickListener listener;

    /**
     * Interface to pass click events safely back to VendorDetailActivity
     */
    public interface SubEventClickListener {
        void onUpdateClick(SubEvent event);
        void onDeleteClick(SubEvent event, int position);
    }

    public SubEventAdapter(List<SubEvent> eventList, SubEventClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sub_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SubEvent event = eventList.get(position);

        // Populate the text views (We will add Date formatting in a later polish pass)
        holder.tvTitle.setText(event.getTitle());
        holder.tvDate.setText(String.valueOf(event.getTimestamp()));

        // Wire up the Delete button and the Update (Row Click) actions
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(event, position));

        // Tapping the card itself acts as the "Update" trigger
        holder.itemView.setOnClickListener(v -> listener.onUpdateClick(event));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tv_event_name);
            tvDate = itemView.findViewById(R.id.tv_event_date);
            btnDelete = itemView.findViewById(R.id.btn_delete_event);
        }
    }
}
