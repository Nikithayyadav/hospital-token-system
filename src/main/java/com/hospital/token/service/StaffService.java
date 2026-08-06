package com.hospital.token.service;

import com.hospital.token.model.Staff;
import com.hospital.token.repository.StaffRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StaffService {

    private final StaffRepository staffRepository;

    public StaffService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    public Staff authenticate(String username, String password) {

        Optional<Staff> staffOptional =
                staffRepository.findByUsername(username);

        if (staffOptional.isPresent()) {

            Staff staff = staffOptional.get();

            if (staff.getPassword().equals(password)
                    && staff.isActive()) {

                return staff;
            }
        }

        return null;
    }
}