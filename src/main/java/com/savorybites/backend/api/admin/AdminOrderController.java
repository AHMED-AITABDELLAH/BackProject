package com.savorybites.backend.api.admin;

import com.savorybites.backend.api.dto.Order.OrderDTO;
import com.savorybites.backend.model.Admin;
import com.savorybites.backend.model.OrderStatus;
import com.savorybites.backend.model.Orders;
import com.savorybites.backend.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final AdminService adminService;

    public AdminOrderController(AdminService adminService) {
        this.adminService = adminService;
    }


    @GetMapping("/")
    public List<OrderDTO> getAllOrders(@AuthenticationPrincipal Admin admin,
                                       @RequestParam(required = false) OrderStatus orderStatus){
        List<Orders> ordersList;
        if(orderStatus==null)
            ordersList = adminService.getAllOrders(admin);
        else
            ordersList = adminService.getAllOrdersByStatus(admin,orderStatus);
        return ordersList.stream().map(OrderDTO::new).toList();
    }


    @GetMapping("/{orderId}")
    public OrderDTO getOrder(@AuthenticationPrincipal Admin admin,
                                             @PathVariable Long orderId) {
        return new OrderDTO(adminService.getOrder(admin, orderId));
    }


    @GetMapping("/orderStatus")
    public OrderStatus[] getAllOrderStatuses() {
        return OrderStatus.values();
    }

}
