package com.smartcampus.controller;

import com.smartcampus.dto.ApiResponse;
import com.smartcampus.dto.request.TimetableSlotRequest;
import com.smartcampus.dto.response.TimetableSlotDto;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.TimetableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timetable")
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TimetableSlotDto>> create(@Valid @RequestBody TimetableSlotRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Timetable slot created", timetableService.createSlot(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TimetableSlotDto>> update(@PathVariable Long id,
                                                                @Valid @RequestBody TimetableSlotRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Timetable slot updated", timetableService.updateSlot(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        timetableService.deleteSlot(id);
        return ResponseEntity.ok(ApiResponse.ok("Timetable slot deleted", null));
    }

    /** Authenticated users may view any section/semester timetable. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TimetableSlotDto>>> bySection(
            @RequestParam String section,
            @RequestParam String semester) {
        return ResponseEntity.ok(ApiResponse.ok(timetableService.getTimetable(section, semester)));
    }

    /** Role-aware: students see their own section, faculty their own slots, admins the full schedule. */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<TimetableSlotDto>>> myTimetable(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(timetableService.getMyTimetable(principal.getId())));
    }
}