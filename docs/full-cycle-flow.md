# Full Cycle Flow (Periodic + Finder/Follower)

```mermaid
flowchart TD
    A[Start Periodic ReLogin] --> B[Reset cycle state]
    B --> C[Stop child jobs + clear session]
    C --> D[Wait for CF cookie]
    D --> E[Login attempt]

    E --> F{Lease account success?}
    F -- No --> Z1[Login cycle failed]
    F -- Yes --> G[Clear OTP for leased contact]
    G --> G1[Solve Turnstile for login]
    G1 --> H{Auth login result}
    
    H -- OTP required --> J1[Solve new Turnstile for verify OTP]
    J1 --> J2[Read OTP from Firebase using leased contact]
    J2 --> J[Verify OTP request]
    J --> K{OTP verify success?}
    K -- No --> Z1
    K -- Yes --> I[Save session]

    H -- Success --> I
    H -- Failure --> Z1

    I --> L1[Start Add Applicant job]
    I --> L2[Start Add Applicant Follower job]

    L1 --> M1{addApplicant success?}
    M1 -- No --> N1[Retry rules:<br/>- counted max 5<br/>- no-slot info error -> non-counted retry with 3min+jitter]
    N1 --> M1
    M1 -- Yes --> O1[Finder path:<br/>mark finder + save today as earliest date to Firebase + save URN]
    O1 --> P1[loadCalender from today]

    L2 --> F1[Read earliest date from Firebase]
    F1 --> F2[Stop regular addApplicant job if running]
    F2 --> M2{addApplicantFollower success?}
    M2 -- No --> N2[Retry rules same as addApplicant]
    N2 --> M2
    M2 -- Yes --> O2[Follower path:<br/>save URN only]
    O2 --> P2[loadCalender from Firebase earliest date]

    P1 --> Q[loadCalender API]
    P2 --> Q
    Q --> Q1[Save availableDates]
    Q1 --> Q2[Use follower app count<br/>preloaded on entry load<br/>fallback 4 if unavailable]
    Q2 --> Q3[Compute followerPriorityDates]
    Q3 --> Q4[Start loadTimeSlot attempts]
    Q4 --> R{Non-empty allocation IDs?}
    R -- Yes --> S[startSchedule]
    R -- No --> T[Slot loading exhausted]

    S --> Z2[Wait 25 min then next periodic cycle]
    T --> Z2
    Z1 --> Z2
```

## Operational Notes

1. Periodic cycle resets and logs in every 25 minutes while active.
2. Login and verify-OTP use separate captcha token solves.
3. OTP submit reads OTP from Firebase using leased contact number.
4. After login success, both add-applicant jobs start in parallel.
5. Regular add-applicant success marks finder and writes today's date (`dd/MM/yyyy`) to Firebase.
6. Follower add-applicant first reads earliest date from Firebase and stops regular add-applicant if it is running.
7. No-slot informational error (`code:10673` or matching message text) does not consume counted add-applicant retries and uses 3min+jitter delay.
8. `loadCalender` now uses explicit `fromDate` (today for finder path, Firebase earliest for follower path).
9. Follower app count is fetched from backend by entry `(countryCode, missionCode)` and cached on entry load.
10. Fallback follower app count is `4` if backend follower-number API is unavailable/invalid.
11. HTTP trace logging is standardized to one `HTTP START` and one `HTTP END` per request with request/response previews.
