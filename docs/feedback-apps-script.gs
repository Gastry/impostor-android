const SHEET_ID = 'PASTE_YOUR_GOOGLE_SHEET_ID_HERE';
const SHEET_NAME = 'Feedback';
const SHARED_TOKEN = 'REPLACE_WITH_A_LONG_SHARED_TOKEN';
const MIN_MESSAGE_LENGTH = 8;
const MAX_MESSAGE_LENGTH = 1200;
const MIN_SHARED_TOKEN_LENGTH = 16;
const LOCALE_REGEX = /^[A-Za-z]{2,3}([_-][A-Za-z0-9]{2,8})*$/;

function doPost(e) {
  try {
    if (!e || !e.postData || !e.postData.contents) {
      return jsonResponse({ ok: false, error: 'EMPTY_BODY' });
    }

    const payload = JSON.parse(e.postData.contents);
    const validationError = validatePayload(payload);
    if (validationError) {
      return jsonResponse({ ok: false, error: validationError });
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

    return jsonResponse({ ok: true });
  } catch (error) {
    return jsonResponse(
      {
        ok: false,
        error: 'INTERNAL_ERROR',
        message: String(error && error.message ? error.message : error),
      }
    );
  }
}

function validatePayload(payload) {
  if (!payload || typeof payload !== 'object') return 'INVALID_JSON';
  if (typeof SHARED_TOKEN !== 'string' || SHARED_TOKEN.trim().length < MIN_SHARED_TOKEN_LENGTH) {
    return 'TOKEN_NOT_CONFIGURED';
  }

  const type = String(payload.type || '').trim().toLowerCase();
  if (type !== 'suggestion' && type !== 'problem') return 'INVALID_TYPE';

  const message = String(payload.message || '').trim();
  if (!message) return 'MESSAGE_REQUIRED';
  if (message.length < MIN_MESSAGE_LENGTH) return 'MESSAGE_TOO_SHORT';
  if (message.length > MAX_MESSAGE_LENGTH) return 'MESSAGE_TOO_LONG';

  const email = String(payload.email || '').trim();
  if (email && !/^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(email)) {
    return 'INVALID_EMAIL';
  }

  const token = String(payload.token || '').trim();
  if (token !== SHARED_TOKEN.trim()) return 'UNAUTHORIZED';

  const appVersion = String(payload.appVersion || '').trim();
  if (!appVersion || appVersion.length > 64) return 'INVALID_APP_VERSION';

  const locale = String(payload.locale || '').trim();
  if (!locale || locale.length > 35 || !LOCALE_REGEX.test(locale)) return 'INVALID_LOCALE';

  if (String(payload.platform || '').trim().toLowerCase() !== 'android') return 'INVALID_PLATFORM';

  const createdAt = normalizeCreatedAt_(payload.createdAt);
  if (!createdAt) return 'INVALID_CREATED_AT';

  if (!isValidOptionalContext_(payload.optionalContext)) return 'INVALID_OPTIONAL_CONTEXT';

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
    const parsed = new Date(value);
    return isNaN(parsed.getTime()) ? null : parsed.toISOString();
  }

  if (typeof value === 'string' && value.trim()) {
    const parsed = new Date(value);
    if (!isNaN(parsed.getTime())) {
      return parsed.toISOString();
    }
  }

  return null;
}

function isValidOptionalContext_(optionalContext) {
  if (optionalContext == null) return true;
  if (typeof optionalContext !== 'object' || Array.isArray(optionalContext)) return false;

  const clueRounds = optionalContext.clueRounds;
  if (clueRounds != null && (!Number.isInteger(clueRounds) || clueRounds < 1 || clueRounds > 3)) {
    return false;
  }

  const playerCount = optionalContext.playerCount;
  if (playerCount != null && (!Number.isInteger(playerCount) || playerCount < 3 || playerCount > 20)) {
    return false;
  }

  return true;
}

function jsonResponse(body) {
  return ContentService
    .createTextOutput(JSON.stringify(body))
    .setMimeType(ContentService.MimeType.JSON);
}
