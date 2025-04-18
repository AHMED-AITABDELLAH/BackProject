package com.savorybites.backend.api.publics;

import com.savorybites.backend.model.addressDetailModels.Commune;
import com.savorybites.backend.model.addressDetailModels.Province;
import com.savorybites.backend.model.addressDetailModels.Region;
import com.savorybites.backend.service.AddressService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/public/addressDetails")
public class AddressDetailsController {


    private final AddressService addressService;

    public AddressDetailsController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("/Regions")
    public List<Region> regions(){
        return addressService.getRegions();
    }

    @GetMapping("/Provinces")
    public List<Province> provinces(@RequestParam long regionID){
        return addressService.getProvinces(regionID);
    }

    @GetMapping("/Communes")
    public List<Commune> Commune(@RequestParam long provinceID){
        return addressService.getCommunes(provinceID);
    }
}
