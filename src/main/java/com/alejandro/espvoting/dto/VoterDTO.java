package com.alejandro.espvoting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link com.alejandro.espvoting.model.Voter}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoterDTO implements Serializable {
    private Long id;
    private String firstName;
    private String lastName;
    private String identificationNumber;
    private LocalDate dateOfBirth;
    private String sex;
    private String email;
    private String phoneNumber;
    private Boolean isActive;
    private Long regionId;
    private String regionName;
    private Long districtId;
    private String districtName;
}