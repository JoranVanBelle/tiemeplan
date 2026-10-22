# Screen Specification

**Project:** Tiemeplan  
**Last Updated:** 2026-10-22  
**Status:** Approved for planning

## Screens

### 1. Setup / Configuration (Landing)
**Purpose:** Define problem space (resources, timeslots, people, courses).  
**Layout:** SplitPane (left: resource lists, right: detail forms).  
**User Actions:**
- Add/edit/remove Classrooms (name, capacity, occupied/unavailable timeslots)
- Add/edit/remove Timeslots (DayOfWeek, LocalTime) or apply template
- Add/edit/remove Teachers, Subjects, Student Groups
- Build course list (link teacher/subject/groups)
- Toggle light/dark mode
- Navigate to Courses/Constraints/Solve

**Loading States:** None (instant)  
**Empty States:** "No classrooms defined. Add one to get started." with CTA  
**Error States:** Inline validation (duplicate names, invalid capacity, missing required fields)

### 2. Courses Editor
**Purpose:** Manage required courses with reordering (affects solver order).  
**Layout:** BorderPane with ReorderableTableView + detail pane.  
**User Actions:**
- Drag-and-drop reorder rows
- Add/edit/remove course (teacher, subject, student groups)
- View participant count (sum of StudentGroup sizes)
- Bulk add from presets
- Navigate to Solve/Constraints

**Loading States:** None  
**Empty States:** "No courses added yet. Add courses to solve." with CTA to Setup  
**Error States:** Validation (course missing teacher/subject/groups)

### 3. Constraints Overview
**Purpose:** Validate before solving.  
**Layout:** Scrollable VBox with sections (Resources, Courses, Timeslots, Validation).  
**User Actions:**
- Review summaries
- Jump to fix issues
- Proceed to Solve if valid

**Loading States:** None  
**Empty States:** N/A  
**Error States:** ValidationBanner listing blocking issues (no timeslots, no courses, insufficient resources). "Solve" disabled until valid.

### 4. Solving / Progress
**Purpose:** Run solver asynchronously with cancellation.  
**Layout:** Centered LoadingOverlay with progress/status.  
**User Actions:**
- Start solve (from valid state)
- Cancel solve (must stop algorithm)

**Loading States:** ProgressIndicator, elapsed time, status message ("Searching for solution...")  
**Empty States:** N/A  
**Error States:** Exception shown if solver throws unexpectedly (non-cancel)

### 5. Timetable View
**Purpose:** Visualize solution.  
**Layout:** Toolbar (filters, layout toggle, export) + TimetableGrid.  
**User Actions:**
- Filter by teacher/student group/classroom/subject
- Toggle layout: by room / by teacher / by group
- Export to PDF, CSV, PNG
- Save scenario (JSON/YAML)
- Back to configure/new solve

**Loading States:** None (after success)  
**Empty States:** N/A (only shown on success)  
**Error States:** N/A

### 6. No Solution / Details
**Purpose:** Explain failure using `BacktrackResult.NoSolutionFound.previousState()`.  
**Layout:** VBox with explanation + suggestions + actions.  
**User Actions:**
- View partial state summary
- Suggested fixes (add rooms/timeslots/capacity)
- Adjust configuration and retry
- Save scenario for analysis

**Loading States:** None  
**Empty States:** N/A  
**Error States:** Clear explanation of likely cause(s) based on constraints tested

### 7. Scenario Manager
**Purpose:** Save/load scenarios as JSON and/or YAML.  
**Layout:** SplitPane (saved scenarios list + details).  
**User Actions:**
- Save current scenario (JSON/YAML, choose location)
- Load scenario (file picker or from list)
- Delete scenario
- Schema version check on load

**Loading States:** File I/O indicator  
**Empty States:** "No saved scenarios."  
**Error States:** Parse errors, version mismatch, IO errors

### 8. About / Help
**Purpose:** App info and theme.  
**Layout:** Simple VBox.  
**User Actions:**
- Toggle theme
- View version/Java/JavaFX

**Loading States:** None  
**Empty States:** N/A  
**Error States:** N/A

## State Transitions (key)
Configure → Courses (reorder) → Constraints (validate) → Solving (cancelable) → Timetable (success) OR NoSolution (failure). ScenarioManager accessible from toolbar.
