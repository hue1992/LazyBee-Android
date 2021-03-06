package com.born2go.lazzybee.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.born2go.lazzybee.R;
import com.born2go.lazzybee.activity.CardDetailsActivity;
import com.born2go.lazzybee.db.Card;
import com.born2go.lazzybee.db.impl.LearnApiImplements;
import com.born2go.lazzybee.gtools.LazzyBeeSingleton;
import com.born2go.lazzybee.shared.LazzyBeeShare;
import com.daimajia.swipe.SwipeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hue on 7/6/2015.
 */
public class RecyclerViewIncomingListAdapter extends RecyclerView.Adapter<RecyclerViewIncomingListAdapter.RecyclerViewReviewTodayListAdapterViewHolder> {
    private static final String TAG = "ReviewAdapter";
    private static final int VIEW_CARD_1 = 1;
    private static final int VIEW_SUB_HEARDER_0 = 0;
    List<Object> objects = new ArrayList<>();
    List<Card> vocabularies;
    private Context context;
    private LearnApiImplements learnApiImplements;
    private RecyclerView mRecyclerViewReviewTodayList;
    private TextView lbCountReviewCard;
    private String mySubject;

    public RecyclerViewIncomingListAdapter(Context context, RecyclerView mRecyclerViewReviewTodayList, List<Card> vocabularies, TextView lbCountReviewCard) {
        this.context = context;
        this.vocabularies = vocabularies;
        this.learnApiImplements = LazzyBeeSingleton.learnApiImplements;
        this.mRecyclerViewReviewTodayList = mRecyclerViewReviewTodayList;
        this.lbCountReviewCard = lbCountReviewCard;
        this.mySubject=LazzyBeeShare.getMySubject();

        List<Card> deafaultList = new ArrayList<>();
        List<Card> customList = new ArrayList<>();
        for (Card card : vocabularies) {
            if (card.isCustom_list()) {
                customList.add(card);
            } else deafaultList.add(card);
        }

        if (customList.size() > 0) {
            objects.add(context.getString(R.string.custom_list));
            objects.addAll(customList);
            objects.add(context.getString(R.string.default_list));
            objects.addAll(deafaultList);
        } else {
            objects.addAll(deafaultList);
        }


    }

