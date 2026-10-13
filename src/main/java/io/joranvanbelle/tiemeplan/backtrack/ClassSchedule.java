package io.joranvanbelle.tiemeplan.backtrack;


import io.joranvanbelle.tiemeplan.variables.Classroom;
import io.joranvanbelle.tiemeplan.variables.Course;
import io.joranvanbelle.tiemeplan.variables.StudentGroup;
import io.joranvanbelle.tiemeplan.variables.Subject;
import io.joranvanbelle.tiemeplan.variables.Teacher;
import io.joranvanbelle.tiemeplan.variables.Timeslot;

import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

public class ClassSchedule {

    private final List<ScheduledCourse> assignedCourses;

    private final Map<Classroom, List<Timeslot>> assignedClassRooms;
    private final Map<Teacher, List<Timeslot>> assignedTeachers;
    private final Map<StudentGroup, List<Timeslot>> assignedStudentGroups;

    private ClassSchedule() {
        this.assignedCourses = new ArrayList<>();
        this.assignedClassRooms = new HashMap<>();
        this.assignedTeachers = new HashMap<>();
        this.assignedStudentGroups = new HashMap<>();
    }

    public static ClassSchedule empty() {
        return new ClassSchedule();
    }

    public boolean isValid(Classroom classroom, Timeslot timeslot, Course course) {
        if (isRoomOccupied(classroom, timeslot) || !classroom.canHoldStudentGroup(course.studentGroup().size())) {
            return false;
        }
        if (isTeacherOccupied(timeslot, course)) {
            return false;
        }
        if (isStudentGroupOccupied(timeslot, course)) {
            return false;
        }
        return true;
    }

    private boolean isRoomOccupied(Classroom classroom, Timeslot timeslot) {
        return this.assignedClassRooms.getOrDefault(classroom, Collections.emptyList()).contains(timeslot);
    }

    private boolean isTeacherOccupied(Timeslot timeslot, Course course) {
        return this.assignedTeachers.getOrDefault(course.teacher(), Collections.emptyList()).contains(timeslot);
    }

    private boolean isStudentGroupOccupied(Timeslot timeslot, Course course) {
        return this.assignedStudentGroups.getOrDefault(course.studentGroup(), Collections.emptyList()).contains(timeslot);
    }

    public ClassSchedule assign(Classroom classroom, Timeslot timeslot, Course course) {
        var scheduledCourse = new ScheduledCourse(classroom, timeslot, course);
        
        this.assignedCourses.add(scheduledCourse);
        this.assignedClassRooms.computeIfAbsent(classroom, k -> new ArrayList<>()).add(timeslot);
        this.assignedTeachers.computeIfAbsent(course.teacher(), k -> new ArrayList<>()).add(timeslot);
        this.assignedStudentGroups.computeIfAbsent(course.studentGroup(), k -> new ArrayList<>()).add(timeslot);
        
        return this;
    }
    
    public ClassSchedule unassign(Classroom classroom, Timeslot timeslot, Course course) {
        assignedCourses.removeLast();
        this.assignedClassRooms.get(classroom).remove(timeslot);
        this.assignedTeachers.get(course.teacher()).remove(timeslot);
        this.assignedStudentGroups.get(course.studentGroup()).remove(timeslot);

        return this;
    }

    public List<ScheduledCourse> assignedCourses() {
        return assignedCourses;
    }

    public Map<Classroom, List<Timeslot>> assignedClassRooms() {
        return assignedClassRooms;
    }

    public Map<Teacher, List<Timeslot>> assignedTeachers() {
        return assignedTeachers;
    }

    public Map<StudentGroup, List<Timeslot>> assignedStudentGroups() {
        return assignedStudentGroups;
    }

}
