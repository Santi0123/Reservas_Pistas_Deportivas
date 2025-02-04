# Reservas de Pistas Deportivas

| Alumnos              | Ángela Romero Lobo    | Santiago Fuente Espinosa |
| -------------------- | ---------------------- | ------------------------ |
| **Fecha**      | *Inicio: 12/12/2024* | *Fin: 04/02/2025*      |
| **Curso**      | *2º DAM *            | *2º DAM*              |
| **Asignatura** | *Acceso a Datos*     | *Accesos a Datos*      |

# Documentación Modelos y Repositorios

### 1. Modelos

### 1.1 Usuario

Descripción

La entidad Usuario representa a los usuarios registrados en la plataforma.

#### Código

```java
@Entity
@Data
@NoArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String username;

    @Column(nullable = false, length = 80)
    private String password;

    @Column(nullable = false, unique = true, length = 80)
    private String email;

    @Column(nullable = false)
    private boolean enabled;

    @Enumerated(EnumType.STRING)
    private Rol tipo;
}
```

Atributos

id: Identificador único del usuario.

username: Nombre de usuario, debe ser único.

password: Contraseña del usuario.

email: Correo electrónico, debe ser único.

enabled: Indica si el usuario está activo.

tipo: Rol del usuario en la aplicación (ADMIN, OPERARIO, USUARIO).

### 1.2 Reserva

#### Descripción

La entidad `Reserva` representa una reserva realizada por un usuario en un horario específico.

#### Código

```java
@Entity
@Data
@NoArgsConstructor
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
  
    @ManyToOne
    private Usuario usuario;
  
    @ManyToOne
    private Horario horario;
  
    private LocalDate fecha;
}
```

#### Atributos

- **id**: Identificador único de la reserva.
- **usuario**: Usuario que realiza la reserva.
- **horario**: Horario reservado.
- **fecha**: Fecha de la reserva.

#### Relaciones

- `@ManyToOne Usuario`: Un usuario puede tener múltiples reservas.
- `@ManyToOne Horario`: Una reserva está asociada a un horario específico.

### 1.3 Rol

#### Descripción

El `enum Rol` define los distintos tipos de roles que un usuario puede tener en la aplicación.

#### Código

```java
public enum Rol {
    ADMIN, OPERARIO, USUARIO
}
```

### 1.4 Horario

#### Descripción

La entidad `Horario` representa un intervalo de tiempo en el que una instalación está disponible para reservas.

#### Código

```java
@Entity
@Data
@NoArgsConstructor
public class Horario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
  
    @ManyToOne
    private Instalacion instalacion;
  
    private LocalTime horaInicio;
    private LocalTime horaFin;
}
```

#### Atributos

- **id**: Identificador único del horario.
- **instalacion**: Instalación asociada al horario.
- **horaInicio**: Hora de inicio de la disponibilidad.
- **horaFin**: Hora de fin de la disponibilidad.

#### Relaciones

- `@ManyToOne Instalacion`: Un horario pertenece a una instalación específica.

### 1.5 Instalación

#### Descripción

La entidad `Instalacion` representa un área o espacio físico donde se pueden realizar reservas.

#### Código

```java
@Entity
@Data
@NoArgsConstructor
public class Instalacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
  
    @Column(nullable = false, length = 80)
    private String nombre;
}
```

#### Atributos

- **id**: Identificador único de la instalación.
- **nombre**: Nombre de la instalación.

---

## 2. Repositorios

Los repositorios permiten la interacción con la base de datos mediante `Spring Data JPA`.

### 2.1 RepoHorario

#### Descripción

Gestiona las operaciones de base de datos relacionadas con la entidad `Horario`, permitiendo obtener horarios por instalación y consultar disponibilidad.

#### Código

