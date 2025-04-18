package com.savorybites.backend.api.dto.admin;

import com.savorybites.backend.api.dto.Order.OrderDTO;
import com.savorybites.backend.api.dto.user.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserStatistics {

    private long totalUserCount;
    private long bannedUserCount;
    private long verifiedUserCount;
    private List<UserDTO> topSpendingUsers;

}
