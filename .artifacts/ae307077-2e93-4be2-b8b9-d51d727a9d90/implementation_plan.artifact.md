# Implementation Plan — Employee App (EMS) Enhancement

Adjust the `employeeApp` (LKDC EMS) to align with the latest API specifications for Leave Management, Leave Policy, and Task Management.

## User Review Required

> [!IMPORTANT]
> The Leave Apply form will now dynamically fetch types from the server. Users will only see types available in their policy with remaining balance.

## Proposed Changes

### [Component] Networking & Models

#### [MODIFY] [RetrofitInstance.kt](file:///C:/LkConsultant-master%20(2)/LkConsultant-master/app/src/main/java/com/room/roomy/retrofit/RetrofitInstance.kt)
- Add `getLeavePolicy()` endpoint.
- Add `getHolidays()` endpoint.
- Define `LeavePolicyResponse`, `LeavePolicy`, and `Holiday` data classes.
- Update `LeaveRequest` to match the latest object structure (including `user.leave_approvers`).

### [Component] Employee Home Screen UI

#### [MODIFY] [EmployeeHomeScreen.kt](file:///C:/LkConsultant-master%20(2)/LkConsultant-master/app/src/main/java/com/io/lkconsultants/view/EmployeeHomeScreen.kt)
- **State Management:**
    - Fetch and store `LeavePolicy` and `Holidays` on initialization.
- **Leave Screen (`LeaveRequestScreen`):**
    - Replace hardcoded leave types with dynamic types from `LeavePolicy`.
    - Display a "Policy Summary" grid showing `Remaining` vs `Used` days for each type.
    - Implement `min_notice_days` validation: Disable dates in the picker that fall within the notice period.
    - (Optional) Highlight company holidays in the date picker if possible (or show a list below).
- **Task Screen (`TaskListScreen`):**
    - Display `approval_status` (e.g., "Pending Approval", "Rejected") using badges.
    - Update Task actions: Ensure "Start" is shown for `pending` tasks and "Complete" only for `in_progress` tasks.
    - Show `checklist` progress more prominently.

### [Component] Helper Utilities

#### [MODIFY] [UIScreen.kt](file:///C:/LkConsultant-master%20(2)/LkConsultant-master/app/src/main/java/com/io/lkconsultants/view/UIScreen.kt)
- (If needed) Add generic badge components for status/priority.

## Verification Plan

### Automated Tests
- N/A (Manual verification on device/emulator is preferred for UI flow).

### Manual Verification
1.  **Login as Employee:**
    - Navigate to "Leaves" tab.
    - Verify that the "Policy Summary" shows correct remaining days.
    - Verify that the leave type dropdown only shows types from the policy.
    - Check that "From Date" respects `min_notice_days`.
2.  **Tasks:**
    - Verify that tasks awaiting approval are clearly marked.
    - Verify the "Start" -> "In Progress" -> "Complete" flow.