```java
@Repository
public interface RepoHorario extends JpaRepository<Horario, Long> {
    Page<Horario> findByInstalacion(Instalacion instalacion, Pageable pageable);
    List<Horario> findByInstalacion(Instalacion instalacion);
  
    @Query("""
        SELECT h 
        FROM Horario h
        WHERE h.instalacion = :instalacion
        AND h.id NOT IN (
            SELECT r.horario.id 
            FROM Reserva r 
            WHERE r.fecha = :fecha
        )
    """)
    List<Horario> findHorariosDisponibles(@Param("instalacion") Instalacion instalacion,
                                          @Param("fecha") LocalDate fecha);
}
```

### 2.2 RepoInstalacion

#### Descripción

Proporciona operaciones CRUD para la entidad `Instalacion`.

#### Código

```java
@Repository
public interface RepoInstalacion extends JpaRepository<Instalacion,Long> {
}
```

### 2.3 RepoReserva

#### Descripción

Maneja la persistencia de las reservas, permitiendo filtrar por usuario.

#### Código

```java
@Repository
public interface RepoReserva extends JpaRepository<Reserva,Long>{
    List<Reserva> findByUsuario(Usuario usuario);
    Page<Reserva> findByUsuario(Usuario usuario, Pageable pageable);
}
```

### 2.4 RepoUsuario

#### Descripción

Facilita la gestión de los usuarios, permitiendo buscar por nombre de usuario o por rol.

#### Código

```java
@Repository
public interface RepoUsuario extends JpaRepository<Usuario, Long>{
    List<Usuario> findByUsername(String username);
    Page<Usuario> findByTipo(Rol tipo, Pageable pageable);
}
```

---

# Documentación de Controladores

## Modelo: Usuario

La entidad `Usuario` representa a los usuarios del sistema y está anotada con `@Entity` para indicar que es una entidad JPA.

### Atributos:

* `id`: Identificador único autogenerado.
* `username`: Nombre de usuario, único y obligatorio.
* `password`: Contraseña encriptada.
* `email`: Dirección de correo electrónico, única y obligatoria.
* `enabled`: Indica si el usuario está activo o no.
* `tipo`: Enumeración que representa el rol del usuario.

---

## Controladores

### Controlador: ControUsuarios

Este controlador gestiona las operaciones CRUD sobre la entidad `Usuario`. Está protegido con `@PreAuthorize("hasAuthority('ADMIN')")`, lo que significa que solo los usuarios con rol de administrador pueden acceder a estas funciones.

#### Métodos:

##### `@GetMapping("")` - Obtener lista de usuarios

* **Ruta** : `/usuario`
* **Descripción** : Obtiene una lista paginada de usuarios.
* **Parámetros** :
* `Model model`: Para pasar datos a la vista.
* `Pageable pageable`: Configuración de paginación.
* **Retorno** : Vista `usuarios/usuarios` con la lista de usuarios y roles disponibles.

##### `@GetMapping("/add")` - Mostrar formulario para agregar usuario

* **Ruta** : `/usuario/add`
* **Descripción** : Muestra el formulario para crear un nuevo usuario con valores predeterminados.
* **Retorno** : Vista `/usuarios/add` con un usuario nuevo y los roles disponibles.

##### `@PostMapping("/add")` - Procesar la creación de usuario

* **Ruta** : `/usuario/add`
* **Descripción** : Guarda un nuevo usuario con su contraseña encriptada usando `BCryptPasswordEncoder`.
* **Parámetros** :
* `Usuario usuario`: Datos del usuario a crear.
* **Retorno** : Redirección a la lista de usuarios (`redirect:/usuario`).

##### `@GetMapping("/edit/{id}")` - Mostrar formulario de edición

* **Ruta** : `/usuario/edit/{id}`
* **Descripción** : Obtiene los datos de un usuario específico para su edición.
* **Parámetros** :
* `id`: Identificador del usuario a editar.
* `Model model`: Para pasar los datos a la vista.
* **Retorno** :
* Vista `/usuarios/add` con los datos del usuario.
* Si el usuario no existe, muestra la vista de error.

