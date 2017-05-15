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
import android.content.pm.PackageManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.github.anhle.contactloader.R;
import com.github.anhle.contactloader.base.BaseActivity;
import com.github.anhle.contactloader.model.ContactModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    public final static String TAG = MainActivity.class.getSimpleName();
    public final static int REQUEST_CODE_SELECT_CONTACTS = 1000;


    @BindView(R.id.activity_main_toolbar)
    Toolbar toolbar;

    @BindView(R.id.activity_main_tvContactSelected)
    TextView tvContactSelected;

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViewOnCreate() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);
    }

    @OnClick(R.id.activity_main_btn_goToContacts) void btnGoToContacts() {
        if (askForContactPermission()) {
            Intent contactsIntent = new Intent(this, ContactActivity.class);

            // Fill selected contact to reselect at Contact list screen.
            if (mSelectedContacts.size() > 0) {
                contactsIntent.putExtra(ContactActivity.KEY_CONTACTS_SELECTED, mSelectedContacts);
            }

            // Open contact activity here
            openOtherActivityForResult(contactsIntent, REQUEST_CODE_SELECT_CONTACTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CONTACT: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    btnGoToContacts();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private final ArrayList<ContactModel> mSelectedContacts = new ArrayList<>();
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_SELECT_CONTACTS && data != null) {
            try {
                ArrayList<ContactModel> selectedContacts = (ArrayList<ContactModel>) data.getSerializableExtra(ContactActivity.KEY_CONTACTS_SELECTED);
                if (selectedContacts != null) {
                    mSelectedContacts.clear();
                    mSelectedContacts.addAll(selectedContacts);
                    String names = "";
                    if (mSelectedContacts.size() > 0) {
                        names = mSelectedContacts.get(0).getName();
                        for (int i = 1; i < mSelectedContacts.size(); i++) {
                            names = names + ", " + mSelectedContacts.get(i).getName();
                        }
                    } else {
                        names = getString(R.string.guide_contact_selected_here);
                    }

                    tvContactSelected.setText(names);
                }
            } catch (ClassCastException ex) {
                Log.e(TAG, ex.toString());
            }

        }
    }
}
