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
package com.github.anhle.contactloader.base;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.anhle.contactloader.R;
import com.github.anhle.contactloader.utils.ContactLoader;

import butterknife.ButterKnife;

/**
 * Created by AnhLe on 5/7/17.
 */

public abstract class BaseActivity extends AppCompatActivity {

    public final static int PERMISSION_REQUEST_CONTACT = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setStatusBarColored();
        }
        setContentView(getResLayoutId());
        ButterKnife.bind(this);
        initViewOnCreate();
    }


    protected abstract int getResLayoutId();

    protected abstract void initViewOnCreate();

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setStatusBarColored() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
    }

    /*********************** CUSTOM ACTIVITY TRANSITION WHEN START & FINISH ***********************/
    public void openOtherActivity(Context ctx, Class<?> activityClazz) {
        openOtherActivity(new Intent(ctx, activityClazz));
    }

    public void openOtherActivity(Intent intent) {
        startActivity(intent);
        overridePendingTransition(R.anim.override_pending_transition_slide_in_right,
                R.anim.override_pending_transition_slide_out_left);
    }


    public void openOtherActivityForResult(Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode);
        overridePendingTransition(R.anim.override_pending_transition_slide_in_right,
                R.anim.override_pending_transition_slide_out_left);
    }

    public void finishActivity() {
        finish();
        overridePendingTransition(R.anim.override_pending_transition_slide_in_left,
                R.anim.override_pending_transition_slide_out_right);
    }

    /********************* END CUSTOM ACTIVITY TRANSITION WHEN START & FINISH *********************/


    /**
     * Ask user for permission and load contact when the access contacts permission has granted.
     *
     * @return True if permission OK
     */
    public boolean askForContactPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Contacts access needed");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setMessage("Please confirm Contacts access");//TODO put real question
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            requestPermissions(new String[] {Manifest.permission.READ_CONTACTS} , PERMISSION_REQUEST_CONTACT);
                        }
                    });
                    builder.show();
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            PERMISSION_REQUEST_CONTACT);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
                return false;

            } else {
                ContactLoader.getInstance(this).startSyncContactIfNeed();
                return true;
            }
        } else {
            ContactLoader.getInstance(this).startSyncContactIfNeed();
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CONTACT: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ContactLoader.getInstance(this).startSyncContactIfNeed();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Toast.makeText(this, "No Permissions", Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