##### `@PostMapping("/edit/{id}")` - Procesar la edición del usuario

* **Ruta** : `/usuario/edit/{id}`
* **Descripción** : Guarda los cambios en un usuario existente. Si la contraseña tiene menos de 5 caracteres, mantiene la anterior.
* **Parámetros** :
* `Usuario usuario`: Datos actualizados del usuario.
* **Retorno** : Redirección a la lista de usuarios.

##### `@GetMapping("/del/{id}")` - Mostrar confirmación de eliminación

* **Ruta** : `/usuario/del/{id}`
* **Descripción** : Muestra un formulario de confirmación para eliminar un usuario.
* **Parámetros** :
* `id`: Identificador del usuario.
* `Model model`: Para pasar los datos a la vista.
* **Retorno** :
* Vista `/usuarios/add` con los datos del usuario.
* Si el usuario no existe, muestra la vista de error.

##### `@PostMapping("/del/{id}")` - Procesar la eliminación del usuario

* **Ruta** : `/usuario/del/{id}`
* **Descripción** : Elimina un usuario de la base de datos.
* **Parámetros** :
* `Usuario usuario`: Usuario a eliminar.
* **Retorno** : Redirección a la lista de usuarios (`redirect:/usuario`).

### Controlador: ControReservas

Este controlador gestiona las operaciones CRUD sobre la entidad `Reserva`. Está protegido con `@PreAuthorize("hasAuthority('ADMIN')")`, lo que significa que solo los usuarios con rol de administrador pueden acceder a estas funciones.

#### Métodos:

##### `@GetMapping("")` - Obtener lista de reservas

* **Ruta** : `/reservas`
* **Descripción** : Obtiene una lista paginada de reservas.
* **Parámetros** :
  * `Model model`: Para pasar datos a la vista.
  * `Pageable pageable`: Configuración de paginación.
* **Retorno** : Vista `reservas/reservas` con la lista de reservas y usuarios disponibles.

##### `@GetMapping("/usuario/{id}")` - Obtener reservas de un usuario

* **Ruta** : `/reservas/usuario/{id}`
* **Descripción** : Obtiene todas las reservas asociadas a un usuario específico.
* **Parámetros** :
  * `id`: Identificador del usuario.
  * `Model model`: Para pasar los datos a la vista.
  * `Pageable pageable`: Configuración de paginación.
* **Retorno** :
  * Vista `reservas/reservas` con las reservas del usuario.
  * Si el usuario no existe, se muestra una vista de error.

##### `@GetMapping("/add")` - Mostrar formulario para agregar una reserva

* **Ruta** : `/reservas/add`
* **Descripción** : Muestra el formulario para crear una nueva reserva con valores predeterminados.
* **Retorno** : Vista `reservas/add` con la lista de usuarios y la fecha actual.

##### `@GetMapping("/add/usuario/{usuarioID}")` - Mostrar formulario para agregar una reserva para un usuario específico

* **Ruta** : `/reservas/add/usuario/{usuarioID}`
* **Descripción** : Muestra el formulario para crear una reserva seleccionando usuario y fecha.
* **Parámetros** :
  * `usuarioID`: ID del usuario.
  * `fecha`: Fecha de la reserva.
  * `Model model`: Para pasar los datos a la vista.
* **Retorno** :
  * Vista `reservas/add` con la lista de usuarios e instalaciones.
  * Si el usuario no existe, se muestra una vista de error.

##### `@GetMapping("/add/usuario/{usuarioID}/instalacion/{instalacionID}")` - Seleccionar horario de la instalación

* **Ruta** : `/reservas/add/usuario/{usuarioID}/instalacion/{instalacionID}`
* **Descripción** : Muestra los horarios disponibles para una instalación en una fecha específica.
* **Parámetros** :
  * `usuarioID`: ID del usuario.
  * `instalacionID`: ID de la instalación.
  * `fecha`: Fecha de la reserva.
  * `Model model`: Para pasar los datos a la vista.