    @Override
    public RecyclerViewReviewTodayListAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == VIEW_CARD_1) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_card_list_result, parent, false); //Inflating the layout
        } else if (viewType == VIEW_SUB_HEARDER_0) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_incoming_list_sub_header, parent, false); //Inflating the layout
        }
        RecyclerViewReviewTodayListAdapterViewHolder recyclerViewReviewTodayListAdapterViewHolder = new RecyclerViewReviewTodayListAdapterViewHolder(view, viewType);
        return recyclerViewReviewTodayListAdapterViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerViewReviewTodayListAdapterViewHolder holder, final int position) {
        //Define view
        final View view = holder.view;
        if (holder.viewType == VIEW_CARD_1) {
            defineCardView(view, position);
        } else if (holder.viewType == VIEW_SUB_HEARDER_0) {
            TextView textView = (TextView) view.findViewById(R.id.sub_header);
            textView.setText(String.valueOf(objects.get(position)));
        }

    }

    private void defineCardView(final View view, int position) {
        final SwipeLayout swipeLayout = (SwipeLayout) view.findViewById(R.id.swipeLayout);
        TextView lbQuestion = (TextView) view.findViewById(R.id.lbQuestion);
        TextView lbMeaning = (TextView) view.findViewById(R.id.lbAnswer);
        TextView level = (TextView) view.findViewById(R.id.level);
        final TextView learned = (TextView) view.findViewById(R.id.learned);
        TextView lbPronoun = (TextView) view.findViewById(R.id.lbPronoun);

        //Define action card
        TextView lbIgnore = (TextView) view.findViewById(R.id.lbIgnore);
        TextView lbLearned = (TextView) view.findViewById(R.id.lbLearned);
        TextView lbAdd = (TextView) view.findViewById(R.id.lbAdd);
        lbAdd.setVisibility(View.GONE);
        LinearLayout mDetailsCard = (LinearLayout) view.findViewById(R.id.mDetailsCard);
        try {
            swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
            //add drag edge.(If the BottomView has 'layout_gravity' attribute, this line is unnecessary)
            swipeLayout.addDrag(SwipeLayout.DragEdge.Left, null);
            swipeLayout.addDrag(SwipeLayout.DragEdge.Right, view.findViewById(R.id.bottom_wrapper));


            //get Card by position
            final Card card = (Card) objects.get(position);

            String pronoun = card.getPronoun();//LazzyBeeShare._getValueFromKey(card.getAnswers(), LazzyBeeShare.CARD_PRONOUN);
            String meaning = card.getMeaning(mySubject);//LazzyBeeShare._getValueFromKey(card.getAnswers(), LazzyBeeShare.CARD_MEANING);

            lbQuestion.setText(card.getQuestion());
            lbMeaning.setText(meaning);
            lbPronoun.setText(pronoun);
            level.setText(String.valueOf(card.getLevel()));

            if (card.getQueue() >= Card.QUEUE_LNR1) {
                learned.setText(context.getResources().getString(R.string.learned));
            } else if (card.getQueue() == Card.QUEUE_DONE_2) {
                learned.setText(context.getResources().getString(R.string.done_card));
            } else if (card.getQueue() == Card.QUEUE_NEW_CRAM0) {
                learned.setText(context.getResources().getString(R.string.new_card));
            }
            learned.setVisibility(View.GONE);
            lbLearned.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        // Toast.makeText(context, "Learned Card:"+position, Toast.LENGTH_SHORT).show();
                        ignoreAndLearnedCard(mRecyclerViewReviewTodayList.getChildAdapterPosition(view), Card.QUEUE_DONE_2);
                    } catch (Exception e) {
                        LazzyBeeShare.showErrorOccurred(context, "1_onBindViewHolder", e);
                    }
                }
            });
            lbIgnore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        // Toast.makeText(context, "Ignore Card:"+position, Toast.LENGTH_SHORT).show();
                        ignoreAndLearnedCard(mRecyclerViewReviewTodayList.getChildAdapterPosition(view), Card.QUEUE_SUSPENDED_1);
                    } catch (Exception e) {
                        LazzyBeeShare.showErrorOccurred(context, "2_onBindViewHolder", e);
                    }
                }
            });
            mDetailsCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String cardId = String.valueOf(card.getId());
                    Intent intent = new Intent(context, CardDetailsActivity.class);
                    intent.putExtra(LazzyBeeShare.CARDID, cardId);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);
                }
            });
        } catch (Exception e) {
            LazzyBeeShare.showErrorOccurred(context, "onBindViewHolder", e);
        }

    }

    private void ignoreAndLearnedCard(int position, int queue) {
        try {
            Log.i(TAG, "position:" + position);
            Card card = (Card) objects.get(position);
            card.setQueue(queue);

            //Update Card in server
            learnApiImplements._updateCard(card);

            //re display Icoming list
            objects.remove(card);
            vocabularies.remove(card);

            //remove card id in Incomming List
            learnApiImplements.saveIncomingCardIdList(learnApiImplements._converlistCardToListCardId(vocabularies));

            //set size
            lbCountReviewCard.setText(context.getString(R.string.message_total_card_incoming) + vocabularies.size());
            lbCountReviewCard.setTag(vocabularies.size());
            //reset adapter
            //mRecyclerViewReviewTodayList.setAdapter(this);
            mRecyclerViewReviewTodayList.getAdapter().notifyItemRemoved(position);
        } catch (Exception e) {
            LazzyBeeShare.showErrorOccurred(context, "ignoreAndLearnedCard", e);
        }
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    @Override
    public int getItemViewType(int position) {
        Object obj = objects.get(position);
        if (obj instanceof String) return VIEW_SUB_HEARDER_0;
        else if (obj instanceof Card) return VIEW_CARD_1;
        else return -1;

    }

    public class RecyclerViewReviewTodayListAdapterViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private int viewType;

        public RecyclerViewReviewTodayListAdapterViewHolder(View itemView, int viewType) {
            super(itemView);
            this.view = itemView;
            this.viewType = viewType;
        }
    }
}
