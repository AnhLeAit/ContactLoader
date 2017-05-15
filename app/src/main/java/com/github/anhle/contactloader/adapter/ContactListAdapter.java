/*
 * Copyright (C) 2017 by Lê Ngọc Anh(Tom Le) - Email: anhle.ait@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.anhle.contactloader.adapter;

import android.net.Uri;
import android.support.v7.util.SortedList;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.anhle.contactloader.R;
import com.github.anhle.contactloader.model.ContactModel;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by AnhLe on 5/8/17.
 */

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ViewHolder> {

    private Set<String> selectedContacts = new HashSet<>();
    private OnSelectContactListener listener;

    SortedList<ContactModel> contactsList = new SortedList<ContactModel>(ContactModel.class, new SortedList.Callback<ContactModel>() {

        @Override
        public int compare(ContactModel o1, ContactModel o2) {
            return o1.getName().compareTo(o2.getName());
        }

        @Override
        public void onInserted(int position, int count) {
            notifyItemRangeInserted(position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            notifyItemRangeRemoved(position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onChanged(int position, int count) {
            notifyItemRangeChanged(position, count);
        }

        @Override
        public boolean areContentsTheSame(ContactModel oldItem, ContactModel newItem) {
            // return whether the items' visual representations are the same or not.
            return oldItem.getName().equals(newItem.getName());
        }

        @Override
        public boolean areItemsTheSame(ContactModel item1, ContactModel item2) {
            return item1.getName() == item2.getName();
        }
    });

    public ContactListAdapter(ArrayList<ContactModel> contactsList, Set<String> selectedContacts, OnSelectContactListener listener) {
        this.contactsList.clear();
        this.contactsList.addAll(contactsList);
        this.listener = listener;
        this.selectedContacts.clear();
        this.selectedContacts.addAll(selectedContacts);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_list, parent, false);
        return new ContactListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ContactModel contact = contactsList.get(position);
        boolean isSelected = false;
        if (contact != null && contact.getName() != null) {
            isSelected = selectedContacts.contains(contact.getName());
        }
        holder.bind(contact, isSelected, listener);
    }

    @Override
    public int getItemCount() {
        return contactsList.size();
    }

    public char getSectionCharacterAt(int position) {
        if (position >= 0 && position < getItemCount()) {
            return contactsList.get(position).getName().toUpperCase().charAt(0);
        }
        return " ".charAt(0);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_contact_list_cb_select)
        AppCompatCheckBox cbSelect;

        @BindView(R.id.item_contact_list_riv_avatar)
        RoundedImageView rivAvatar;

        @BindView(R.id.item_contact_list_tv_name)
        TextView tvName;

        @BindView(R.id.item_contact_list_tv_phone)
        TextView tvPhone;

        int avatarSize = 0;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            avatarSize = rivAvatar.getContext().getResources().getDimensionPixelOffset(R.dimen.avatar_size_normal);
        }

        public void bind(final ContactModel contactModel, boolean isSelected, final OnSelectContactListener listener) {
            cbSelect.setChecked(isSelected);
            tvName.setText(contactModel.getName());
            tvPhone.setText(contactModel.getPhone());

            if (contactModel.getAvatar() != null) {
                Glide.with(rivAvatar.getContext())
                        .load(Uri.parse(contactModel.getAvatar()))
                        .centerCrop()
                        .override(avatarSize, avatarSize)
                        .into(rivAvatar);
            } else {
                rivAvatar.setImageResource(R.mipmap.ic_default_avatar);
            }


            cbSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((CheckBox) v).isChecked()) {
                        if (listener != null) {
                            listener.onSelectContact(contactModel);
                        }
                        selectedContacts.add(contactModel.getName());
                    } else {
                        if (listener != null) {
                            listener.onUnSelectContact(contactModel);
                        }
                        selectedContacts.remove(contactModel.getName());
                    }
                }
            });
        }
    }

    public int add(ContactModel item) {
        return contactsList.add(item);
    }

    public boolean remove(ContactModel item) {
        return contactsList.remove(item);
    }

    public void updateItemAt(int index, ContactModel item) {
        contactsList.updateItemAt(index, item);
    }

    /**
     * Do search and update list right here!
     *
     * @param searchList
     * @param searchKey
     * @return Result size.
     */
    public int doSearch(ArrayList<ContactModel> searchList, String searchKey) {
        contactsList.beginBatchedUpdates();

        if (searchKey == null || "".equals(searchKey)) {
            contactsList.addAll(searchList);
        } else {
            // Do search now!
            String searchKeyUppercase = searchKey.toUpperCase();

            // Search for remove item inside current list result!
            for (int i = contactsList.size() - 1; i >= 0; i--) {
                if (!contactsList.get(i).getName().toUpperCase().contains(searchKeyUppercase)) {
                    contactsList.removeItemAt(i);
                }
            }

            // Search for adding items!
            for (ContactModel c : searchList) {
                if (c.getName().toUpperCase().contains(searchKeyUppercase)) {
                    // Just add result do not exist inside current list result.
                    if (contactsList.indexOf(c) == SortedList.INVALID_POSITION) {
                        contactsList.add(c);
                    }
                }
            }

        }
        contactsList.endBatchedUpdates();

        return contactsList.size();
    }

    public static interface OnSelectContactListener {
        void onSelectContact(ContactModel contact);
        void onUnSelectContact(ContactModel contact);
    }
}