* **Retorno** :
  * Vista `reservas/add` con la lista de horarios disponibles.
  * Si el usuario o la instalación no existen, se muestra una vista de error.

##### `@GetMapping("/add/usuario/{usuarioID}/instalacion/{instalacionID}/horario/{horarioID}")` - Confirmar reserva

* **Ruta** : `/reservas/add/usuario/{usuarioID}/instalacion/{instalacionID}/horario/{horarioID}`
* **Descripción** : Muestra la confirmación de una reserva antes de guardarla.
* **Parámetros** :
  * `usuarioID`: ID del usuario.
  * `instalacionID`: ID de la instalación.
  * `horarioID`: ID del horario.
  * `fecha`: Fecha de la reserva.
  * `Model model`: Para pasar los datos a la vista.
* **Retorno** :
  * Vista `reservas/confirmar-reserva` con los detalles de la reserva.
  * Si el usuario o el horario no existen, se muestra una vista de error.

##### `@PostMapping("/add/usuario/{usuarioID}/instalacion/{instalacionID}/horario/{horarioID}")` - Procesar la creación de la reserva

* **Ruta** : `/reservas/add/usuario/{usuarioID}/instalacion/{instalacionID}/horario/{horarioID}`
* **Descripción** : Guarda la reserva en la base de datos.
* **Parámetros** :
  * `Reserva reserva`: Datos de la reserva a crear.
* **Retorno** : Redirección a la lista de reservas (`redirect:/reservas`).

##### `@GetMapping("/del/{id}")` - Mostrar confirmación de eliminación

* **Ruta** : `/reservas/del/{id}`
* **Descripción** : Muestra un formulario de confirmación para eliminar una reserva.
* **Parámetros** :
  * `id`: Identificador de la reserva.
  * `Model model`: Para pasar los datos a la vista.
* **Retorno** :
  * Vista `reservas/del` con los datos de la reserva.
  * Si la reserva no existe, se muestra una vista de error.

##### `@PostMapping("/del/{id}")` - Procesar la eliminación de la reserva

* **Ruta** : `/reservas/del/{id}`
* **Descripción** : Elimina una reserva de la base de datos.
* **Parámetros** :
  * `Reserva reserva`: Reserva a eliminar.
* **Retorno** : Redirección a la lista de reservas (`redirect:/reservas`).

---

# Documentación del Sistema de Reservas

## Agregar Reserva

### Seleccionar Fecha

El usuario puede seleccionar una fecha para realizar la reserva mediante un campo de entrada tipo fecha.

```html
<input type="date" name="fecha" id="fecha" class="form-control"
th:value="${localdate != null ? localdate : ''}">
```

### Seleccionar Usuario

Se ofrece una lista desplegable con los usuarios disponibles para la reserva.

```html
<select id="usuario" class="form-select">
    <option value="-1">Todos los usuarios</option>
    <option th:each="user : ${usuarios}" th:value="${user.id}" th:text="${user.username}"
        th:selected="${usuario} != null ? (${usuario.id} == ${user.id} ? 'selected':'false') : 'false'">
    </option>
</select>
```

### Seleccionar Instalación

El usuario puede elegir la instalación donde se realizará la reserva.

```html
<select id="instalacion" class="form-select">
    <option value="-1">Todas las instalaciones</option>
    <option th:each="inst : ${instalaciones}" th:value="${inst.id}" th:text="${inst.nombre}"
        th:selected="${instalacion} !=null  ? (${instalacion.id} == ${inst.id} ? 'selected':'false') : 'false'">
    </option>
</select>
```

### Lista de Horarios Disponibles

Se muestra una tabla con los horarios disponibles para la instalación seleccionada.

