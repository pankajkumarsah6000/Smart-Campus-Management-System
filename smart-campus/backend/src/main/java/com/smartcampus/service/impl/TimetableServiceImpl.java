package com.smartcampus.service.impl;

import com.smartcampus.dto.request.TimetableSlotRequest;
import com.smartcampus.dto.response.TimetableSlotDto;
import com.smartcampus.entity.Faculty;
import com.smartcampus.entity.Student;
import com.smartcampus.entity.Subject;
import com.smartcampus.entity.Timetable;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.FacultyRepository;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.repository.SubjectRepository;
import com.smartcampus.repository.TimetableRepository;
import com.smartcampus.service.TimetableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimetableServiceImpl implements TimetableService {

    private final TimetableRepository timetableRepository;
    private final SubjectRepository subjectRepository;
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;

    @Override
    @Transactional
    public TimetableSlotDto createSlot(TimetableSlotRequest request) {
        validateTimes(request.getStartTime(), request.getEndTime());
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + request.getSubjectId()));

        Long facultyId = subject.getFaculty() != null ? subject.getFaculty().getId() : null;
        checkConflicts(request, facultyId, null);

        Timetable slot = new Timetable();
        apply(slot, request, subject);
        return toDto(timetableRepository.save(slot));
    }

    @Override
    @Transactional
    public TimetableSlotDto updateSlot(Long id, TimetableSlotRequest request) {
        validateTimes(request.getStartTime(), request.getEndTime());
        Timetable slot = timetableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable slot not found with id: " + id));
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + request.getSubjectId()));

        Long facultyId = subject.getFaculty() != null ? subject.getFaculty().getId() : null;
        checkConflicts(request, facultyId, id);

        apply(slot, request, subject);
        return toDto(timetableRepository.save(slot));
    }

    @Override
    @Transactional
    public void deleteSlot(Long id) {
        Timetable slot = timetableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable slot not found with id: " + id));
        timetableRepository.delete(slot);
    }

    @Override
    public List<TimetableSlotDto> getTimetable(String section, String semester) {
        return timetableRepository.findBySectionAndSemester(section, semester).stream()
                .sorted(slotComparator())
                .map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<TimetableSlotDto> getMyTimetable(Long userId) {
        if (studentRepository.findByUserId(userId).isPresent()) {
            Student student = studentRepository.findByUserId(userId).orElseThrow();
            return getTimetableForStudent(student.getId());
        }
        if (facultyRepository.findByUserId(userId).isPresent()) {
            return getTimetableForFaculty(userId);
        }
        // Admin: full weekly schedule
        return timetableRepository.findAll().stream()
                .sorted(slotComparator())
                .map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<TimetableSlotDto> getTimetableForStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        return timetableRepository.findBySectionAndSemester(student.getSection(), student.getSemester()).stream()
                .sorted(slotComparator())
                .map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<TimetableSlotDto> getTimetableForFaculty(Long facultyUserId) {
        Faculty faculty = facultyRepository.findByUserId(facultyUserId)
                .orElseThrow(() -> new ResourceNotFoundException("No faculty profile linked to this account"));
        return timetableRepository.findByFacultyId(faculty.getId()).stream()
                .sorted(slotComparator())
                .map(this::toDto).collect(Collectors.toList());
    }

    private void validateTimes(LocalTime start, LocalTime end) {
        if (end.isBefore(start) || end.equals(start)) {
            throw new BadRequestException("End time must be after start time");
        }
    }

    private void checkConflicts(TimetableSlotRequest request, Long facultyId, Long excludeId) {
        List<Timetable> all = timetableRepository.findAll();
        for (Timetable t : all) {
            if (excludeId != null && t.getId().equals(excludeId)) continue;
            if (t.getDayOfWeek() != request.getDayOfWeek()) continue;
            if (!overlaps(t, request.getStartTime(), request.getEndTime())) continue;

            boolean sameSection = request.getSection().equalsIgnoreCase(t.getSection())
                    && request.getSemester().equalsIgnoreCase(t.getSemester());
            boolean sameRoom = request.getRoom() != null && t.getRoom() != null
                    && request.getRoom().equalsIgnoreCase(t.getRoom());
            boolean sameFaculty = facultyId != null && t.getFaculty() != null
                    && t.getFaculty().getId().equals(facultyId);

            if (sameSection) {
                throw new BadRequestException(
                        "Timetable conflict: section " + request.getSection() + " already has a class at this time on " + request.getDayOfWeek());
            }
            if (sameRoom) {
                throw new BadRequestException(
                        "Timetable conflict: room " + request.getRoom() + " is already booked at this time on " + request.getDayOfWeek());
            }
            if (sameFaculty) {
                throw new BadRequestException(
                        "Timetable conflict: the assigned faculty already has a class at this time on " + request.getDayOfWeek());
            }
        }
    }

    private boolean overlaps(Timetable t, LocalTime start, LocalTime end) {
        return t.getStartTime().isBefore(end) && t.getEndTime().isAfter(start);
    }

    private void apply(Timetable slot, TimetableSlotRequest request, Subject subject) {
        slot.setSubject(subject);
        slot.setFaculty(subject.getFaculty());
        slot.setSection(request.getSection());
        slot.setSemester(request.getSemester());
        slot.setDayOfWeek(request.getDayOfWeek());
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setRoom(request.getRoom());
    }

    private Comparator<Timetable> slotComparator() {
        return Comparator
                .comparingInt((Timetable t) -> t.getDayOfWeek().getValue())
                .thenComparing(Timetable::getStartTime);
    }

    private TimetableSlotDto toDto(Timetable t) {
        TimetableSlotDto dto = new TimetableSlotDto();
        dto.setId(t.getId());
        dto.setSubjectName(t.getSubject().getName());
        dto.setFacultyName(t.getFaculty() != null
                ? t.getFaculty().getUser().getFirstName() + " " + t.getFaculty().getUser().getLastName() : null);
        dto.setDayOfWeek(t.getDayOfWeek().name());
        dto.setStartTime(t.getStartTime().toString());
        dto.setEndTime(t.getEndTime().toString());
        dto.setRoom(t.getRoom());
        return dto;
    }
}