# Model Release Cloud — Android

Proyecto Android Studio que integra la versión `model_release_formato_arcangel.html`.

## Compilar APK
1. Abrir esta carpeta en Android Studio (versión reciente con JDK 17 y Android SDK 35).
2. Esperar la sincronización Gradle.
3. Build > Build Bundle(s) / APK(s) > Build APK(s).
4. APK de depuración: `app/build/outputs/apk/debug/app-debug.apk`.
5. Transferir al teléfono e instalar, permitiendo la instalación desde la fuente correspondiente.

## Características
- Supabase Auth y contratos, con conexión a Internet.
- Firma táctil y fotografía por selector de archivos.
- Exportación PDF a Descargas/ModelReleaseCloud mediante puente nativo.
- Contenido HTML empaquetado en `app/src/main/assets/index.html`.

## Consideraciones
- No se incluye APK compilada: el entorno de generación no dispone de Android SDK/Gradle.
- Dependencias de Tailwind, Supabase, jsPDF y html2canvas se cargan desde CDN; necesita Internet.
- Validar en un dispositivo físico el login, enlaces de firma, selector de fotos y PDF.
- Las URL externas se abren fuera de la app; los enlaces públicos de firma se abren en el navegador.
- Este proyecto es una base funcional, no una auditoría de seguridad ni un paquete listo para Play Store.
