package model;

import androidx.annotation.NonNull;

public class UserModel {
    private String firstName;
    private String lastName;
    private Integer credit;

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Integer getCredit() {
        return credit;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setCredit(Integer credit) {
        this.credit = credit;
    }

    @NonNull
    @Override
    public String toString() {
        return firstName + " " + lastName + " Credits: " + credit;
    }
}
