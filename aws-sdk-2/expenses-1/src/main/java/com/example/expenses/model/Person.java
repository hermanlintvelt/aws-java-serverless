package com.example.expenses.model;

public class Person {
    protected final String email;

    public Person(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Person person = (Person) o;

        return email.equals(person.email);
    }

    @Override
    public int hashCode() {
        return email.hashCode();
    }
}