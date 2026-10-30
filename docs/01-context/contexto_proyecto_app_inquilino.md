# Contexto del proyecto

## 1. Propósito del documento

Este documento describe el contexto funcional inicial de una aplicación móvil dirigida al inquilino, destinada a registrar información básica de su alquiler, consultar sus obligaciones y recibir recordatorios de pago.

Este contexto servirá como entrada para la etapa posterior de análisis, documentación de requisitos, diseño y arquitectura.

**Este documento no constituye una especificación técnica ni una aprobación de arquitectura.** Las tecnologías mencionadas como posibles referencias deberán confirmarse durante el diseño técnico.

## 2. Objetivo de la aplicación

Permitir que un inquilino gestione localmente la información básica de su alquiler, consulte las fechas y montos de sus obligaciones en un calendario y reciba notificaciones configurables antes de sus vencimientos.

La aplicación debe poder utilizarse con y sin conexión a Internet.

## 3. Usuario objetivo

El usuario principal es el inquilino que utiliza la aplicación para llevar un registro personal de su inmueble alquilado y de las obligaciones asociadas.

La aplicación no está planteada, en su alcance inicial, como un sistema de administración para arrendadores o propietarios.

## 4. Funcionalidades principales

### 4.1 Registro del inquilino

Permitir registrar información básica del usuario:

- Nombre completo.
- Teléfono (opcional).
- Correo electrónico (opcional).

### 4.2 Registro del inmueble

Permitir registrar información básica de la propiedad alquilada:

- Nombre o descripción del inmueble.
- Dirección.
- Nombre del arrendador (opcional).

### 4.3 Obligaciones de alquiler

Permitir registrar las obligaciones periódicas de alquiler:

- Monto del alquiler.
- Fecha de pago mensual.
- Frecuencia: mensual.
- Estado de la obligación.

### 4.4 Calendario de obligaciones

Proporcionar una vista mensual de las obligaciones del inquilino que permita:

- Visualizar las fechas de pago.
- Identificar obligaciones próximas.
- Consultar el monto a pagar.
- Diferenciar obligaciones pendientes y vencidas.

### 4.5 Configuración de notificaciones

Permitir que el usuario seleccione cuándo recibir recordatorios de pago:

- 3 días antes.
- 2 días antes.
- 1 día antes.
- El mismo día del pago.

También se contempla permitir seleccionar la hora de las notificaciones.

## 5. Funcionalidades complementarias previstas para uso local

Las siguientes funcionalidades pueden operar sin requerir un servidor.

### 5.1 Generación automática de obligaciones recurrentes

El usuario registra el monto y el día de pago una vez. La aplicación genera las fechas de obligaciones mensuales automáticamente.

Ejemplos mencionados: alquiler, cuota de mantenimiento o servicios incluidos en el alquiler.

### 5.2 Recordatorios personalizados

Permitir activar o desactivar cada recordatorio y seleccionar la hora de notificación.

### 5.3 Calendario con estados

Diferenciar visualmente:

- Obligación próxima.
- Obligación de hoy.
- Obligación vencida.
- Obligación marcada como pagada.

### 5.4 Exportación y respaldo de información

Permitir generar un archivo de respaldo que incluya la información del inquilino, inmueble y obligaciones.

El propósito es que el usuario pueda conservar una copia de sus datos y facilitar su recuperación ante pérdida o cambio de dispositivo.

### 5.5 Resumen de obligaciones

La pantalla inicial debe presentar información resumida de las obligaciones, incluyendo:

- Próximo pago.
- Monto que debe pagar.
- Cuántos días faltan.
- Próximas obligaciones del mes.
- Resumen de obligaciones por semana.

#### Resumen semanal

Permitir consultar las obligaciones agrupadas por semana, mostrando:

- Semana correspondiente.
- Fechas de vencimiento.
- Monto total de las obligaciones de esa semana.
- Estado de las obligaciones: pendientes, vencidas o pagadas.

El resumen semanal debe derivarse de las obligaciones registradas y sus fechas de vencimiento. No requiere conexión a Internet.

## 6. Funcionalidades que utilizan aplicaciones externas y pueden requerir Internet

### 6.1 Contacto con el arrendador

Permitir guardar datos de contacto del arrendador y abrir una aplicación externa para:

- WhatsApp.
- Llamada telefónica.
- Correo electrónico.

La aplicación facilitará el acceso a las aplicaciones disponibles en el dispositivo. No se ha definido que la aplicación gestione directamente mensajes, llamadas o correos.

## 7. Restricciones y principios del alcance inicial

- La aplicación debe poder utilizarse con y sin Internet.
- La información del usuario, inmueble y obligaciones debe almacenarse localmente.
- El alcance inicial contempla SQLite como almacenamiento local.
- No se requiere un backend para las funcionalidades principales descritas.
- El calendario, el resumen de obligaciones y las notificaciones deben funcionar localmente.
- Internet se contempla para facilitar el uso de aplicaciones externas, como WhatsApp o correo electrónico.
- No se ha definido una cuenta de usuario ni autenticación.
- No se ha definido sincronización en la nube ni entre dispositivos.

## 8. Tecnologías consideradas como referencias preliminares

Se han mencionado como posibles tecnologías para explorar:

- React Native.
- Expo.
- TypeScript.
- SQLite.
- Notificaciones locales.

Estas tecnologías son referencias preliminares y **no constituyen decisiones tecnológicas aprobadas**. La selección debe documentarse posteriormente mediante el proceso de arquitectura.

## 9. Funcionalidades fuera del alcance definido hasta el momento

No se han incluido en el contexto actual:

- Procesamiento de pagos dentro de la aplicación.
- Integración con bancos o SINPE.
- Portal para arrendadores.
- Chat o mensajería propia.
- Backend.
- Sincronización en la nube.
- Gestión multiusuario.
- IA o consultas legales.
- Reportes financieros avanzados.

Esta lista representa lo que no se ha definido como parte del alcance actual; cualquier incorporación posterior deberá evaluarse y aprobarse.

## 10. Aspectos pendientes de definición

Los siguientes aspectos requieren análisis posterior y no deben resolverse mediante suposiciones:

- Comportamiento cuando el mes no contiene el día de pago registrado.
- Regla para determinar cuándo una obligación pasa a estar vencida.
- Cómo y cuándo se marca una obligación como pagada.
- Si se permite modificar el monto o la fecha de pago y cómo afecta a obligaciones futuras.
- Horizonte de generación de obligaciones recurrentes.
- Reglas exactas para agrupar fechas en semanas y límites de cada semana.
- Formato y contenido del archivo de respaldo.
- Requisitos de permisos y comportamiento de las notificaciones en Android y/o iOS.
- Plataforma móvil objetivo y requisitos de distribución.
- Necesidades de privacidad y protección de datos.
- Reglas de accesibilidad, idioma y formato de moneda.

## 11. Orientación para la siguiente etapa

Este contexto debe utilizarse para iniciar el análisis y la documentación del proyecto.

La siguiente etapa deberá:

1. Revisar y confirmar el alcance funcional.
2. Identificar requisitos funcionales y no funcionales.
3. Registrar preguntas y decisiones pendientes.
4. Definir criterios de aceptación verificables.
5. Evaluar y documentar la arquitectura y las tecnologías.
6. Mantener trazabilidad entre el contexto, los requisitos, las decisiones y las pruebas.

No se debe implementar funcionalidad ni tomar decisiones técnicas definitivas basándose únicamente en este documento.
