package com.example.expenses.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class PersonTest {

    @Test
    @DisplayName("Two people are the same if their emails are the same")
    void samePerson() {
        Person p1 = new Person("person@domain.com");
        Person p2 = new Person("person@domain.com");
        assertThat(p1).isEqualTo(p2);
    }

}