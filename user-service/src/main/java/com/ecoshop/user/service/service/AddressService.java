package com.ecoshop.user.service.service;

import com.ecoshop.common.exception.BusinessException;
import com.ecoshop.user.service.domain.Address;
import com.ecoshop.user.service.domain.User;
import com.ecoshop.user.service.dto.UserDtos.*;
import com.ecoshop.user.service.repo.AddressRepository;
import com.ecoshop.user.service.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AddressResponse> listAddresses(UUID userId) {
        return addressRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public AddressResponse createAddress(UUID userId, AddressRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("USER_NOT_FOUND", "User not found"));

        if (req.isDefault()) {
            // Unset existing default
            addressRepository.findByUserId(userId).forEach(a -> a.setDefault(false));
        }

        Address address = Address.builder()
                .user(user)
                .label(req.label())
                .recipientName(req.recipientName())
                .phone(req.phone())
                .line1(req.line1())
                .line2(req.line2())
                .city(req.city())
                .state(req.state())
                .postalCode(req.postalCode())
                .country(req.country() != null ? req.country() : "IN")
                .isDefault(req.isDefault())
                .build();
        address = addressRepository.save(address);
        return toResponse(address);
    }

    @Transactional
    public AddressResponse updateAddress(UUID userId, UUID addressId, AddressRequest req) {
        Address address = findAndAssertOwner(userId, addressId);

        if (req.isDefault() && !address.isDefault()) {
            addressRepository.findByUserId(userId).forEach(a -> a.setDefault(false));
        }

        address.setLabel(req.label());
        address.setRecipientName(req.recipientName());
        address.setPhone(req.phone());
        address.setLine1(req.line1());
        address.setLine2(req.line2());
        address.setCity(req.city());
        address.setState(req.state());
        address.setPostalCode(req.postalCode());
        if (req.country() != null) address.setCountry(req.country());
        address.setDefault(req.isDefault());
        return toResponse(address);
    }

    @Transactional
    public void deleteAddress(UUID userId, UUID addressId) {
        Address address = findAndAssertOwner(userId, addressId);
        addressRepository.delete(address);
    }

    private Address findAndAssertOwner(UUID userId, UUID addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> BusinessException.notFound("ADDRESS_NOT_FOUND", "Address not found"));
        if (!address.getUser().getId().equals(userId)) {
            throw BusinessException.badRequest("FORBIDDEN", "Not your address");
        }
        return address;
    }

    public AddressResponse toResponse(Address a) {
        return new AddressResponse(
                a.getId(), a.getLabel(), a.getRecipientName(), a.getPhone(),
                a.getLine1(), a.getLine2(), a.getCity(), a.getState(),
                a.getPostalCode(), a.getCountry(), a.isDefault()
        );
    }
}
