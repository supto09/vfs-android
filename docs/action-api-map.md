# Action -> API Map (Quick Reference)

This file maps app actions to ViewModel handlers and API calls/endpoints.

## Auth Panel

| UI Action | ViewModel Method | API Call Order | Endpoint(s) |
|---|---|---|---|
| `Login` | `MainViewModel.login()` | 1) `leasedAccountApi.leaseAccount(entry)` -> 2) `authApi.login(...)` -> 3) if OTP required: `authApi.verifyOtp(...)` (auto) | `POST https://vfsapi.ashulo.org/accounts/lease` -> `POST https://lift-api.vfsglobal.com/user/login` -> `POST https://lift-api.vfsglobal.com/user/login` |
| `Stop` | `MainViewModel.stopLoginFlow()` | Cancels running login/OTP jobs | No API call |

## System Control Panel

| UI Action | ViewModel Method | API Call Order | Endpoint(s) |
|---|---|---|---|
| `Start ReLogin` | `MainViewModel.startPeriodicReLogin()` | Repeating cycle: `logout()` -> wait CF cookie -> `login()` -> callback `addApplicant(triggerSlotFlowOnSuccess = true)` -> on add success: `loadTimeSlot()` + `startCheckIsSlotAvailable()` | See chained actions below |
| `Stop` (ReLogin) | `MainViewModel.stopPeriodicReLogin()` | Cancels periodic relogin job | No API call |
| `Cloudflare` modal | (UI-only component) | WebView/manual bypass flow | No direct API class call |
| `Turnstile` modal | (UI-only component) | WebView/manual token flow | No direct API class call |
| `Settings` | `MainViewModel.updateAppConfig(...)` | Persists config only | No network API |

## App Control Panel

| UI Action | ViewModel Method | API Call Order | Endpoint(s) |
|---|---|---|---|
| `Load Applicants` | `MainViewModel.loadApplicants()` | `applicantApi.loadApplicants(...)` | `POST https://lift-api.vfsglobal.com/appointment/application` |
| `Stop` (Load Applicants) | `MainViewModel.stopLoadApplicants()` | Cancels load-applicants job | No API call |
| `Add Applicants` | `MainViewModel.addApplicantManual()` -> `addApplicant()` | Retry loop (max 5): `applicantApi.addApplicant(...)` until non-blank URN | `POST https://lift-api.vfsglobal.com/appointment/applicants` |
| `Stop` (Add Applicants) | `MainViewModel.stopAddApplicant()` | Cancels add-applicant job/retry loop | No API call |
| `Check Slot Ava` | `MainViewModel.startCheckIsSlotAvailable()` | Loop: `calenderApi.checkIsSlotAvailable(...)` every ~3 min + jitter until earliest date found | `POST https://lift-api.vfsglobal.com/appointment/CheckIsSlotAvailable` |
| `Stop` (Check Slot) | `MainViewModel.stopCheckIsSlotAvailable()` | Cancels check-slot job | No API call |
| `Load Calender` | `MainViewModel.loadCalender()` | Reads earliest date from Firebase and stops check-slot if active. Finder app exits early; follower app calls `calenderApi.loadCalender(...)` to populate `availableDates` | Firebase path `z_earliest_date/{countryCode}/{missionCode}/date` + `POST https://lift-api.vfsglobal.com/appointment/calendar` (follower path) |
| `Stop` (Load Calender) | `MainViewModel.stopLoadCalender()` | Cancels load-calender job | No API call |
| `Load Slot` | `MainViewModel.loadTimeSlot()` | 1) read earliest date from Firebase -> 2) `slotApi.loadSlots(...)` -> 3) `startSchedule(allocationIds)` | `POST https://lift-api.vfsglobal.com/appointment/timeslot` -> then schedule endpoint below |
| `Stop` (Load Slot) | `MainViewModel.stopLoadTimeSlot()` | Cancels load-slot job | No API call |
| `Schedule` (status-only button) | No action bound | UI state indicator only | No API call from button press |
| `Logout` | `MainViewModel.logout()` | Stops child jobs + clears session | No network API |

## Internal/Automatic API Calls (Not Direct Button Presses)

| Trigger | ViewModel Method | API Call | Endpoint |
|---|---|---|---|
| Login failure path | `attemptLoginOnce()` | `leasedAccountApi.reportBlock(email, entry)` | `POST https://vfsapi.ashulo.org/accounts/reportBlocked` |
| Load slot success path | `startSchedule(allocationIds)` | `scheduleApi.schedule(...)` for each allocation ID (randomized order) until success or exhausted | `POST https://lift-api.vfsglobal.com/appointment/schedule` |

## Notes

- OTP verify is automatic now when login returns `enableOTPAuthentication=true` and no `accessToken`.
- `Stop` buttons cancel jobs/coroutines; they do not call stop endpoints.
- ReLogin flow is a chained orchestration over multiple API calls; it is not a single endpoint.
