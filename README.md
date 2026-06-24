  El Infiltrado

Android app para jugar presencialmente a un juego de infiltrado con **un solo movil** que se pasa entre jugadores.

   Que incluye
- Flujo completo: Home -> Setup -> Reveal jugador por jugador -> Round Ready -> Result.
- Modo de revelado seguro con **mantener pulsado para revelar**.
- 3 a 12 jugadores.
- 1 o 2 infiltrados (2 habilitado desde 6 jugadores).
- Dataset local offline por categorias.
- Persistencia local con DataStore:
  - ajustes,
  - historial reciente,
  - estadisticas basicas,
  - ultimas configuraciones.
- Historial de rondas.
- Pantallas de ayuda, ajustes y creditos.
- Tema claro/oscuro/sistema + seleccion de idioma.
- Seguridad visual en pantallas sensibles (`FLAG_SECURE` opcional).
- Build debug/release y tests unitarios.

   Stack tecnico
- Kotlin
- Jetpack Compose + Material 3
- Navigation Compose
- Clean Architecture pragmatica + MVVM
- Coroutines + Flow/StateFlow
- Hilt
- DataStore Preferences
- Kotlinx Serialization
- Gradle Kotlin DSL + Version Catalog

   Modulos
- `app`: UI, navegacion, ViewModel, tema, recursos y empaquetado Android.
- `domain`: modelos, contratos y casos de uso puros.
- `data`: implementaciones de repositorio, DataStore y carga de dataset local.

   Estructura
- `app/src/main/java/com/impostorparty/app/...`
- `domain/src/main/kotlin/com/impostorparty/domain/...`
- `data/src/main/java/com/impostorparty/data/...`
- `data/src/main/assets/words_v1.json`
- `docs/` documentacion de arquitectura, dise�o y marketing.

   Requisitos
- Android Studio moderno (JBR incluido)
- Android SDK instalado (se usa `sdk.dir` en `local.properties`)

   Compilar y ejecutar
1. Abrir `D:\Proyectos\impostor-android` en Android Studio.
2. Esperar sync de Gradle.
3. Ejecutar configuracion `app` en un emulador o dispositivo.

    CLI (PowerShell)
```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
$env:GRADLE_USER_HOME='D:\Proyectos\impostor-android\.gradle-user-home'
.\gradlew.bat :app:assembleDebug
```

   Tests
```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
$env:GRADLE_USER_HOME='D:\Proyectos\impostor-android\.gradle-user-home'
.\gradlew.bat :domain:test :app:testDebugUnitTest
```

   Verificacion realizada
Comandos ejecutados y en verde:
- `:domain:test`
- `:app:testDebugUnitTest`
- `:app:assembleDebug`
- `:app:assembleRelease`

   Dataset y contenido
El dataset inicial esta en `data/src/main/assets/words_v1.json` con categorias:
- comida
- animales
- profesiones
- objetos
- lugares
- peliculas/series
- deportes
- paises/ciudades
- naturaleza
- cultura general

    Como anadir nuevas palabras o categorias
1. Editar `data/src/main/assets/words_v1.json`.
2. Mantener `code` de categoria estable.
3. Anadir strings de nombre de categoria en `app/src/main/res/values*/strings.xml`.
4. Si creas categoria nueva, agregarla en:
   - `domain/model/Category.kt`
   - `app/util/UiMappings.kt`

   Idiomas
Recursos incluidos:
- `values` (en)
- `values-es`
- `values-fr`
- `values-de`
- `values-it`
- `values-pt`
- `values-ja`

   Publicacion Play
Ver:
- `docs/privacy-policy.md`
- `docs/terms-and-conditions.md`
- `docs/index.md` para GitHub Pages
- `docs/marketing/play-store-es.md`
- `docs/marketing/play-store-en.md`
- `docs/marketing/screenshot-guide.md`
- `docs/release-checklist.md`

   Notas
- App 100% offline para la experiencia principal de juego.
- Sin login.
- Sin permisos peligrosos.
