# UI Design Specification

**Project:** Tiemeplan  
**Last Updated:** 2026-10-22  
**Status:** Approved for planning (no implementation yet)

## 1. Design Principles
- **Native desktop feel**: Resizable windows, keyboard navigation, standard controls, platform-appropriate behavior.
- **Pragmatic over decorative**: Focus on configuring constraints and reading timetables. Avoid dashboard clichés.
- **Pure domain separation**: UI never calls solver directly; uses `SolverService` wrapping existing `Solver`. Domain in `variables/` and `backtrack/` remains untouched.
- **Responsive during computation**: All solving runs off JavaFX thread with cancellation.
- **Themeable**: First-class light and dark mode.
- **Accessible**: Sufficient contrast, focus indicators, keyboard nav, labels/tooltips.

## 2. Visual Tokens

### Colors (CSS variables)
Define in `styles.css` base with theme overrides.

#### Light Theme
| Token | Value (suggested) | Usage |
|---|---|---|
| `-color-bg` | `#f7f7f8` | App background |
| `-color-surface` | `#ffffff` | Cards/panels/tables |
| `-color-surface-2` | `#f0f0f2` | Subtle backgrounds |
| `-color-border` | `#d0d0d4` | Borders/dividers |
| `-color-text` | `#1a1a1a` | Primary text |
| `-color-text-secondary` | `#4a4a4a` | Secondary text |
| `-color-accent` | `#2563eb` | Primary actions/links |
| `-color-accent-hover` | `#1d4ed8` | Hover state |
| `-color-success` | `#16a34a` | Success states |
| `-color-warning` | `#d97706` | Warnings |
| `-color-error` | `#dc2626` | Errors/conflicts |
| `-color-focus` | `#3b82f6` | Focus ring |

#### Dark Theme
| Token | Value (suggested) | Usage |
|---|---|---|
| `-color-bg` | `#121214` | App background |
| `-color-surface` | `#1e1e22` | Cards/panels/tables |
| `-color-surface-2` | `#2a2a2e` | Subtle backgrounds |
| `-color-border` | `#3a3a3f` | Borders/dividers |
| `-color-text` | `#e5e5e5` | Primary text |
| `-color-text-secondary` | `#b0b0b5` | Secondary text |
| `-color-accent` | `#3b82f6` | Primary actions/links |
| `-color-accent-hover` | `#2563eb` | Hover state |
| `-color-success` | `#22c55e` | Success states |
| `-color-warning` | `#f59e0b` | Warnings |
| `-color-error` | `#ef4444` | Errors/conflicts |
| `-color-focus` | `#60a5fa` | Focus ring |

### Typography
| Element | Size/Weight | Notes |
|---|---|---|
| Body | 13-14px / 400 | Default text |
| Small/Label | 12-13px / 400 | Table headers, labels |
| Heading 1 | 18px / 600 | Screen titles |
| Heading 2 | 16px / 600 | Section headers |
| Button | 13px / 500 | Action buttons |

Font stack: `-fx-font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;`

### Spacing (4pt grid)
| Token | Value | Usage |
|---|---|---|
| `space-1` | 4px | Tight gaps |
| `space-2` | 8px | Default gaps |
| `space-3` | 12px | Form spacing |
| `space-4` | 16px | Section padding |
| `space-6` | 24px | Major section gaps |
| `space-8` | 32px | Page padding |

### Elevation/Radius
| Token | Value | Usage |
|---|---|---|
| `radius-sm` | 4px | Inputs/buttons |
| `radius-md` | 6px | Cards/panels |
| `radius-lg` | 8px | Dialogs |
| `shadow-sm` | 0 1px 2px rgba(0,0,0,0.05) | Subtle elevation |

## 3. Reusable Components
| Component | Type | Behavior | Notes |
|---|---|---|---|
| `PrimaryButton` | Button | Accent color, focus ring | Default action (Solve, Save) |
| `SecondaryButton` | Button | Surface with border | Secondary actions |
| `GhostButton` | Button | Transparent, text color | Tertiary actions |
| `DestructiveButton` | Button | Error color | Delete/remove (confirmation required) |
| `ValidatedTextField` | TextField | Inline validation messages | Used in forms |
| `SearchableComboBox` | ComboBox | Filterable options | For teachers/subjects/groups |
| `ReorderableTableView<T>` | TableView | Drag-and-drop row reordering | For Courses list (required) |
| `TimetableGrid` | Custom Control | Grid by Day×Time, color-coded cells | Filters by teacher/group/room/subject, supports export snapshot |
| `CourseCell` | Custom Control | Displays course/teacher/groups/room | Clickable for details |
| `EmptyState` | Pane | Icon/message/CTA | Shown when lists empty |
| `LoadingOverlay` | StackPane | ProgressIndicator + message + Cancel | Shown during solving |
| `ValidationBanner` | HBox | Warnings/errors summary | Shown in Constraints Overview |
| `FileDropZone` | Pane | Drag-drop for load/save scenarios | Scenario Manager |

## 4. Navigation & Interaction Patterns
- **Navigation model**: Screen-based navigation via `NavigationService`. Screens: Setup, Courses, Constraints, Solving, Timetable, NoSolution, ScenarioManager, About.
- **Layout**: `BorderPane` root with `ToolBar` (nav, theme toggle, save/load) and content area. `SplitPane` where lists+details make sense.
- **State preservation**: ViewModels retain scenario state across navigation.
- **Modal dialogs**: Only for destructive actions and critical confirmations.
- **Keyboard**: Tab order logical, Enter activates primary action where appropriate, Esc closes dialogs/cancels where safe.
- **Reordering**: Drag rows in Courses table with visual feedback.
- **Theming**: Theme toggle persists preference.
- **Long operations**: Disable inputs, show loading overlay with Cancel during solving.
- **Cancellation**: Cancel propagates to cooperative cancellation token in Solver.

## 5. Responsive Design Notes
- Minimum window size: 900×650
- Tables scroll horizontally/vertically as needed
- Timetable grid scrolls/pans if many timeslots
- SplitPane dividers resizable
- Text wraps where reasonable, tooltips for truncated content
