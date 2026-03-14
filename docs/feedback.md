# Feedback via Google Apps Script

## Overview

Flow:

`Android app -> HTTPS POST -> Google Apps Script Web App -> Google Sheets`

The app sends a JSON payload directly to a deployed Apps Script Web App. The script validates the request, appends a row to a Google Sheet, and returns a small JSON response.
The implementation stays intentionally small for this MVP, but it now includes stricter payload validation, a shared token, and a message length limit.

## Google Sheet

Recommended spreadsheet name:

`El Impostor Feedback`

Recommended tab name:

`Feedback`

Exact columns and order:

1. `createdAt`
2. `type`
3. `message`
4. `email`
5. `appVersion`
6. `locale`
7. `platform`
8. `optionalContext`

Example row:

`2026-03-14T18:42:10.000Z | suggestion | More category packs please | mail@example.com | 1.0.0 | es | android | {"clueRounds":2,"playerCount":6}`

`optionalContext` is stored as JSON text and may include:

- `clueRounds`
- `playerCount`

## Apps Script

Use the script in [feedback-apps-script.gs](./feedback-apps-script.gs).

Before deploying, set:

- `SHEET_ID`
- `SHEET_NAME`
- `SHARED_TOKEN`

Use a long random token and keep the same value in Android and Apps Script.

## Deploy the Web App

1. Create a Google Sheet and copy its ID from the URL.
2. Open [script.google.com](https://script.google.com) and create a new Apps Script project.
3. Paste the contents of `docs/feedback-apps-script.gs` into `Code.gs`.
4. Set `SHEET_ID` to your spreadsheet ID.
5. Keep `SHEET_NAME` as `Feedback`, or change it in both places.
6. Set `SHARED_TOKEN` to the same long random string used in Android.
7. Deploy as `Web app`.
8. Execute as: `Me`.
9. Who has access: `Anyone` or `Anyone with the link`.
10. Copy the deployed `/exec` URL.

## Configure the Android app

Set the endpoint through a Gradle property:

`feedbackEndpointUrl=https://script.google.com/macros/s/YOUR_DEPLOYMENT_ID/exec`

Set the shared token through another Gradle property:

`feedbackSharedToken=YOUR_LONG_RANDOM_SHARED_TOKEN`

Recommended place:

- user-level `~/.gradle/gradle.properties`

On Windows, the equivalent user-level file is typically:

- `%USERPROFILE%\\.gradle\\gradle.properties`

Alternative:

- project `gradle.properties` if you intentionally want to share it with the team
- a local machine-specific `gradle.properties` that is not committed

Recommended example in user-level `gradle.properties`:

```properties
feedbackEndpointUrl=https://script.google.com/macros/s/YOUR_DEPLOYMENT_ID/exec
feedbackSharedToken=REPLACE_WITH_A_LONG_RANDOM_SHARED_TOKEN
```

Do not hardcode either value in source files or commit real production values to the repository.

The data module exposes both values through:

- `BuildConfig.FEEDBACK_ENDPOINT_URL`
- `BuildConfig.FEEDBACK_SHARED_TOKEN`

## Payload sent by the app

- `type`
- `message`
- `email`
- `token`
- `appVersion`
- `locale`
- `platform`
- `createdAt`
- `optionalContext`

Validation enforced on both Android and Apps Script:

- `type` must be `suggestion` or `problem`
- `message` is required and must be between `8` and `1200` characters
- `email` is optional, but must match a basic email format if present
- `token` must match the shared token configured on the server
- `platform` must be `android`
- `locale`, `appVersion`, `createdAt`, and optional context values must be well formed

The app does not send:

- game history
- secret word
- round logs
- device identifiers

The app may also send optional round context only when it is already available:

- `clueRounds`
- `playerCount`

## Known limitations

- The shared token is lightweight protection, not a full auth system.
- Apps Script quotas apply to requests and writes.
- Apps Script `ContentService` returns JSON bodies, but does not provide robust custom HTTP status control in this setup.
- In this phase there is no attachment support, no admin dashboard, and no server-side deduplication.
- If the endpoint URL or token is missing from the build config, the app shows a configuration error and does not send anything.
