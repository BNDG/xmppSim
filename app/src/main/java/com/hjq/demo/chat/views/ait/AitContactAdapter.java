// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.hjq.demo.chat.views.ait;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hjq.demo.R;
import com.hjq.demo.chat.model.ait.AitUserInfo;
import com.hjq.demo.chat.utils.AvatarGenerator;
import com.hjq.demo.utils.Trace;

import java.util.ArrayList;
import java.util.List;

/** Team member @ adapter */
public class AitContactAdapter extends RecyclerView.Adapter<AitContactAdapter.AitContactHolder> {
  private Context mContext;
  private List<AitUserInfo> members = new ArrayList<>();
  //@所有人的特殊类型
  private static final int SHOW_ALL_TYPE = 101;

  private OnItemListener listener;


  private boolean showAll = true;

  public AitContactAdapter(Context context) {
    this.mContext = context;
  }

    public void setMembers(List<AitUserInfo> userInfoWithTeams) {
    this.members.clear();
    this.members.addAll(userInfoWithTeams);
  }

  public void addMembers(List<AitUserInfo> userInfoWithTeams) {
    this.members.addAll(userInfoWithTeams);
  }

  public void setShowAll(boolean showAll) {
    this.showAll = showAll;
  }

  public void setOnItemListener(OnItemListener listener) {
    this.listener = listener;
  }

  @NonNull
  @Override
  public AitContactHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new AitContactHolder(LayoutInflater.from(mContext).inflate(R.layout.item_ait_contact, parent, false));
  }

  @Override
  public int getItemViewType(int position) {
    if (showAll && position == 0) {
      return SHOW_ALL_TYPE;
    }
    return super.getItemViewType(position);
  }

  @Override
  public void onBindViewHolder(@NonNull AitContactHolder holder, int position) {

    int dataPosition = position;
    if (showAll) {
      if (position == 0) {
        holder.contactName.setText(R.string.chat_team_ait_all);
        Glide.with(mContext)
                        .load(R.drawable.ic_team_all)
                                .into(holder.contactHeader);
        holder.rootView.setOnClickListener(
                v -> {
                  if (listener != null) {
                    listener.onSelect(null);
                  }
                });
        return;
      }
      dataPosition = position - 1;
    }

    AitUserInfo member = members.get(dataPosition);
    if (member == null) {
      return;
    }
    String showName = member.getNickname();
    holder.contactName.setText(showName);
    AvatarGenerator.loadAvatar(mContext, member.getUserJid(), member.getNickname(), holder.contactHeader, false);
    holder.rootView.setOnClickListener(
            v -> {
              Trace.d("onBindViewHolder: click");
              if (listener != null) {
                listener.onSelect(member);
              }
            });
  }

  @Override
  public int getItemCount() {
    // add ait all
    if (members == null || members.isEmpty()) {
      return 0;
    }

    return showAll ? members.size() + 1 : members.size();
  }

  public static class AitContactHolder extends RecyclerView.ViewHolder {
    public TextView contactName;
    public ImageView contactHeader;
    public View rootView;

    public AitContactHolder(@NonNull View binding) {
      super(binding);
      rootView = binding;
      contactName = binding.findViewById(R.id.contact_name);
      contactHeader = binding.findViewById(R.id.contact_header);
    }
  }

  public interface OnItemListener {
    /** @param item null: @All */
    void onSelect(AitUserInfo item);
  }

}
