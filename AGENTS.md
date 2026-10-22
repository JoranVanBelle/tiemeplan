---
name: javafx-frontend
description: Design, implement, test, and package polished cross-platform JavaFX desktop frontends that integrate with an existing backend.
---

# JavaFX Frontend Engineering

## Objective

Build a maintainable, accessible, visually consistent desktop application using JavaFX and modern Java.

The frontend integrates with an existing backend. The backend remains the source of truth for business rules, persistence, authentication, authorization, and data validation.

Prioritize usability, consistency, maintainability, and cross-platform compatibility over unnecessary abstraction.

## 1. Inspect Before Implementing

Before modifying files:

- Inspect the existing repository, `AGENTS.md`, `pom.xml`, and project documentation.
- Identify the Java version, JavaFX version, Maven configuration, and current application entry point.
- Inspect the backend API, API documentation, DTOs, authentication mechanism, and error responses.
- Identify the existing domain model and supported user workflows.
- Determine whether the application already has UI components, stylesheets, tests, or packaging configuration.
- Identify platform-specific assumptions.

Do not invent backend endpoints, request fields, response structures, or business rules.

If requirements are ambiguous, document the assumptions and ask for clarification when the ambiguity materially affects the design.

For a new frontend, propose the screens, navigation, visual direction, architecture, and implementation milestones before writing substantial implementation code.

## 2. JavaFX Architecture

Use a clear separation of responsibilities.

Recommended structure:

- `app`: application entry point and lifecycle.
- `ui`: JavaFX views, controllers, reusable components, and navigation.
- `viewmodel`: presentation state and UI-specific logic where appropriate.
- `application`: use cases and orchestration.
- `client`: backend HTTP communication, serialization, and API DTOs.
- `domain`: frontend domain concepts only when they provide genuine value.
- `config`: configuration and dependency wiring.

Adapt this structure to the size and complexity of the application. Do not create empty layers or unnecessary abstractions.

### JavaFX rules

- Keep business logic and HTTP communication out of FXML controllers.
- Keep JavaFX-specific types out of the backend client and domain logic wherever practical.
- Use constructor injection for application-owned dependencies.
- Prefer small, focused controllers and reusable UI components.
- Use FXML when it improves separation of layout and behavior; use programmatic JavaFX layouts when they are simpler and easier to maintain.
- Avoid static mutable application state and service locators.
- Use standard JavaFX controls where they satisfy the requirements before introducing custom controls.
- Do not block the JavaFX Application Thread with network requests, file I/O, or long-running operations.
- Use `Task`, `Service`, or a suitable asynchronous abstraction for background work.
- Update JavaFX UI state on the JavaFX Application Thread.
- Handle task cancellation and application shutdown where relevant.
- Dispose of listeners, subscriptions, and background resources when their owners are no longer active.

Do not introduce a large dependency-injection framework or MVVM library without a clear justification.

## 3. UI and UX Design

Before implementing screens, establish a coherent design system.

Define:

- Typography and heading hierarchy.
- Spacing and alignment conventions.
- Color palette and semantic colors.
- Button, input, dialog, table, and navigation styles.
- Focus, hover, pressed, disabled, loading, success, and error states.
- Consistent page headers and primary actions.
- Window sizing, resizing behavior, and sensible minimum dimensions.

### Desktop-specific design

- Design for mouse, keyboard, and resizable windows.
- Support keyboard navigation and visible focus indicators.
- Use appropriate tab order and standard keyboard conventions.
- Use native-feeling menus, dialogs, and controls when appropriate.
- Avoid unnecessary modal dialogs.
- Keep destructive actions explicit and provide confirmation when warranted.
- Make empty states informative and provide useful next actions.
- Provide clear feedback for long-running operations.
- Use layouts that resize correctly rather than relying on fixed coordinates.
- Test realistic window sizes and long or missing content.
- Support accessibility features and adequate text contrast.
- Use icons consistently and provide accessible labels or tooltips where needed.

Avoid generic dashboard layouts, excessive cards, decorative gradients, and custom styling that does not improve the user experience.

Use CSS stylesheets and reusable style classes for shared visual rules rather than duplicating inline styles throughout the application.

