package com.savorybites.backend.api.dto.cart;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class OrderDetailsDescription {

    private List<ItemDetails> itemDetails;
    private double subTotal;
    private double shipping;
    private double total;

    public void add(ItemDetails item){
        itemDetails.add(item);
    }

    public void setSubTotal(){
        double sum=0.0;
        for (ItemDetails item:itemDetails)
            sum+= item.getTotal();
        subTotal=sum;
    }

    public void setTotal(){
        total=subTotal+shipping;
    }

}