```html
<table class="table">
    <thead>
        <tr>
            <th>#</th>
            <th>Instalación</th>
            <th>Inicio</th>
            <th>Fin</th>
            <th>Reservar</th>
        </tr>
    </thead>
    <tbody>
        <tr th:each="horario : ${horarios}">
            <td th:text="${horario.id}">ID</td>
            <td th:text="${horario.instalacion.nombre}">Nombre</td>
            <td th:text="${horario.horaInicio}">Inicio</td>
            <td th:text="${horario.horaFin}">Fin</td>
            <td>
                <a class="btn btn-success reservarBtn" th:data-horario-id="${horario.id}">
                    Reservar
                </a>
            </td>
        </tr>
    </tbody>
</table>
```

## Confirmar Borrado

### Confirmación de Eliminación

Se muestra un mensaje de advertencia para confirmar la eliminación de una reserva.

```html
<h3 class="mb-4 text-danger">⚠️ Confirmar Borrado</h3>
<p class="text-muted">¿Estás seguro de que deseas eliminar esta reserva? Esta acción no se puede deshacer.</p>
```

### Información de la Reserva

```html
<label><strong>Fecha:</strong></label>
<p th:text="*{fecha}" class="form-control-plaintext"></p>

<label><strong>Usuario:</strong></label>
<p th:text="*{usuario.username}" class="form-control-plaintext"></p>

<label><strong>Horario:</strong></label>
<p th:text="*{horario.horaInicio} + ' - ' + *{horario.horaFin}" class="form-control-plaintext"></p>
```

### Botones de Acción

```html
<div class="form-group spaced-buttons">
    <button type="submit" class="btn btn-danger">Confirmar Borrado</button>
    <a class="btn btn-secondary" th:href="@{/reservas}">Volver</a>
</div>
```

## Gestión de Reservas

### Seleccionar Usuario

Permite filtrar las reservas por usuario.

```html
<form id="usuarioForm" method="get" class="mb-3">
    <label for="usuario" class="font-weight-bold">Seleccionar Usuario</label>
    <select id="usuario" class="form-control">
        <option value="-1">Todos los usuarios</option>
        <option th:each="user : ${usuarios}" th:value="${user.id}" th:text="${user.username}"
            th:selected="${usuario} != null ? (${usuario.id} == ${user.id} ? 'selected':'false'):false">
        </option>
    </select>
</form>
```

### Tabla de Reservas

Muestra las reservas existentes.

```html
<table class="table table-hover table-striped mt-4 text-center">
    <thead class="thead-dark">
        <tr>
            <th>#</th>
            <th>Usuario</th>
            <th>Instalación</th>
            <th>Fecha</th>
            <th>Horario</th>
            <th>Acción</th>
        </tr>
    </thead>
    <tbody>
        <tr th:each="reserva : ${reservas}">
            <td th:text="${reserva.id}"></td>
            <td th:text="${reserva.usuario.username}"></td>
            <td th:text="${reserva.instalacion.nombre}"></td>
            <td th:text="${reserva.fecha}"></td>
            <td th:text="${reserva.horario.horaInicio} + ' - ' + ${reserva.horario.horaFin}"></td>
            <td>
                <a class="btn btn-danger" th:href="@{/reservas/delete/{id}(id=${reserva.id})}">
                    Eliminar
                </a>
            </td>
        </tr>
    </tbody>
</table>
```

# Documentación: Páginas de Gestión de Usuarios

## Introducción

Este documento describe la estructura y funcionalidad de dos páginas HTML que implementan la gestión de usuarios en una aplicación web usando Thymeleaf y Bootstrap. Las páginas permiten agregar, editar y eliminar usuarios, además de listar los usuarios existentes.

## Dependencias Utilizadas

Las páginas incluyen los siguientes recursos externos:

- **Bootstrap 5.3**: Para estilos y componentes responsivos.
- **Bootstrap Icons**: Para iconos en los botones.
- **Thymeleaf**: Motor de plantillas para la integración con Spring Boot.

## Página de Formulario de Usuarios

### Estructura General

```html
<!DOCTYPE html>
<html lang="es">
<head th:replace="plantilla/fragmentos.html :: headfiles">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestión de Usuarios</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons/font/bootstrap-icons.css">
</head>
<body>
```

