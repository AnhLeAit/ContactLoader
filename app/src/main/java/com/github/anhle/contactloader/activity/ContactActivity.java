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
package com.github.anhle.contactloader.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.github.anhle.contactloader.R;
import com.github.anhle.contactloader.adapter.ContactListAdapter;
import com.github.anhle.contactloader.adapter.SelectedContactAdapter;
import com.github.anhle.contactloader.base.BaseActivity;
import com.github.anhle.contactloader.utils.ContactLoader;
import com.github.anhle.contactloader.model.ContactModel;
import com.github.anhle.contactloader.widget.FlexibleWidthRecyclerView;
import com.github.anhle.contactloader.widget.RecyclerSectionItemDecoration;
import com.google.gson.Gson;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

import static java.lang.String.format;

public class ContactActivity extends BaseActivity implements ContactListAdapter.OnSelectContactListener {

    public final static String TAG = ContactActivity.class.getSimpleName();
    public final static String KEY_CONTACTS_SELECTED = "selected_contacts";

    @BindView(R.id.activity_contact_toolbar)
    Toolbar toolbar;

    @BindView(R.id.activity_contact_et_search_contacts)
    EditText etSearchBox;

    @BindView(R.id.activity_contact_rc_contacts_list)
    RecyclerView rcContactsList;

    @BindView(R.id.activity_contact_list_selected_contacts)
    FlexibleWidthRecyclerView rcSelectedContactsList;

    private ContactListAdapter mContactListAdapter;
    private DividerItemDecoration mDividerItemDecoration;
    private LinearLayoutManager mContactListLayoutManager;
    private final ArrayList<ContactModel> mDeviceContactsList = new ArrayList<>();
    private final ArrayList<ContactModel> mSelectedContacts = new ArrayList<>();

    private SelectedContactAdapter mSelectedContactAdapter;
    private LinearLayoutManager mSelectedContactListLayoutManager;

    private Disposable mDisposable;

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_contact;
    }

    @Override
    protected void initViewOnCreate() {
        mDeviceContactsList.clear();
        mDeviceContactsList.addAll(ContactLoader.getInstance(this).getDeviceContactsList());

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_select_contacts);

        setupContactList();
        setupSelectedContactList();
        setupSearchContact();

    }

    private void setupContactList() {
        mContactListLayoutManager = new LinearLayoutManager(this);
        rcContactsList.setLayoutManager(mContactListLayoutManager);
        rcContactsList.setHasFixedSize(true);

        // Fill selected contact from MainActivity into contact list.
        ArrayList<ContactModel> selectedContacts = (ArrayList<ContactModel>) getIntent().getSerializableExtra(ContactActivity.KEY_CONTACTS_SELECTED);
        if (selectedContacts != null && selectedContacts.size() > 0) {
            mSelectedContacts.clear();
            mSelectedContacts.addAll(selectedContacts);
        }
        HashSet<String> contactModelHashSet = new HashSet<>();
        for (ContactModel c : mSelectedContacts) {
            contactModelHashSet.add(c.getName());
        }
        mContactListAdapter = new ContactListAdapter(mDeviceContactsList, contactModelHashSet, this);
        rcContactsList.setAdapter(mContactListAdapter);

        mDividerItemDecoration = new DividerItemDecoration(this, mContactListLayoutManager.getOrientation());
        mDividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.line_divider));
        rcContactsList.addItemDecoration(mDividerItemDecoration);

        RecyclerSectionItemDecoration sectionItemDecoration =
                new RecyclerSectionItemDecoration(getResources().getDimensionPixelSize(R.dimen.recycler_section_header_height),
                        true, // true for sticky, false for not
                        new RecyclerSectionItemDecoration.SectionCallback() {
                            @Override
                            public boolean isSection(int position) {
                                if (position < 0) {
                                    return false;
                                }
                                return position == 0
                                        || mContactListAdapter.getSectionCharacterAt(position)
                                        != mContactListAdapter.getSectionCharacterAt(position - 1);
                            }

                            @Override
                            public CharSequence getSectionHeader(int position) {
                                if (position < 0) {
                                    return "";
                                }
                                return mContactListAdapter.getSectionCharacterAt(position) + "";
                            }
                        });
        rcContactsList.addItemDecoration(sectionItemDecoration);
    }

    private void setupSelectedContactList() {
        mSelectedContactListLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rcSelectedContactsList.setLayoutManager(mSelectedContactListLayoutManager);
        rcSelectedContactsList.setHasFixedSize(true);

        mSelectedContactAdapter = new SelectedContactAdapter(mSelectedContacts);
        rcSelectedContactsList.setAdapter(mSelectedContactAdapter);
    }

    private void setupSearchContact() {
        mDisposable = RxTextView.textChangeEvents(etSearchBox)
                .debounce(300, TimeUnit.MILLISECONDS) // default Scheduler is Computation
                .filter(changes -> (changes.text().toString() != null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getSearchObserver());
    }

    // -----------------------------------------------------------------------------------
    // Main Rx entities

    private DisposableObserver<TextViewTextChangeEvent> getSearchObserver() {
        return new DisposableObserver<TextViewTextChangeEvent>() {
            @Override
            public void onComplete() {
                Log.d(TAG, "Search --------- onComplete");
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "Search --------- Oops on error!");
            }

            @Override
            public void onNext(TextViewTextChangeEvent onTextChangeEvent) {
                String searchKey = onTextChangeEvent.text().toString();
                int resultSize = mContactListAdapter.doSearch(mDeviceContactsList, searchKey);
                if (resultSize > 0) {
                    rcContactsList.setVisibility(View.VISIBLE);
                } else {
                    // Show No result!
                    rcContactsList.setVisibility(View.GONE);
                }

                Log.d(TAG, format("Searching for '%s' | resultSize = %d", searchKey, resultSize));
            }
        };
    }

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contact_screen, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
            case R.id.action_done:
                Intent resultIntent = new Intent();
                resultIntent.putExtra(KEY_CONTACTS_SELECTED, mSelectedContacts);
                setResult(Activity.RESULT_OK, resultIntent);
                finishActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onBackPressed() {
        finishActivity();
    }

    @Override
    public void onSelectContact(ContactModel contact) {
        mSelectedContacts.add(contact);
        mSelectedContactAdapter.addContact(contact);
        Log.d(TAG, String.format("onSelectContact --> " + (new Gson().toJson(contact))));

        // Let FlexibleWidthRecyclerView calculate width again by #onMeasure()
        rcSelectedContactsList.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    rcSelectedContactsList.requestLayout();
                } catch (Exception ex) {

                }
            }
        }, 250);
    }

    @Override
    public void onUnSelectContact(ContactModel contact) {
        mSelectedContacts.remove(contact);
        mSelectedContactAdapter.removeContact(contact);
        Log.d(TAG, String.format("onUnSelectContact --> " + (new Gson().toJson(contact))));

        // Let FlexibleWidthRecyclerView calculate width again by #onMeasure()
        rcSelectedContactsList.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    rcSelectedContactsList.requestLayout();
                } catch (Exception ex) {

                }
            }
        }, 250);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDisposable.dispose();
    }
}
