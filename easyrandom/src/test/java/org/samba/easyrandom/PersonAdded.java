package org.samba.easyrandom;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Objects;

@EqualsAndHashCode
@ToString
public class PersonAdded {

    private String firstName;
    private String lastName;
    private String nickName = "the boss";

    public PersonAdded() {
    }

    private PersonAdded(String firstName, String lastName,
                        String nickName) {
        this.firstName = Objects.requireNonNull(firstName, "firstname should not be null");
        this.lastName = Objects.requireNonNull(lastName, "lastName is marked non-null but is null");
        this.nickName = Objects.requireNonNull(nickName, "nickName is marked non-null but is null");
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getNickName() {
        return nickName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String firstName;
        private String lastName;
        private String nickName;

        private Builder() {
            this.nickName = "the boss";
        }

        public PersonAdded build() {
            return new PersonAdded(
                    this.firstName,
                    this.lastName,
                    this.nickName);
        }

        public Builder firstName(String value) {
            this.firstName = value;
            return this;
        }

        public Builder lastName(String value) {
            this.lastName = value;
            return this;
        }

        public Builder nickName(String value) {
            this.nickName = value;
            return this;
        }
    }
}
