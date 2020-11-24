package com.example.covidcaster;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GraphNameView extends RecyclerView.Adapter<GraphNameView.ViewHolder>
    {
        private List<String> captions;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            private CardView cardView;

            public ViewHolder(CardView v) {
                super(v);
                cardView = v;
            }
        }

        public GraphNameView(List<String> captions) {
            this.captions = captions;
        }

        @Override
        public int getItemCount() {
            return captions.size();
        }

        @Override
        public GraphNameView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            CardView cv = (CardView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.graph_card, parent, false);
            return new ViewHolder(cv);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            final CardView cardView = holder.cardView;


            TextView textView = cardView.findViewById(R.id.item_text);
            textView.setText(captions.get(position));

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(cardView.getContext(), GraphActivity.class);
                    intent.putExtra("statisticName", captions.get(position));
                    cardView.getContext().startActivity(intent);
                }
            });

        }

    }

