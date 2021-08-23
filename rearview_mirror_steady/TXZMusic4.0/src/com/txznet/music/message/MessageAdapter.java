package com.txznet.music.message;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.txznet.music.R;
import com.txznet.music.utils.ScreenUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by brainBear on 2017/12/23.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private static final String TAG = "MessageAdapter:";
    private List<Message> mMessages;
    private boolean isSelectStatus;
    private SimpleDateFormat mFormat;
    private List<Message> mSelectMessages;
    private OnMessageClickListener mListener;

    public MessageAdapter() {
        mFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        mSelectMessages = new ArrayList<>();
    }

    public List<Message> getSelectMessages() {
        return this.mSelectMessages;
    }

    public void setSelectStatus(boolean isSelectStatus) {
        this.isSelectStatus = isSelectStatus;
        notifyDataSetChanged();
    }

    public void replaceData(List<Message> messages) {
        this.mMessages = messages;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (ScreenUtils.isPhonePortrait()) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_phone_portrait, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Message message = mMessages.get(position);
        if (isShowTime(position)) {
            holder.tvTime.setVisibility(View.VISIBLE);
            String format = mFormat.format(new Date(message.getTime() * 1000));
            holder.tvTime.setText(format);
        } else {
            holder.tvTime.setVisibility(View.GONE);
        }

        if (isSelectStatus) {
            holder.cbSelect.setVisibility(View.VISIBLE);
            holder.cbSelect.setChecked(mSelectMessages.contains(message));
        } else {
            holder.cbSelect.setVisibility(View.GONE);
        }

        holder.tvTitle.setText(message.getTitle());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getAdapterPosition();
                if (pos < 0 || pos >= mMessages.size()) {
                    return;
                }
                if (isSelectStatus) {
                    Message msg = mMessages.get(pos);
                    boolean checked = holder.cbSelect.isChecked();
                    holder.cbSelect.setChecked(!checked);
                    if (checked) {
                        mSelectMessages.remove(msg);
                    } else {
                        mSelectMessages.add(msg);
                    }
                } else {
                    if (null != mListener) {
                        mListener.onItemClick(v, mMessages.get(holder.getAdapterPosition()));
                    }
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!isSelectStatus) {
                    mListener.onItemLongClick(v, mMessages.get(holder.getAdapterPosition()));
                    return true;
                }
                return false;
            }
        });
    }

    public void setOnItemClickListener(OnMessageClickListener listener) {
        this.mListener = listener;
    }

    private boolean isShowTime(int position) {
        if (position == 0) {
            return true;
        }

        String time = mFormat.format(new Date(mMessages.get(position).getTime() * 1000));
        String preTime = mFormat.format(new Date(mMessages.get(position - 1).getTime() * 1000));

        if (!TextUtils.equals(time, preTime)) {
            return true;
        }

        return false;
    }

    @Override
    public int getItemCount() {
        return null == mMessages ? 0 : mMessages.size();
    }

    public interface OnMessageClickListener {

        void onItemClick(View view, Message message);

        void onItemLongClick(View view, Message message);

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tv_time)
        TextView tvTime;
        @Bind(R.id.cb_select)
        CheckBox cbSelect;
        @Bind(R.id.tv_title)
        TextView tvTitle;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
