# Publicar TerraQuake en Render

El `Dockerfile` de la raíz empaqueta el mapa, la página inicial y la API Spring
Boot en un solo servicio. El Blueprint `render.yaml` permite a Render construir
y ejecutar la aplicación. MongoDB debe ser una instancia administrada en
MongoDB Atlas; no uses `localhost` para la base de datos en producción.

## Preparar MongoDB Atlas

1. Crea un clúster y un usuario de base de datos en MongoDB Atlas.
2. En **Network Access**, permite conexiones desde Render. Si no tienes una
   lista de IP de salida fija, permite `0.0.0.0/0` y usa una contraseña fuerte,
   exclusiva para ese usuario.
3. Copia la URI de conexión de la aplicación, reemplaza la contraseña y añade
   `/earth_rotations` como nombre de base de datos antes de los parámetros
   (`?`). Si la contraseña contiene caracteres especiales, codifícala para URL.

## Crear o actualizar el servicio en Render

1. Sube los cambios de este repositorio a GitHub y conecta la rama `main` a
   Render. Si el servicio anterior sigue en tu cuenta, úsalo; no es necesario
   crear otro.
2. Para crear uno nuevo, selecciona **New > Blueprint** y elige el repositorio.
   Render detectará `render.yaml` y definirá el servicio Docker.
3. Cuando Render solicite `SPRING_DATA_MONGODB_URI`, pega la URI completa de
   Atlas. Debe incluir `/earth_rotations` antes de los parámetros de la URI. Es
   un secreto: configúralo en Render, no lo escribas en el repositorio.
4. Espera a que el despliegue quede **Live**. Render proporciona una URL
   pública `https://...onrender.com`.
5. Abre esa URL para ver el mapa. La página inicial está en
   `/Pantalla%20inicial/index1.html`; su botón **Explorar el mapa** abre el mapa.
   Comprueba también `/actuator/health`, que debe responder con `status: UP`.

El plan gratuito de Render puede suspender el servicio cuando está inactivo; la
primera visita posterior puede tardar mientras vuelve a arrancar. La aplicación
también necesita Internet para cargar Three.js y las texturas del globo desde
sus CDN.
