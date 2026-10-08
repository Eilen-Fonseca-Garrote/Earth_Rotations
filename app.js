const container = document.getElementById('container');

// Renderer
const renderer = new THREE.WebGLRenderer({ antialias: true });
renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
renderer.setSize(window.innerWidth, window.innerHeight);
renderer.outputEncoding = THREE.sRGBEncoding;
container.appendChild(renderer.domElement);

// Scene + Camera
const scene = new THREE.Scene();
scene.fog = new THREE.FogExp2(0x000000, 0.0008);

const camera = new THREE.PerspectiveCamera(45, window.innerWidth / window.innerHeight, 0.1, 2000);
let cameraDistance = 6;
const minDistance = 2.2;
const maxDistance = 12;
camera.position.set(0, 0, cameraDistance);

// Lights
scene.add(new THREE.AmbientLight(0xffffff, 0.45));
const dir = new THREE.DirectionalLight(0xffffff, 1.0);
dir.position.set(5, 3, 5);
scene.add(dir);

// Textures
const loader = new THREE.TextureLoader();
const earthMap = loader.load('https://threejs.org/examples/textures/planets/earth_atmos_2048.jpg');
const cloudsMap = loader.load('https://threejs.org/examples/textures/planets/earth_clouds_1024.png');

// Earth group
const earthGroup = new THREE.Group();
scene.add(earthGroup);

const RADIUS = 1.8;
const earthGeo = new THREE.SphereGeometry(RADIUS, 64, 64);
const earthMat = new THREE.MeshPhongMaterial({ map: earthMap });
const earthMesh = new THREE.Mesh(earthGeo, earthMat);
earthGroup.add(earthMesh);

const cloudGeo = new THREE.SphereGeometry(RADIUS + 0.02, 64, 64);
const cloudMat = new THREE.MeshLambertMaterial({ map: cloudsMap, transparent: true, opacity: 0.9, depthWrite: false });
const cloudMesh = new THREE.Mesh(cloudGeo, cloudMat);
earthGroup.add(cloudMesh);

// Stars background
(function createStars() {
  const starGeometry = new THREE.BufferGeometry();
  const starCount = 5000;
  const positions = new Float32Array(starCount * 3);
  for (let i = 0; i < starCount; i++) {
    const i3 = i * 3;
    const r = 100 + Math.random() * 400;
    const theta = Math.random() * Math.PI * 2;
    const phi = Math.acos(2 * Math.random() - 1);
    positions[i3] = r * Math.sin(phi) * Math.cos(theta);
    positions[i3 + 1] = r * Math.sin(phi) * Math.sin(theta);
    positions[i3 + 2] = r * Math.cos(phi);
  }
  starGeometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
  const starMaterial = new THREE.PointsMaterial({ color: 0xffffff, size: 0.7, sizeAttenuation: true });
  scene.add(new THREE.Points(starGeometry, starMaterial));
})();

// Interaction state (custom controls)
let isPointerDown = false;
let lastX = 0;
let lastY = 0;
let targetRotX = 0;
let targetRotY = 0;
const rotSmoothing = 0.12;
let autoRotate = false;

// Initial tilt for a more engaging view.
earthGroup.rotation.x = 0.4;
earthGroup.rotation.y = 0.4;

// Pointer handlers on canvas
renderer.domElement.addEventListener('pointerdown', (event) => {
  isPointerDown = true;
  lastX = event.clientX;
  lastY = event.clientY;
  renderer.domElement.setPointerCapture(event.pointerId);
});
renderer.domElement.addEventListener('pointermove', (event) => {
  if (!isPointerDown) return;
  const dx = event.clientX - lastX;
  const dy = event.clientY - lastY;
  lastX = event.clientX;
  lastY = event.clientY;
  targetRotY += dx * 0.005;
  targetRotX += dy * 0.005;
  targetRotX = Math.max(-Math.PI / 2 + 0.1, Math.min(Math.PI / 2 - 0.1, targetRotX));
});
renderer.domElement.addEventListener('pointerup', (event) => {
  isPointerDown = false;
  try {
    renderer.domElement.releasePointerCapture(event.pointerId);
  } catch {
    // The pointer capture may already have been released by the browser.
  }
});
renderer.domElement.addEventListener('pointercancel', () => {
  isPointerDown = false;
});

// Wheel zoom
renderer.domElement.addEventListener('wheel', (event) => {
  event.preventDefault();
  cameraDistance += event.deltaY * 0.01;
  cameraDistance = Math.min(maxDistance, Math.max(minDistance, cameraDistance));
}, { passive: false });

