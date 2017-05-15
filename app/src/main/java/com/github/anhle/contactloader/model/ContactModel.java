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
package com.github.anhle.contactloader.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by AnhLe on 5/8/17.
 */
public class ContactModel implements Parcelable
{

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("phone")
    @Expose
    private String phone;
    @SerializedName("avatar")
    @Expose
    private String avatar;

    public final static Parcelable.Creator<ContactModel> CREATOR = new Creator<ContactModel>() {


        @SuppressWarnings({
                "unchecked"
        })
        public ContactModel createFromParcel(Parcel in) {
            ContactModel instance = new ContactModel();
            instance.name = ((String) in.readValue((String.class.getClassLoader())));
            instance.phone = ((String) in.readValue((String.class.getClassLoader())));
            instance.avatar = ((String) in.readValue((String.class.getClassLoader())));
            return instance;
        }

        public ContactModel[] newArray(int size) {
            return (new ContactModel[size]);
        }

    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(name);
        dest.writeValue(phone);
        dest.writeValue(avatar);
    }

    public int describeContents() {
        return 0;
    }

    /**
     * For remove ContactModel object out of Collection type like ArrayList, HashSet, Vector,....
     */
    public boolean equals(Object o) {
        if (!(o instanceof ContactModel)) {
            return false;
        }

        ContactModel other = (ContactModel) o;
        return (new Gson()).toJson(this).equals((new Gson()).toJson(other));
    }

}