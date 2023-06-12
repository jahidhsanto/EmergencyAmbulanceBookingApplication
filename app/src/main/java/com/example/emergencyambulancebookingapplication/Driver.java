package com.example.emergencyambulancebookingapplication;

public class Driver {

    String fullName, companyName, ambulanceCategory, TOKEN;

    public Driver() {
    }

    public Driver(String fullName, String companyName, String ambulanceCategory, String TOKEN) {
        this.fullName = fullName;
        this.companyName = companyName;
        this.ambulanceCategory = ambulanceCategory;
        this.TOKEN = TOKEN;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getAmbulanceCategory() {
        return ambulanceCategory;
    }

    public void setAmbulanceCategory(String ambulanceCategory) {
        this.ambulanceCategory = ambulanceCategory;
    }

    public String getTOKEN() {
        return TOKEN;
    }

    public void setTOKEN(String TOKEN) {
        this.TOKEN = TOKEN;
    }
}