- Se usa `th:replace` para importar fragmentos de la plantilla base.
- Se define el encabezado y se cargan los archivos CSS de Bootstrap e iconos.

### Contenido Principal

```html
<div class="container mt-5">
    <div class="card">
        <div class="card-header">
            <h3 th:switch="${operacion}" class="mb-0">
                <span th:case="'ADD'">Añadir un nuevo usuario</span>
                <span th:case="'DEL'">Eliminar un usuario</span>
                <span th:case="'EDIT'">Editar un usuario</span>
                <span th:case="*">Operación no soportada</span>
            </h3>
        </div>
```

- Se usa `th:switch` para cambiar el título según la operación seleccionada (Agregar, Eliminar, Editar).
- La estructura `card` de Bootstrap se usa para presentar el formulario de usuario.

### Formulario de Usuario

```html
<form method="post" th:object="${usuario}" class="needs-validation" novalidate>
    <input type="number" hidden name="id" th:field="*{id}">
  
    <div class="mb-3 form-check">
        <input type="checkbox" class="form-check-input" id="enabled" th:field="*{enabled}"
            th:attr="disabled=${operacion} == 'DEL' ? 'disabled' : null">
        <label for="enabled" class="form-check-label">Activado</label>
    </div>
```

- El formulario usa Thymeleaf para enlazar los campos a un objeto `usuario`.
- Se deshabilitan los campos cuando la operación es "Eliminar".

## Página de Listado de Usuarios

### Estructura General

```html
<!DOCTYPE html>
<html lang="es">
<head th:replace="plantilla/fragmentos.html :: headfiles">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestión de Usuarios</title>
</head>
<body>
```

- Similar a la página anterior, pero enfocada en la visualización de usuarios.

### Tabla de Usuarios

```html
<table class="table table-striped mt-4">
    <thead>
        <tr>
            <th scope="col">#</th>
            <th scope="col">Usuario</th>
            <th scope="col">Email</th>
            <th scope="col">Contraseña</th>
            <th scope="col">Rol</th>
            <th scope="col">Activado</th>
            <th scope="col">Editar</th>
            <th scope="col">Eliminar</th>
        </tr>
    </thead>
    <tbody>
        <tr th:each="usuario : ${usuarios}">
            <th scope="row" th:text="${usuario.id}"></th>
            <td th:text="${usuario.username}"></td>
            <td th:text="${usuario.email}"></td>
            <td th:text="${usuario.password}"></td>
            <td th:text="${usuario.tipo}"></td>
            <td th:text="${usuario.enabled}"></td>
            <td><a class="btn btn-warning" th:href="@{/usuario/edit/{id}(id=${usuario.id})}">Editar</a></td>
            <td><a class="btn btn-danger" th:href="@{/usuario/del/{id}(id=${usuario.id})}">Eliminar</a></td>
        </tr>
    </tbody>
</table>
```

- Se usa `th:each` para iterar sobre la lista de usuarios.
- Se generan botones de editar y eliminar con enlaces dinámicos.

### Paginación

```html
<nav aria-label="Page navigation example">
    <ul class="pagination">
        <li class="page-item">
            <a th:href="${page.hasPrevious()} ? |/usuario?page=${page.number - 1}| : '#'" class="page-link">
                <i class="fa-solid fa-chevron-left"></i> Anterior
            </a>
        </li>
        <li class="page-item"><a class="page-link active"
                th:text="|Estás en la página ${page.number + 1} de ${page.totalPages}|" href="#"></a></li>
        <li class="page-item">
            <a th:href="${page.hasNext()} ? |/usuario?page=${page.number + 1}| : '#'" class="page-link">
                Siguiente <i class="fa-solid fa-chevron-right"></i>
            </a>
        </li>
    </ul>
</nav>
```

- Se implementa una navegación para cambiar de página usando Thymeleaf.
