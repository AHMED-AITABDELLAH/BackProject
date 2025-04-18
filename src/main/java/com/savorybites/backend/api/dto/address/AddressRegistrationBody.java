package com.savorybites.backend.api.dto.address;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AddressRegistrationBody

{

    //have all the data needed for adding an address
    private String title;
    private String street;
    private long regionID;
    private long provinceID;
    private long communeID;
    private Double latitude;
    private Double longitude;
    private boolean isDefault;
    private boolean isCoordinationActivated;

}//We got our data to the database using this dto
//Here where we use @RequestBody
//Hope you got the point


