package com.vend.Entity;

import com.vend.Dto.SignUpDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Created by makha on 28/06/2018.
 */

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    //@Column(name = "first_name")
    String name;
    String email;
    String password;
    String contactNumber;
    Boolean verifiedUser;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", verifiedUser=" + verifiedUser +
                ", conatctNumber = "+ contactNumber +
                '}';
    }


    public User (SignUpDto signUpDto) {
        this.setContactNumber(signUpDto.getContactNumber());
        this.setEmail(signUpDto.getEmail());
        this.setName(signUpDto.getName());
        String password = "";
        try {
            password = BCrypt.hashpw(signUpDto.getPassword(), BCrypt.gensalt(12));
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.setPassword(password);
        this.setVerifiedUser(false);
    }
}