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
package com.github.anhle.contactloader.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.github.anhle.contactloader.model.ContactModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by AnhLe on 5/11/17.
 */

public class ContactLoader {
    private static final String TAG = ContactLoader.class.getSimpleName();
    private static final Uri QUERY_CONTACT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
    private static final String SORT_ORDER = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

    private static ContactLoader instance;

    private Context mContext;

    // LinkedHashMap for speedup searching time when checking duplicate contact.
    private final LinkedHashMap<String, ContactModel> mDeviceContactsMap = new LinkedHashMap<>();
    private final ArrayList<ContactModel> mDeviceContactsList = new ArrayList<>();


    public static ContactLoader getInstance(Context ctx) {
        if (instance == null) {
            initialize(ctx);
        }
        return instance;
    }
    public static void initialize(Context context) {
        if (instance == null) {
            instance = new ContactLoader(context);
            synchronized (ContactLoader.class) {
                instance.getContext().getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, instance.mObserver);
            }
        }

    }

    public ContactLoader(Context context) {
        this.mContext = context;
        startSyncContact();
    }

    public Context getContext() {
        return mContext;
    }

    private final ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            startSyncContact();
        }
    };

    public ArrayList<ContactModel> getDeviceContactsList() {
        if (mDeviceContactsList.size() == 0) {
            startSyncContact();
        }
        return mDeviceContactsList;
    }

    public void startSyncContactIfNeed() {
        if (mDeviceContactsList.size() == 0) {
            startSyncContact();
        }
    }

    public void startSyncContact() {
        // Check permission first!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        // Permission was Granted so load OK!
        mDeviceContactsMap.clear();
        mDeviceContactsList.clear();

        Cursor cursor = mContext.getContentResolver().query(
                QUERY_CONTACT_URI,
                new String[]{
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
                },
                null,
                null,
                SORT_ORDER
        );

        Log.d(TAG, "START LOAD DEVICE CONTACT HERE!");
        long t = System.currentTimeMillis();
        if (cursor != null && !cursor.isClosed() && cursor.moveToFirst()) {
            try {
                do {
                    String phoneNumber = cursor.getString(0);
                    String name = cursor.getString(1);
                    String avatarUrl = cursor.getString(2);

                    // SOS phone number has 3 characters (911, 101, 114,...) so it should be valid.
                    if (phoneNumber == null || "".equals(phoneNumber) == true || phoneNumber.length() < 3) continue;


                    // One people maybe has many phone number so we group them at one contact name.
                    if (mDeviceContactsMap.containsKey(name)) {
                        ContactModel existContact = mDeviceContactsMap.get(name);
                        String phoneNumbers = existContact.getPhone() + ", " + phoneNumber;
                        existContact.setPhone(phoneNumbers);

                        // Replace avatar uri if the existing contact has no avatar
                        if (avatarUrl != null && existContact.getAvatar() == null) {
                            existContact.setAvatar(avatarUrl);
                        }

                        // Update existing contact with new adding phone number
                        mDeviceContactsMap.put(name, existContact);
                    } else {
                        ContactModel newContact = new ContactModel();
                        newContact.setName(name);
                        newContact.setPhone(phoneNumber);
                        newContact.setAvatar(avatarUrl);
                        mDeviceContactsMap.put(name, newContact);
                    }
                } while (cursor.moveToNext());
            } finally {
                cursor.close();
            }
        }
        t = System.currentTimeMillis() - t;

        // Just log for testing.
        int i = 0;
        for (Map.Entry<String, ContactModel> contact : mDeviceContactsMap.entrySet()) {
            mDeviceContactsList.add(contact.getValue());
            //Log.d(TAG, String.format("%d | Name = %s | Phone = %s | Avatar = %s", i++, contact.getKey(), contact.getValue().getPhone(), contact.getValue().getAvatar()));
        }

        Log.d(TAG, "END LOAD DEVICE CONTACT WIDTH: " + mDeviceContactsMap.size() + " Contact(s)! | In total: " + t + " ms");
    }
}