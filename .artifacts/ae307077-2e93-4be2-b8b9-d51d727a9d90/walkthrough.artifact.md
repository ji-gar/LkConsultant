# Walkthrough — Employee App (EMS) Enhancement

I have updated the Employee Management System (EMS) to align with the latest API specifications. The changes focus on dynamic leave policies, holiday awareness, and improved task lifecycle management.

## Changes Made

### 1. Networking & Data Models
- Updated `RetrofitInstance.kt` with new models: `LeavePolicy`, `Holiday`, and their corresponding response wrappers.
- Added `getLeavePolicy()` and `getHolidays()` endpoints to the API service.

### 2. Enhanced Leave Portal
- **Dynamic Policy:** The "Apply for Leave" form now fetches available leave types directly from the server. Users can only select types with a remaining balance.
- **Policy Summary:** Added a "Policy Status" section that shows used vs. remaining days for each leave category (e.g., Casual, Sick, Annual) with a progress bar.
- **Notice Period & Holiday Validation:** The date picker now restricts selection based on:
    - `min_notice_days` defined in the policy.
    - Company holidays fetched from the API.
    - Sundays (automatically disabled).

### 3. Improved Task Management
- **Approval Tracking:** Task items now display an `Approval Status` badge (e.g., "Pending Approval", "Approved").
- **Task Lifecycle Actions:**
    - Added a "Start Task" button for tasks in `pending` status.
    - Added a "Complete Task" button for tasks `in_progress`.
    - These actions are automatically hidden if the task is awaiting approval or has been rejected.

## Verification Results

### Manual Verification
- **Leave Application:** Selection of leave type dynamically updates the "remaining" count. Date picker correctly greys out Sundays and dates within the notice period.
- **Task List:** Approval badges are visible. "Start Task" and "Complete Task" buttons appear and function as expected, triggering a list refresh upon success.

> [!NOTE]
> The application now enforces business rules locally in the date picker, but the server remains the final authority on all leave and task validations.
