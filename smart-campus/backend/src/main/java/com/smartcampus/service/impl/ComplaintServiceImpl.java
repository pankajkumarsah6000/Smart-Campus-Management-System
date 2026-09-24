package com.smartcampus.service.impl;

import com.smartcampus.dto.request.ComplaintRequest;
import com.smartcampus.dto.request.ComplaintResolutionRequest;
import com.smartcampus.dto.response.ComplaintDto;
import com.smartcampus.entity.Complaint;
import com.smartcampus.entity.User;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.ComplaintRepository;
import com.smartcampus.repository.UserRepository;
import com.smartcampus.service.ComplaintService;
import com.smartcampus.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ComplaintDto raiseComplaint(ComplaintRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Complaint complaint = new Complaint();
        complaint.setRaisedBy(user);
        complaint.setSubject(request.getSubject());
        complaint.setDescription(request.getDescription());
        complaint.setCategory(request.getCategory() != null
                ? Complaint.ComplaintCategory.valueOf(request.getCategory()) : Complaint.ComplaintCategory.OTHER);
        complaint.setStatus(Complaint.ComplaintStatus.OPEN);
        return toDto(complaintRepository.save(complaint));
    }

    @Override
    public List<ComplaintDto> getMyComplaints(Long userId) {
        return complaintRepository.findByRaisedById(userId).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ComplaintDto> getAllComplaints() {
        return complaintRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ComplaintDto resolveComplaint(Long id, ComplaintResolutionRequest request, Long adminUserId) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Complaint.ComplaintStatus newStatus;
        try {
            newStatus = Complaint.ComplaintStatus.valueOf(request.getStatus());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid status: " + request.getStatus());
        }
        if (newStatus == Complaint.ComplaintStatus.OPEN) {
            throw new BadRequestException("Use IN_PROGRESS, RESOLVED or CLOSED when updating a complaint");
        }

        complaint.setStatus(newStatus);
        complaint.setResolutionNote(request.getResolutionNote());
        complaint.setResolvedBy(admin);
        complaint = complaintRepository.save(complaint);

        notificationService.notifyUser(
                complaint.getRaisedBy().getId(),
                "Complaint " + complaint.getStatus().name().toLowerCase().replace('_', ' '),
                "Your complaint \"" + complaint.getSubject() + "\" has been updated to " + complaint.getStatus().name() + ".",
                "COMPLAINT",
                "/my-complaints"
        );

        return toDto(complaint);
    }

    private ComplaintDto toDto(Complaint c) {
        ComplaintDto dto = new ComplaintDto();
        dto.setId(c.getId());
        dto.setRaisedByUserId(c.getRaisedBy().getId());
        dto.setRaisedByName(c.getRaisedBy().getFirstName() + " " + c.getRaisedBy().getLastName());
        dto.setRaisedByRole(c.getRaisedBy().getRoles().stream()
                .findFirst().map(r -> r.getName().name()).orElse(null));
        dto.setSubject(c.getSubject());
        dto.setDescription(c.getDescription());
        dto.setCategory(c.getCategory() == null ? null : c.getCategory().name());
        dto.setStatus(c.getStatus() == null ? null : c.getStatus().name());
        dto.setResolutionNote(c.getResolutionNote());
        if (c.getResolvedBy() != null) {
            dto.setResolvedByUserId(c.getResolvedBy().getId());
            dto.setResolvedByName(c.getResolvedBy().getFirstName() + " " + c.getResolvedBy().getLastName());
        }
        dto.setCreatedAt(c.getCreatedAt());
        return dto;
    }
}