# Feedback via Google Apps Script

## Overview

Flow:

`Android app -> HTTPS POST -> Google Apps Script Web App -> Google Sheets`

The app sends a JSON payload directly to a deployed Apps Script Web App. The script validates the request, appends a row to a Google Sheet, and returns a small JSON response.

## Google Sheet

Suggested spreadsheet tab name:

`Feedback`

Expected columns:

1. `createdAt`
2. `type`
3. `message`
4. `email`
5. `appVersion`
6. `locale`
7. `platform`
8. `optionalContext`

`optionalContext` is stored as JSON text and may include:

- `clueRounds`
- `playerCount`

## Apps Script

Use the script in [feedback-apps-script.gs](./feedback-apps-script.gs).

Before deploying, set:

- `SHEET_ID`
- `SHEET_NAME`

## Deploy the Web App

1. Create a Google Sheet and copy its ID from the URL.
2. Open [script.google.com](https://script.google.com) and create a new Apps Script project.
3. Paste the contents of `docs/feedback-apps-script.gs` into `Code.gs`.
4. Set `SHEET_ID` to your spreadsheet ID.
5. Keep `SHEET_NAME` as `Feedback`, or change it in both places.
6. Deploy as `Web app`.
7. Execute as: `Me`.
8. Who has access: `Anyone` or `Anyone with the link`.
9. Copy the deployed `/exec` URL.

## Configure the Android app

Set the endpoint through a Gradle property:

`feedbackEndpointUrl=https://script.google.com/macros/s/YOUR_DEPLOYMENT_ID/exec`

Recommended place:

- user-level `~/.gradle/gradle.properties`

Alternative:

- project `gradle.properties` if you intentionally want to share it with the team

The data module exposes the URL through `BuildConfig.FEEDBACK_ENDPOINT_URL`.

## Payload sent by the app

- `type`
- `message`
- `email`
- `appVersion`
- `locale`
- `platform`
- `createdAt`
- `optionalContext`

The app does not send:

- game history
- secret word
- round logs
- device identifiers

## Known limitations

- There is no auth layer beyond the Apps Script deployment settings.
- Apps Script quotas apply to requests and writes.
- In this phase there is no attachment support, no admin dashboard, and no server-side deduplication.
- If the endpoint URL is missing from the build config, the app shows a configuration error and does not send anything.
