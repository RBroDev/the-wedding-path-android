package com.rebeccabro.theweddingpath;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * Adapter for rendering the sub-event list on the Vendor Detail screen.
 * Handles UI binding and routes user interactions back to the host Activity.
 */
public class SubEventAdapter extends RecyclerView.Adapter<SubEventAdapter.ViewHolder> {

    private final List<SubEvent> eventList;
    private final SubEventClickListener listener;

    /**
     * Interface definition for callbacks to be invoked when a sub-event is interacted with.
     */
    public interface SubEventClickListener {
        /**
         * Called when a sub-event row is clicked to trigger an update.
         *
         * @param event The SubEvent object associated with the clicked row.
         */
        void onUpdateClick(SubEvent event);

        /**
         * Called when the delete action is triggered for a sub-event.
         *
         * @param event    The SubEvent object to delete.
         * @param position The adapter position of the item being deleted.
         */
        void onDeleteClick(SubEvent event, int position);
    }

    /**
     * Constructs a new SubEventAdapter.
     *
     * @param eventList The data set containing the sub-events to display.
     * @param listener  The listener for handling click events on the items.
     */
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

        // Bind data to views
        holder.tvTitle.setText(event.getTitle());
        holder.tvDate.setText(String.valueOf(event.getTimestamp()));

        // Route delete action to the listener
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(event, position));

        // Route row click action to the listener as an update trigger
        holder.itemView.setOnClickListener(v -> listener.onUpdateClick(event));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * ViewHolder class that holds references to the UI components for individual sub-events.
     */
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