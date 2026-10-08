# Publicar TerraQuake en Render

El servicio de Render publica en una misma URL el mapa, la página inicial y la
API Spring Boot. MongoDB debe ser una instancia administrada en MongoDB Atlas;
no se debe usar `localhost` para la base de datos en producción.

## Preparar MongoDB Atlas

1. Crea un clúster y un usuario de base de datos en MongoDB Atlas.
2. En **Network Access**, permite conexiones desde Render. Si no tienes una
   lista de IP de salida fija, permite `0.0.0.0/0` y usa una contraseña robusta
   y exclusiva para ese usuario.
3. Copia la URI de conexión de la aplicación, reemplaza la contraseña y añade
   `/earth_rotations` como nombre de base de datos. Si la contraseña contiene
   caracteres especiales, codifícala para URL.

## Crear el servicio en Render

1. Sube este repositorio a GitHub y conéctalo a Render.
2. En Render, selecciona **New > Blueprint** y elige el repositorio. Render
   detectará `render.yaml` y construirá el servicio Docker.
3. Cuando lo solicite, define `MONGODB_URI` con la URI de Atlas. Es un secreto:
   configúralo en Render, no lo escribas en el repositorio.
4. Espera a que el despliegue quede **Live**. Render proporciona una URL
   pública `https://...onrender.com`.
5. Abre esa URL para ver el mapa. La página inicial está en
   `/Pantalla%20inicial/index1.html`; su botón **Explorar el mapa** abre el mapa.
   Comprueba también `/actuator/health`, que debe responder con `status: UP`.

El plan gratuito de Render puede suspender el servicio cuando está inactivo; la
primera visita posterior puede tardar mientras vuelve a arrancar. La aplicación
también necesita conexión a Internet para cargar Three.js y las texturas del
globo desde sus CDN.
