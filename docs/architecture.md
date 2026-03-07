# Arquitectura

## Enfoque
- Clean Architecture pragmatica con separacion `app / domain / data`.
- UDF en presentacion: `StateFlow` + eventos hacia ViewModel.
- Casos de uso centrados en:
  - validacion de setup,
  - seleccion de palabra,
  - asignacion de roles,
  - transicion de estado de revelado.

## Capas
- `domain`: reglas puras y contratos.
- `data`: implementacion offline con DataStore + JSON local.
- `app`: UI Compose, navegacion, tema, strings, estado de sesion.

## Estado de partida
- `GameViewModel` mantiene:
  - configuracion actual,
  - ronda activa,
  - estado de revelado (`PassingPhone`, `RevealingSecret`, `RoundReady`),
  - mensajes de error,
  - ganador seleccionado.

## Persistencia
- `DataStorePreferencesRepository`
  - ajustes globales,
  - ultima configuracion,
  - lista de palabras recientes.
- `DataStoreStatsRepository`
  - historial de rondas,
  - estadisticas agregadas.

## Seguridad
- En rutas sensibles (`Reveal`, `Result`) se puede activar `FLAG_SECURE` desde ajustes.