## 4. Backend Integration

Treat the existing backend as the source of truth.

- Inspect the actual API contract before implementing a client.
- Use Java's HTTP client or an existing project-approved HTTP library.
- Centralize base URL configuration, request construction, serialization, and error handling.
- Keep API DTOs separate from JavaFX view state.
- Handle HTTP status codes, timeouts, connection failures, malformed responses, and unauthorized requests.
- Present useful error messages to users without exposing internal stack traces or secrets.
- Never silently swallow exceptions.
- Handle expired authentication and logout consistently with the backend's contract.
- Keep credentials and tokens out of logs.
- Avoid duplicating backend validation and business rules.
- Use explicit loading, success, and failure states.
- Ensure background requests cannot update disposed views or stale application state.

Do not add mock responses as a substitute for real backend integration unless the task explicitly requests a prototype or test fixture.

## 5. Java and Maven Conventions

- Use the Java version already established by the project.
- Follow modern, idiomatic Java conventions.
- Prefer immutable data where practical.
- Use records for suitable immutable data carriers.
- Use descriptive names and small, focused methods.
- Avoid unnecessary inheritance, generic frameworks, and premature abstractions.
- Use Maven dependency and plugin versions deliberately.
- Keep JavaFX dependencies and runtime packaging compatible with supported operating systems.
- Do not assume a regular executable JAR includes a working JavaFX runtime.
- Avoid platform-specific native dependencies unless they are correctly handled by the build.
- Do not add dependencies when the JDK or JavaFX already provides an adequate solution.

## 6. Testing

Write automated tests appropriate to each layer.

### Unit tests

Test application services, mapping, validation, and presentation logic without launching the entire JavaFX application whenever possible.

### JavaFX tests

- Test controller behavior and important UI interactions.
- Verify loading, empty, success, and failure states.
- Test navigation and user actions.
- Initialize JavaFX correctly in tests that require the JavaFX toolkit.
- Avoid brittle assertions against implementation details or exact layout coordinates.

Use TestFX only when its capabilities are needed and its maintenance and compatibility are acceptable for the project.

### Backend client tests

- Test serialization and deserialization.
- Verify request methods, paths, headers, and bodies.
- Test status-code handling, timeouts, and malformed responses.
- Prefer a controlled mock HTTP server or another deterministic test boundary.

Run the existing Maven tests after changes. Do not weaken or remove tests merely to make the build pass.

## 7. Cross-Platform Packaging

The application should be designed to run on Windows, macOS, and Linux unless the project explicitly limits its target platforms.

- Distinguish compilation from native packaging.
- Use JavaFX dependencies compatible with each target platform.
- Evaluate `jlink` and `jpackage` for creating self-contained runtimes and native application packages.
- Account for OS-specific JavaFX native libraries, application icons, launchers, and file paths.
- Do not assume a native package built on one operating system will work on another.
- Use operating-system-specific CI runners when native installers are required.
- Document runtime prerequisites and supported platforms.
- Keep secrets and environment-specific endpoints out of packaged source and configuration.

If release automation is requested, propose a reproducible build and release workflow that includes appropriate tests and artifacts for each supported platform.

## 8. Implementation Workflow

For each feature:

1. Understand the user workflow and relevant backend endpoints.
2. Identify the screens, states, and interactions involved.
3. Reuse the established design system.
4. Implement the smallest complete vertical slice.
5. Connect it to the actual backend.
6. Add appropriate tests.
7. Run relevant Maven checks.
8. Review the resulting UI for usability and visual consistency.
9. Summarize changed files, verification results, and remaining issues.

Do not implement every screen in one enormous change.

Keep unrelated refactoring out of feature changes. Ask for approval before major architectural changes or introducing significant dependencies.

## 9. Completion Criteria

A feature is complete when:

- Its intended user workflow is implemented.
- It integrates with the actual backend contract.
- Loading, empty, error, and success states are handled where applicable.
- The UI remains responsive during network operations.
- The layout behaves correctly when resized.
- Keyboard navigation and accessibility have been considered.
- Relevant tests pass.
- The application builds successfully.
- Any unverified behavior or remaining limitations are reported honestly.