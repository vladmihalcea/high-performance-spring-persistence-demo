package com.vladmihalcea.spring.demo.domain.jdbctemplate;

/**
 * @author Vlad Mihalcea
 */
public class User {

    private String id;

    private String firstName;

    private String lastName;

    public String getId() {
        return id;
    }

    public User setId(String id) {
        this.id = id;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public User setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public User setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }
}
