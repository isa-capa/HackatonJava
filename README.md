# Agenda de Contactos (Java) — Consola / Swing / JavaFX

Proyecto de agenda de contactos en Java con **tres interfaces**:
- ✅ **Consola** (`AppConsola`)
- ✅ **Swing** (`AppSwing`)
- ✅ **JavaFX** (`AppFx`)

La lógica de negocio se concentra en:
- `Contacto.java` → modelo de contacto (ID autogenerado, validaciones básicas)
- `Agenda.java` → almacena contactos, evita duplicados por nombre+apellido, controla capacidad

---

## 1) Requisitos

- **Java JDK 17+** (recomendado)
- **Maven 3.8+**
- (Solo para JavaFX) **JavaFX** configurado por Maven (ver sección JavaFX)

Bash:

- Java 17 o superior
- Maven 3.8+

---
## 2) Clonar y ejecutar

bash:
git clone <URL_DEL_REPO>
cd <NOMBRE_DEL_PROYECTO>
mvn clean compile

---
## 3) Estructura del proyecto

src/
 └─ main/
    └─ java/
       └─ app/
          ├─ Main.java
          ├─ AppConsola.java
          ├─ AppSwing.java
          ├─ AppFx.java
          ├─ Agenda.java
          └─ Contacto.java

---
## 4) ¿Cómo funciona el código?

4.1 Contacto

Genera un ID incremental automáticamente.
Valida:
- nombre y apellido deben ser una sola palabra (sin espacios).
- telefono es opcional (si viene null, se guarda como "").

4.2 Agenda
Tiene una capacidad máxima (por defecto 10).
* anadirContacto(contacto):
- No agrega si está llena o si ya existe alguien con mismo nombre+apellido (ignorando mayúsculas/minúsculas).
* buscaContacto(nombre, apellido):
- Regresa el contacto encontrado o null.
* eliminarContactoPorId(id):
- Elimina por ID (si existe).
* listarContactos():
- Lista de solo lectura (no se puede modificar desde fuera).

4.3 Interfaces
- AppConsola → menú en terminal
- AppSwing → interfaz clásica con botones
- AppFx → interfaz moderna con JavaFX
- Main → entrypoint que actualmente lanza JavaFX (AppFx)