// Touch pinch support
let lastDistance = null;
renderer.domElement.addEventListener('touchstart', (event) => {
  if (event.touches.length === 2) {
    const dx = event.touches[0].clientX - event.touches[1].clientX;
    const dy = event.touches[0].clientY - event.touches[1].clientY;
    lastDistance = Math.hypot(dx, dy);
  }
}, { passive: true });
renderer.domElement.addEventListener('touchmove', (event) => {
  if (event.touches.length === 2 && lastDistance !== null) {
    const dx = event.touches[0].clientX - event.touches[1].clientX;
    const dy = event.touches[0].clientY - event.touches[1].clientY;
    const distance = Math.hypot(dx, dy);
    const diff = lastDistance - distance;
    cameraDistance += diff * 0.01;
    cameraDistance = Math.min(maxDistance, Math.max(minDistance, cameraDistance));
    lastDistance = distance;
  }
}, { passive: true });
renderer.domElement.addEventListener('touchend', (event) => {
  if (event.touches.length < 2) lastDistance = null;
}, { passive: true });

const autoRotateButton = document.getElementById('autoRotateBtn');
const countrySelect = document.getElementById('countrySelect');
const earthquakePanel = document.getElementById('earthquakePanel');
const panelTitle = document.getElementById('panelTitle');
const panelStatus = document.getElementById('panelStatus');
const earthquakeList = document.getElementById('earthquakeList');
const API_BASE_URL = window.location.protocol === 'file:' ||
  ['localhost', '127.0.0.1'].includes(window.location.hostname)
  ? 'http://localhost:8080'
  : '';
const API_URL = `${API_BASE_URL}/api/earthquakes`;
let earthquakePanelOpen = false;

// Camera and zoom controls
document.getElementById('leftBtn').addEventListener('click', () => {
  targetRotY -= 0.35;
});
document.getElementById('rightBtn').addEventListener('click', () => {
  targetRotY += 0.35;
});
document.getElementById('upBtn').addEventListener('click', () => {
  targetRotX -= 0.25;
});
document.getElementById('downBtn').addEventListener('click', () => {
  targetRotX += 0.25;
});
document.getElementById('zoomInBtn').addEventListener('click', () => {
  cameraDistance = Math.max(minDistance, cameraDistance - 0.9);
});
document.getElementById('zoomOutBtn').addEventListener('click', () => {
  cameraDistance = Math.min(maxDistance, cameraDistance + 0.9);
});
autoRotateButton.addEventListener('click', () => {
  if (earthquakePanelOpen) return;
  autoRotate = !autoRotate;
  autoRotateButton.textContent = autoRotate ? 'Auto Rotate: ON' : 'Toggle Auto Rotate';
});
document.getElementById('resetBtn').addEventListener('click', () => {
  targetRotX = 0.4;
  targetRotY = 0.4;
  cameraDistance = 6;
  autoRotate = false;
  autoRotateButton.textContent = 'Toggle Auto Rotate';
});

async function loadCountries() {
  try {
    const response = await fetch(`${API_BASE_URL}/api/countries`);
    if (!response.ok) throw new Error('No se pudo cargar el catálogo');
    const countries = await response.json();
    countries.sort((a, b) => a.name.common.localeCompare(b.name.common));
    countries.forEach((country) => {
      if (!country.cca2 || !country.latlng || country.latlng.length < 2) return;
      const option = document.createElement('option');
      option.value = country.cca2;
      option.textContent = country.name.common;
      option.dataset.latitude = country.latlng[0];
      option.dataset.longitude = country.latlng[1];
      countrySelect.appendChild(option);
    });
    countrySelect.querySelector('option[value=""]').textContent = 'Selecciona un país';
  } catch (error) {
    countrySelect.innerHTML = '<option value="">No se pudo cargar el catálogo</option>';
    panelStatus.textContent = 'No se pudo cargar el catálogo de países.';
  }
}

function rotateToCountry(latitude, longitude) {
  targetRotX = THREE.MathUtils.clamp(latitude * Math.PI / 180, -Math.PI / 2 + 0.1, Math.PI / 2 - 0.1);
  targetRotY = -longitude * Math.PI / 180 - Math.PI / 2;
  cameraDistance = 4.2;
}

