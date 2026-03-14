# Privacy Policy (Draft)

El Impostor funciona principalmente offline.

## Datos recopilados
- La app no requiere cuenta ni login.
- La app no envia datos a servidores durante el juego normal.
- El usuario puede enviar voluntariamente una sugerencia o reportar un problema desde la pantalla de feedback.

## Datos de feedback enviados solo si el usuario pulsa Enviar
Cuando el usuario decide enviar feedback, la app manda estos campos:
- tipo de feedback: sugerencia o problema,
- mensaje escrito por el usuario,
- email opcional si el usuario quiere incluirlo,
- version de la app,
- idioma actual,
- timestamp,
- plataforma = android,
- contexto basico de partida si ya esta disponible, como `clueRounds` o `playerCount`.

El feedback se envia a una Web App de Google Apps Script y se guarda en una Google Sheet gestionada por el responsable de la app.

## Datos que no se envian
- historial completo de partidas,
- palabra secreta,
- logs detallados del dispositivo,
- identificadores persistentes del dispositivo,
- contactos u otra informacion sensible del usuario.

## Datos almacenados localmente
La app guarda solo en el dispositivo:
- preferencias de usuario,
- historial local de partidas,
- estadisticas basicas,
- palabras recientes para evitar repeticion,
- senales locales del flujo de review y feedback para no mostrar prompts en exceso.

## Comparticion con terceros
- El envio de feedback utiliza Google Apps Script y Google Sheets como infraestructura tecnica.
- Fuera de ese envio voluntario, no hay comparticion adicional de datos para jugar.

## Limitaciones
- El canal de feedback depende de cuotas y disponibilidad de Google Apps Script y Google Sheets.
- El email es opcional y solo debe rellenarse si el usuario quiere facilitar una respuesta.

## Menores
- La app esta pensada para juego social casual.

## Contacto
- Anadir email de soporte antes de publicar en Play Store.
