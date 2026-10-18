[README.md](https://github.com/user-attachments/files/33623889/README.md)
# 🔫 Cobblegun

<div align="center">

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen?logo=minecraft&style=for-the-badge)
![Modloaders](https://img.shields.io/badge/Modloader-Fabric%20%7C%20NeoForge-orange?style=for-the-badge)
![Cobblemon](https://img.shields.io/badge/Cobblemon-1.5.2+-red?style=for-the-badge)
![License](https://img.shields.io/badge/License-LGPL--3.0-blue?style=for-the-badge)

**La suite client-side definitiva para Cobblemon.**  
Combates automatizados con IA táctica, radar tridimensional de Pokédex y Shinies, alertas auditivas/chat, HUD de ratio de captura y controles rápidos de equipo.

</div>

---

## 📖 Descripción General

**Cobblegun** transforma la experiencia de Cobblemon añadiendo una completa suite de utilidades ejecutadas **100% en el cliente**. Olvídate de los mods de auto-batalla básicos que eligen movimientos al azar: Cobblegun analiza tipos, debilidades elementales, cambios tácticos y protege a los Pokémon salvajes raros de ser debilitados accidentalmente.

---

## ⚡ Características Principales

### 🧠 1. IA de Combate Táctica e Inteligente
* **Cálculo de Efectividades:** Selecciona el ataque óptimo calculando ventajas de tipos elementales, bonificación por mismo tipo (STAB), daño base y PP restantes.
* **Anti-Spam de Movimientos de Estado:** Si un ataque de estado falla o el rival ya tiene un estado alterado (sueño, parálisis, etc.), la IA no lo spameará indefinidamente.
* **Cambio Táctico (Smart Switching):** Si tu Pokémon activo entra en estado crítico (< 30% HP) y tienes un compañero en la banca con ventaja elemental clara ($\ge 2\times$), la IA realizará un relevo táctico.
* **Jitter Antidetección (Delays humanos):** Tiempos de respuesta configurables con variación aleatoria (*jitter*) para simular clics humanos y evitar comportamientos robóticos.

### 🎯 2. Modo Caza y Captura Segura (CobbleHunt)
* **Protección contra KO accidental:** Bloquea ataques letales cuando el Pokémon salvaje tiene poca vida, evitando debilitarlo si está en tu lista de caza o si es Shiny/Legendario.
* **Prioridad de Falso Tortazo y Estados:** Prioriza automáticamente movimientos como *Falso Tortazo (False Swipe)* e inductores de estado cuando el objetivo debe ser capturado.
* **Listas Blancas y Negras:** Configura listas de especies prioritarias para cazar.

### 📡 3. Radar de Pokédex y Rastreador Tridimensional (Tracers)
* **Radar Inteligente de Dex:** Marca en el mundo Pokémon salvajes que **no tengas registrados** en la Pokédex (nuevas especies o nuevas formas), ignorando variantes de género repetidas para evitar falsas alarmas.
* **Tracers y Beacons:** Líneas visuales y balizas de luz hacia Shinies, Legendarios y Pokémon no capturados con colores personalizables.
* **Alerta Shiny Completa:** Reproduce un sonido característico inconfundible (`shiny.ogg`) y envía al chat un aviso con:
  * Especie y nivel.
  * Coordenadas exactas $(X, Y, Z)$.
  * Dirección cardinal relativa (Norte, Sur, Este, Oeste).

### 📊 4. HUDs en Tiempo Real y Apuntado Elemental
* **HUD de Combate:** Muestra el porcentaje de vida, estado de combate y probabilidad de captura en vivo.
* **Crosshair Elemental (Cobbleit):** Al apuntar con la mira a cualquier Pokémon salvaje en el mundo, muestra sobre la cruceta sus tipos elementales y debilidades.

### 👥 5. Acciones Rápidas de Equipo (Party Controls)
* **Lanzar todo el equipo:** Saca a todos los miembros vivos de tu equipo al mundo con una sola tecla.
* **Recoger todo el equipo:** Guarda a todos tus Pokémon al instante.

### 🏃 6. Anti-AFK Integrado
* Modos *Sprint Runner* y *Micro Step* para evitar desconexiones por inactividad en servidores mientras vigilas apariciones.

---

## ⌨️ Controles y Teclas por Defecto

> 💡 *Todas las teclas son 100% configurables en **Opciones ➔ Controles ➔ Asignación de teclas** dentro de la categoría **Cobblegun**.*

| Tecla | Acción | Descripción |
| :---: | :--- | :--- |
| <kbd>K</kbd> | **Alternar Auto-Batalla** | Activa o desactiva la selección automática de acciones en combate. |
| <kbd>O</kbd> | **Menú de Configuración (GUI)** | Abre la pantalla interactiva de opciones dentro del juego. |
| <kbd>P</kbd> | **Pánico / Pausa de Emergencia** | Detiene de inmediato cualquier acción automática y limpia alertas. |
| <kbd>J</kbd> | **Ciclar Anti-AFK** | Alterna entre *Desactivado ➔ Sprint Runner ➔ Micro Step*. |
| <kbd>F5</kbd> | **Lanzar todo el equipo** | Saca a todos los Pokémon de tu party al mundo. |
| <kbd>F6</kbd> | **Guardar todo el equipo** | Regresa a todos tus Pokémon a sus Pokéballs. |

---

## ⚙️ Menú de Configuración

Puedes acceder a la configuración pulsando <kbd>O</kbd> dentro del juego o editando directamente el archivo:  
`config/cobblemon_autobattle-client.toml`

Entre las opciones más destacadas:
* **Tiempos de espera:** `base_delay`, `min_jitter` y `max_jitter` (milisegundos).
* **IA y Combate:** `smart_switching`, `force_move`, `auto_catch_enabled`.
* **Filtros de Caza:** `protect_shiny`, `protect_legendary`, `species_whitelist`, `species_blacklist`.
* **Personalización visual:** Escalas, posiciones de HUD y colores hexadecimales de los tracers (Shiny, Legendario, Faltante en Dex).

---

## 📥 Instalación y Dependencias

Cobblegun es un mod **Client-Side** (solo necesitas instalarlo en tu cliente de Minecraft; no requiere estar instalado en el servidor).

### Requisitos:
* **Minecraft:** `1.21.1`
* **Cargador de Mods:** [Fabric](https://fabricmc.net/) o [NeoForge](https://neoforged.net/)
* **Dependencias obligatorias:**
  * [Cobblemon](https://modrinth.com/mod/cobblemon) (v1.5.2 o superior)
  * [Architectury API](https://modrinth.com/mod/architectury-api)
  * [Forge Config API Port](https://modrinth.com/mod/forge-config-api-port) *(Solo requerido en Fabric)*

---

## 📄 Licencia

Este proyecto está distribuido bajo los términos de la licencia **GNU Lesser General Public License v3.0 (LGPL-3.0)**.  
Consulta el archivo `LICENSE.md` para más información.
