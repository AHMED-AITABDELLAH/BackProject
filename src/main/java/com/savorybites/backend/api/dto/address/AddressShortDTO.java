package com.savorybites.backend.api.dto.address;


import com.savorybites.backend.model.Address;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddressShortDTO {
    private long id;
    private String title;



    public AddressShortDTO(Address address){
        this.id=address.getId();
        this.title= address.getTitle();
    }
}
