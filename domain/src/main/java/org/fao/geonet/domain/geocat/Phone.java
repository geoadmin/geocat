package org.fao.geonet.domain.geocat;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Represents the phone numbers.
 *
 * User: Jesse
 * Date: 11/15/13
 * Time: 11:12 AM
 */
@Embeddable
public class Phone implements Serializable {
    private String phone;
    private String facsimile;
    private String mobile;
    private String directnumber;


    public String getPhone() {
        return phone;
    }

    public Phone setPhone(String phone) {
        this.phone = phone;
        return this;
    }


    public String getFacsimile() {
        return facsimile;
    }

    public Phone setFacsimile(String facsimile) {
        this.facsimile = facsimile;
        return this;
    }


    public String getMobile() {
        return mobile;
    }

    public Phone setMobile(String mobile) {
        this.mobile = mobile;
        return this;
    }

    public String getDirectnumber() {
        return directnumber;
    }

    public Phone setDirectnumber(String directnumber) {
        this.directnumber = directnumber;
        return this;
    }

}
