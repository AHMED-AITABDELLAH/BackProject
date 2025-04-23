package com.savorybites.backend.api.dto.address;


import com.savorybites.backend.model.Address;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddressShortDTO {
    private long id;
    private String title;
    private String region;
    private String province;
    private String commune;



    public AddressShortDTO(Address address){
        this.id=address.getId();
        this.title= address.getTitle();
        this.region=address.getRegion().getRegionName();
        this.province=address.getProvince().getProvinceName();
        this.region=address.getCommune().getCommuneName();
    }
}
