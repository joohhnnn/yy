package com.example.week1;

import android.os.Parcel;
import android.os.Parcelable;

public class Person implements Parcelable {
    private String id;
    private String name;
    private String phone;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phone=" + phone +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(phone);

    }
    public static final Parcelable.Creator<Person> CREATOR =new Creator<Person>() {
        @Override
        public Person createFromParcel(Parcel source){
            Person person = new Person();
            person.id=source.readString();
            person.name=source.readString();
            person.phone=source.readString();
            return person;
        }
        @Override
        public Person[] newArray(int size){
            return new Person[size];
        }



    };


}
