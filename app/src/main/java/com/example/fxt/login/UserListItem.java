package com.example.fxt.login;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class UserListItem {

    public List<id_list> id_list = new ArrayList<>();
    public List<id_list> getId_list() { return id_list;}

    public class id_list{
        @SerializedName("ID") String id;
        @SerializedName("name") String name;
        public String getId(){return id;}
        public String getName(){return name;}
    }
}