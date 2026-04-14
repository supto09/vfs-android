# Full Cycle Flow (Periodic + Finder/Follower)

```mermaid
flowchart TD
    A[Start Periodic ReLogin] --> B[Reset cycle state]
    B --> C[Stop child jobs + clear session]
    C --> D[Wait for CF cookie]
    D --> E[Login attempt]

    E --> F{Lease account success?}
    F -- No --> Z1[Login cycle failed]
    F -- Yes --> G[Solve Turnstile]
    G --> H{Auth login result}

    H -- Success --> I[Save session]
    H -- OTP required --> J[Auto verify OTP with fixed code]
    J --> K{OTP verify success?}
    K -- No --> Z1
    K -- Yes --> I
    H -- Failure --> Z1

    I --> L[Add Applicant]
    L --> M{URN non-blank?}
    M -- No --> N[Retry addApplicant up to 5 attempts<br/>10s delay]
    N --> M
    M -- Yes --> O[Save URN]

    O --> P[Start Check Slot Ava]
    O --> Q[Start Load Calender]

    P --> R{checkIsSlotAvailable found earliest date?}
    R -- No --> P1[Wait ~3min + jitter] --> P
    R -- Yes --> S[Finder mode]
    S --> T[Save earliest date to Firebase]
    T --> U[Stop loadCalender if running]
    U --> V[Stop loadSlot if running]
    V --> W[loadTimeSlot using finder date]
    W --> X[Finder slot attempts: same date x3<br/>3s delay between failures]
    X --> Y{Non-empty allocations?}
    Y -- Yes --> Y1[startSchedule]
    Y -- No --> Y2[Finder slot loading exhausted]

    Q --> Q1[Read earliest date from Firebase]
    Q1 --> Q2[Stop check-slot if running]
    Q2 --> Q3{Is current app finder?}
    Q3 -- Yes --> Q4[Exit loadCalender early]
    Q3 -- No --> Q5[Call loadCalender API]
    Q5 --> Q6[Save availableDates]
    Q6 --> Q7[Compute followerPriorityDates up to 3]
    Q7 --> Q8[Build follower attempt list round-robin x2]
    Q8 --> Q9[Start load slot attempts with 3s delay]
    Q9 --> Q10{Any attempt returns non-empty allocations?}
    Q10 -- Yes --> Y1
    Q10 -- No --> Q11[Follower slot loading exhausted]

    Z1 --> Z2[Wait 25 min then next periodic cycle]
    Y1 --> Z2
    Y2 --> Z2
    Q11 --> Z2
    Q4 --> Z2
```

## Operational Notes

1. Periodic cycle starts by fully resetting state and jobs, then performs login.
2. Login uses mission/country from leased account payload for auth requests.
3. `addApplicant` is crash-safe: max 5 attempts, 10s fixed retry delay, success requires non-blank URN.
4. After `addApplicant` success, `startCheckIsSlotAvailable()` and `loadCalender()` run in parallel.
5. Finder app is the first app that gets earliest date from check-slot in current cycle.
6. Finder writes earliest date to Firebase, preempts calendar/slot jobs, then loads slot.
7. Finder slot loading retries the same date at least 3 times with 3s delay between failures.
8. Follower reads earliest date from Firebase, stops check-slot, then loads calendar API dates.
9. Follower computes per-device prioritized dates and tries up to 3 dates over 2 rounds.
10. Delay between failed follower slot attempts is 3s.
11. Slot load success means non-empty allocation IDs; then `startSchedule(...)` is triggered.