function fitCountry(bounds, country, earthquakes) {
  const eventLongitudes = earthquakes.map((earthquake) => earthquake.longitude);
  const eventLatitudes = earthquakes.map((earthquake) => earthquake.latitude);
  const minLongitude = eventLongitudes.length ? Math.min(...eventLongitudes) : bounds?.minLongitude ?? country.longitude;
  const maxLongitude = eventLongitudes.length ? Math.max(...eventLongitudes) : bounds?.maxLongitude ?? country.longitude;
  const minLatitude = bounds?.minLatitude ?? (eventLatitudes.length ? Math.min(...eventLatitudes) : country.latitude);
  const maxLatitude = bounds?.maxLatitude ?? (eventLatitudes.length ? Math.max(...eventLatitudes) : country.latitude);
  const centerLatitude = (minLatitude + maxLatitude) / 2;
  const centerLongitude = (minLongitude + maxLongitude) / 2;
  const span = Math.max(maxLatitude - minLatitude, maxLongitude - minLongitude);
  targetRotX = THREE.MathUtils.clamp(centerLatitude * Math.PI / 180, -Math.PI / 2 + 0.1, Math.PI / 2 - 0.1);
  targetRotY = -centerLongitude * Math.PI / 180 - Math.PI / 2;
  cameraDistance = THREE.MathUtils.clamp(4.2 + span * 0.04, 4.2, 11);
}

function renderEarthquakes(earthquakes) {
  earthquakeList.innerHTML = '';
  if (!earthquakes.length) {
    earthquakeList.innerHTML = '<p class="panel-status">No hay sismos cercanos en las últimas 24 horas.</p>';
    return;
  }
  earthquakes.forEach((earthquake) => {
    const item = document.createElement('article');
    item.className = 'earthquake-item';
    item.innerHTML = `<strong>Magnitud ${earthquake.magnitude.toFixed(1)}</strong> · ${earthquake.place || 'Ubicación desconocida'}<small>Profundidad: ${earthquake.depth.toFixed(1)} km · ${new Date(earthquake.occurredAt).toLocaleString()}</small>`;
    earthquakeList.appendChild(item);
  });
}

async function showCountryEarthquakes(country) {
  earthquakePanel.hidden = false;
  panelTitle.textContent = `Sismos en ${country.name}`;
  panelStatus.textContent = 'Consultando los últimos sismos...';
  earthquakeList.innerHTML = '';
  try {
    const [earthquakeResponse, boundsResponse] = await Promise.all([
      fetch(`${API_URL}?country=${encodeURIComponent(country.code)}`),
      fetch(`${API_BASE_URL}/api/countries/${encodeURIComponent(country.code)}/bounds`)
    ]);
    if (!earthquakeResponse.ok || !boundsResponse.ok) throw new Error('API no disponible');
    const earthquakes = await earthquakeResponse.json();
    const bounds = await boundsResponse.json();
    fitCountry(bounds, country, earthquakes);
    panelStatus.textContent = `${earthquakes.length} eventos encontrados en el país.`;
    renderEarthquakes(earthquakes);
  } catch (error) {
    panelStatus.textContent = 'No se pudo conectar con Spring Boot. Comprueba que el backend esté activo.';
  }
}

countrySelect.addEventListener('change', () => {
  const option = countrySelect.selectedOptions[0];
  if (!option.value) return;
  earthquakePanelOpen = true;
  autoRotate = false;
  earthMesh.rotation.y = 0;
  cloudMesh.rotation.y = 0;
  autoRotateButton.textContent = 'Auto Rotate: OFF';
  const country = {
    name: option.textContent,
    code: option.value,
    latitude: Number(option.dataset.latitude),
    longitude: Number(option.dataset.longitude)
  };
  rotateToCountry(country.latitude, country.longitude);
  showCountryEarthquakes(country);
});

document.getElementById('closePanel').addEventListener('click', () => {
  earthquakePanel.hidden = true;
  earthquakePanelOpen = false;
  autoRotate = true;
  autoRotateButton.textContent = 'Auto Rotate: ON';
});

loadCountries();

// Animation loop
const clock = new THREE.Clock();
function animate() {
  const dt = clock.getDelta();

  if (autoRotate) {
    targetRotY += 0.0018 * dt * 60;
  }

  earthGroup.rotation.x += (targetRotX - earthGroup.rotation.x) * rotSmoothing;
  earthGroup.rotation.y += (targetRotY - earthGroup.rotation.y) * rotSmoothing;

  camera.position.set(0, 0, cameraDistance);
  camera.lookAt(new THREE.Vector3(0, 0, 0));

  renderer.render(scene, camera);
  requestAnimationFrame(animate);
}
animate();

window.addEventListener('resize', () => {
  camera.aspect = window.innerWidth / window.innerHeight;
  camera.updateProjectionMatrix();
  renderer.setSize(window.innerWidth, window.innerHeight);
});

targetRotX = earthGroup.rotation.x;
targetRotY = earthGroup.rotation.y;
