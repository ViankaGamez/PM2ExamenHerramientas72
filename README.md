# PM2ExamenHerramientas-72
Aplicación Android desarrollada para el Examen Primer Parcial de Programación Móvil I.
La app permite gestionar herramientas de mantenimiento y sus asignaciones a técnicos, utilizando SQLite, RecyclerView, SearchView, AlertDialog e Intents.

###### Capturas de Pantallas Clave ######

### Alta de Herramienta
- Registro con nombre, descripción, especificaciones y foto.
- Validaciones obligatorias.

<img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/c51a86fa-2274-4c73-a626-f33a2965084f" />

### Lista de Herramientas
- RecyclerView con estado.
- Colores según fecha de entrega.
- Buscador por nombre, técnico o especificaciones.

<img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/ba9b45bd-82db-42c0-bdae-64782836f666" />

### Asignación de Herramienta
- Selección de técnico.
- Fecha inicio y fecha fin.
- Confirmación con AlertDialog.

<img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/4faa4b87-97fc-4fb9-aaa4-b63d33e79499" />

### Devolución y Compartir
- Cambio de estado a DISPONIBLE.
- Intent implícito para compartir resumen.

<img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/a3ded182-0f23-4f8d-a27b-edc3f4b9a2ec" />
<img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/aa3bc19d-e97e-4e29-b8c2-a5bdf0625335" />
<img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/93879ea2-c9fa-47c2-b574-4fa965e18f47" />

## Información Técnica

- Lenguaje: Java
- Base de datos: SQLite
- Arquitectura: Activities + RecyclerView + Adapter
- SDK mínimo: 24
- Target SDK: 36

## Funcionalidades Implementadas

- Registro de herramientas con validación.
- Persistencia en SQLite.
- Consultas JOIN para mostrar última asignación activa.
- Filtro con SearchView.
- Orden por fecha de entrega.
- Validación para evitar doble asignación.
- Confirmación con AlertDialog.
- Cambio de estado automático (DISPONIBLE / ASIGNADA).
- Compartir resumen por Intent implícito.

## Pasos de Construcción y Pruebas

Clonar el repositorio:
   bash
   git clone https://github.com/ViankaGamez/PM2ExamenHerramientas72.git

Video de Prueba:
  https://youtu.be/0ib0sEAP0pU
