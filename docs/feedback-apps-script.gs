const SHEET_ID = 'PASTE_YOUR_GOOGLE_SHEET_ID_HERE';
const SHEET_NAME = 'Feedback';

function doPost(e) {
  try {
    if (!e || !e.postData || !e.postData.contents) {
      return jsonResponse({ ok: false, error: 'EMPTY_BODY' }, 400);
    }

    const payload = JSON.parse(e.postData.contents);
    const validationError = validatePayload(payload);
    if (validationError) {
      return jsonResponse({ ok: false, error: validationError }, 400);
    }

    const sheet = getOrCreateSheet_();
    ensureHeader_(sheet);

    const createdAt = normalizeCreatedAt_(payload.createdAt);
    const optionalContext = JSON.stringify(payload.optionalContext || {});

    sheet.appendRow([
      createdAt,
      payload.type,
      payload.message,
      payload.email || '',
      payload.appVersion || '',
      payload.locale || '',
      payload.platform || 'android',
      optionalContext,
    ]);

    return jsonResponse({ ok: true }, 200);
  } catch (error) {
    return jsonResponse(
      {
        ok: false,
        error: 'INTERNAL_ERROR',
        message: String(error && error.message ? error.message : error),
      },
      500
    );
  }
}

function validatePayload(payload) {
  if (!payload || typeof payload !== 'object') return 'INVALID_JSON';

  const type = String(payload.type || '').trim().toLowerCase();
  if (type !== 'suggestion' && type !== 'problem') return 'INVALID_TYPE';

  const message = String(payload.message || '').trim();
  if (!message) return 'MESSAGE_REQUIRED';
  if (message.length < 8) return 'MESSAGE_TOO_SHORT';

  const email = String(payload.email || '').trim();
  if (email && !/^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(email)) {
    return 'INVALID_EMAIL';
  }

  return null;
}

function getOrCreateSheet_() {
  const spreadsheet = SpreadsheetApp.openById(SHEET_ID);
  return spreadsheet.getSheetByName(SHEET_NAME) || spreadsheet.insertSheet(SHEET_NAME);
}

function ensureHeader_(sheet) {
  if (sheet.getLastRow() > 0) return;

  sheet.appendRow([
    'createdAt',
    'type',
    'message',
    'email',
    'appVersion',
    'locale',
    'platform',
    'optionalContext',
  ]);
}

function normalizeCreatedAt_(value) {
  if (typeof value === 'number') {
    return new Date(value).toISOString();
  }

  if (typeof value === 'string' && value.trim()) {
    const parsed = new Date(value);
    if (!isNaN(parsed.getTime())) {
      return parsed.toISOString();
    }
  }

  return new Date().toISOString();
}

function jsonResponse(body, statusCode) {
  return ContentService
    .createTextOutput(JSON.stringify(body))
    .setMimeType(ContentService.MimeType.JSON);
}
