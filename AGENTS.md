# Repository Instructions

- Se critico, constructivo y conciso.
- No inventes nada si no te lo piden explicitamente. Si algo no se sabe, dilo.
- No des la razon al usuario sin base.
- `app/src/main/res/**/strings.xml` se deben editar preservando UTF-8. No uses rutas de edicion que puedan recodificar texto sin control.
- No uses `PowerShell Set-Content`, `Out-File` ni scripts equivalentes para reescribir `strings.xml` salvo que preserven explicitamente UTF-8 y se valide despues.
- Tras tocar cualquier `strings.xml`, ejecuta `:app:checkStringsEncoding`.
- Antes de cerrar cambios que toquen traducciones o textos, intenta tambien `:app:assembleDebug`.

## Publicacion Android

- Antes de generar artefactos para publicar, incrementa siempre `appVersionCode` en `app/build.gradle.kts`.
- `versionName` debe seguir el formato `1.{versionCode}`.
- Para generar el bundle release, usa `:app:bundleRelease`.
- En este proyecto, `:app:bundleRelease` ejecuta `signReleaseBundle`, asi que el `.aab` de `app/build/outputs/bundle/release/app-release.aab` sale firmado.
- Keystore de subida conocida: `signing/ElImpostorUpload.jks`
- Alias conocido de la keystore: `gastry`
- No guardes contraseñas en el repo ni en `AGENTS.md`.
- Si hace falta un artefacto final para compartir o subir manualmente, copialo a `release-artifacts/` con nombre que incluya version y fecha.
