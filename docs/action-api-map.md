# Action -> API Map (Quick Reference)

This file maps app actions to ViewModel handlers and API calls/endpoints.

## Auth Panel

| UI Action | ViewModel Method | API Call Order | Endpoint(s) |
|---|---|---|---|
| `Login` | `MainViewModel.login()` | 1) `leasedAccountApi.leaseAccount(entry)` -> 2) clear OTP by leased contact -> 3) solve captcha (login) -> 4) `authApi.login(...)` -> 5) if OTP required: solve new captcha (verify) -> read OTP from Firebase -> `authApi.verifyOtp(...)` | `POST https://vfsapi.ashulo.org/accounts/lease` -> `POST https://lift-api.vfsglobal.com/user/login` -> `POST https://lift-api.vfsglobal.com/user/login` |
| `Stop` | `MainViewModel.stopLoginFlow()` | Cancels running login/OTP jobs | No API call |

## System Control Panel

| UI Action | ViewModel Method | API Call Order | Endpoint(s) |
|---|---|---|---|
| `Start ReLogin` | `MainViewModel.startPeriodicReLogin()` | Repeating cycle: `stopAllChildJob()` -> clear session -> wait CF cookie -> `login()` -> callback starts both `addApplicant(triggerSlotFlowOnSuccess = true)` and `addApplicantFollower(triggerSlotFlowOnSuccess = true)` | See chained actions below |
| `Stop` (ReLogin) | `MainViewModel.stopPeriodicReLogin()` | Cancels periodic relogin job | No API call |
| `Cloudflare` modal | (UI-only component) | WebView/manual bypass flow | No direct API class call |
| `Turnstile` modal | (UI-only component) | WebView/manual token flow | No direct API class call |
| `Settings` | `MainViewModel.updateAppConfig(...)` | Persists config only | No network API |

## App Control Panel

| UI Action | ViewModel Method | API Call Order | Endpoint(s) |
|---|---|---|---|
| `Load Applicants` | `MainViewModel.loadApplicants()` | `applicantApi.loadApplicants(...)` | `POST https://lift-api.vfsglobal.com/appointment/application` |
| `Stop` (Load Applicants) | `MainViewModel.stopLoadApplicants()` | Cancels load-applicants job | No API call |
| `Add Appl.` | `MainViewModel.addApplicantManual()` -> `addApplicant()` | Retry loop (max 5 counted attempts): `applicantApi.addApplicant(...)`. If no-slot info error (`code:10673`/message), retry does not consume counted max and uses 3min+jitter delay. On success: mark finder -> save today's date to Firebase -> save URN -> `loadCalender(today)` | `POST https://lift-api.vfsglobal.com/appointment/applicants` |
| `Stop` (Add Applicants) | `MainViewModel.stopAddApplicant()` | Cancels add-applicant job/retry loop | No API call |
| `Add Appl Follower.` | `MainViewModel.addApplicantFollowerManual()` -> `addApplicantFollower()` | 1) read earliest date from Firebase -> 2) stop regular addApplicant job if running -> 3) retry `applicantApi.addApplicant(...)` with same no-slot handling -> 4) on success save URN and call `loadCalender(fromDate = earliestFromFirebase)` (does not write Firebase date) | Firebase path `z_earliest_date/{countryCode}/{missionCode}/date` + `POST https://lift-api.vfsglobal.com/appointment/applicants` |
| `Stop` (Add Applicant Follower) | `MainViewModel.stopAddApplicantFollower()` | Cancels add-applicant-follower job | No API call |
| `Check Slot Ava` | `MainViewModel.startCheckIsSlotAvailable()` | Loop: `calenderApi.checkIsSlotAvailable(...)` every ~3 min + jitter until earliest date found | `POST https://lift-api.vfsglobal.com/appointment/CheckIsSlotAvailable` |
| `Stop` (Check Slot) | `MainViewModel.stopCheckIsSlotAvailable()` | Cancels check-slot job | No API call |
| `Load Calender` | `MainViewModel.loadCalender(fromDate?)` | Calls `calenderApi.loadCalender(...)` using provided `fromDate` or today. Then computes follower priority dates using backend follower-app-number and starts slot loading. | `POST https://vfsapi.ashulo.org/accounts/follower-app-number` + `POST https://lift-api.vfsglobal.com/appointment/calendar` |
| `Stop` (Load Calender) | `MainViewModel.stopLoadCalender()` | Cancels load-calender job | No API call |
| `Load Slot` | `MainViewModel.loadTimeSlot()` | resolve target date (in-memory finder date, selected follower date, or Firebase earliest date fallback) -> `slotApi.loadSlots(...)` -> `startSchedule(allocationIds)` | Firebase path `z_earliest_date/{countryCode}/{missionCode}/date` (fallback only) + `POST https://lift-api.vfsglobal.com/appointment/timeslot` -> schedule endpoint below |
| `Stop` (Load Slot) | `MainViewModel.stopLoadTimeSlot()` | Cancels load-slot job | No API call |
| `Schedule` (status-only button) | No action bound | UI state indicator only | No API call from button press |
| `Logout` | `MainViewModel.logout()` | Stops child jobs + clears session | No network API |

## Internal/Automatic API Calls (Not Direct Button Presses)

| Trigger | ViewModel Method | API Call | Endpoint |
|---|---|---|---|
| Login failure path | `attemptLoginOnce()` | `leasedAccountApi.reportBlock(email, entry)` | `POST https://vfsapi.ashulo.org/accounts/reportBlocked` |
| Entry load path | `entryIndex` observer in `MainViewModel.init` | `leasedAccountApi.getFollowerAppCount(entry)` | `POST https://vfsapi.ashulo.org/accounts/follower-app-number` |
| Load slot success path | `startSchedule(allocationIds)` | `scheduleApi.schedule(...)` for each allocation ID (randomized order) until success or exhausted | `POST https://lift-api.vfsglobal.com/appointment/schedule` |

## Notes

- Lease/reportBlocked/follower-app-number APIs now require `clientToken` in request body.
- OTP verify is automatic now when login returns `enableOTPAuthentication=true` and no `accessToken`, and uses a fresh captcha token before OTP submit.
- `Stop` buttons cancel jobs/coroutines; they do not call stop endpoints.
- ReLogin flow is a chained orchestration over multiple API calls; it is not a single endpoint.
