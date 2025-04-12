package com.savorybites.backend.api.dto.address;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AddressRegistrationBody

{

    private String Street;
    private long regionID;
    private long provinceID;
    private long communeID;
    private Double latitude;
    private Double longitude;
    private boolean isDefault;
    private boolean isCoordinationActivated;

}

