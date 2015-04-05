package com.thijsdev.studentaanhuis;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class PrikbordViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView adress, distance, omschrijving;
    private ClickListener clickListener;

    public PrikbordViewHolder(View itemView) {
        super(itemView);
        adress = (TextView) itemView.findViewById(R.id.prikbord_locatie);
        distance = (TextView) itemView.findViewById(R.id.prikbord_afstand);
        omschrijving = (TextView) itemView.findViewById(R.id.prikbord_omschrijving);
        itemView.findViewById(R.id.prikbord_item).setOnClickListener(this);
    }

    public interface ClickListener
    {
        public void onClick(View v, int position);

    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public void onClick(View v)
    {
        clickListener.onClick(v, getPosition());
    }
}
