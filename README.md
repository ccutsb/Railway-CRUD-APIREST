# API REST CRUD con Spring Boot y PostgreSQL

Este proyecto es una implementación de una API REST que permite realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar) sobre una entidad Producto. La aplicación está desarrollada con Java, Spring Boot, Maven y PostgreSQL, y puede ser ejecutada en contenedores Docker.

## Demo en vivo

El proyecto está desplegado en Railway y se puede acceder a través del siguiente enlace:
[https://railway-crud-apirest-production-5c10.up.railway.app/productos](https://railway-crud-apirest-production-5c10.up.railway.app/productos)


## Tecnologías utilizadas

- Java 21
- Spring Boot 3.2.1
- Spring Data JPA
- Maven
- PostgreSQL
- Docker y Docker Compose

## Características

- Implementación de endpoints RESTful para operaciones CRUD
- Conexión a base de datos PostgreSQL
- Gestión de dependencias con Maven
- Contenerización con Docker
- Variables de entorno para configuración flexible

## Estructura del proyecto

```
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/empresaccutsb/apirest/apirest/
│   │   │       ├── Controllers/
│   │   │       │   └── ProductoController.java
│   │   │       ├── Entities/
│   │   │       │   └── Producto.java
│   │   │       ├── Repositories/
│   │   │       │   └── ProductoRepository.java
│   │   │       └── ApirestApplication.java
│   │   └── resources/
│   │       └── application.properties
├── .env.template
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

## Endpoints disponibles

- `GET /productos`: Lista todos los productos
- `GET /productos/{id}`: Obtiene un producto por su ID
- `POST /productos`: Crea un nuevo producto
- `PUT /productos/{id}`: Actualiza un producto existente
- `DELETE /productos/{id}`: Elimina un producto

## Requisitos previos

- Java 21 o superior
- Maven 3.6 o superior
- Docker y Docker Compose (opcional para contenerización)

## Configuración

1. Clona este repositorio:
   ```bash
   git clone https://github.com/tu-usuario/CRUD-API-REST.git
   cd CRUD-API-REST
   ```

2. Configura las variables de entorno:
   - Copia el archivo `.env.template` a `.env`
   - Modifica las variables con tus configuraciones de base de datos

## Ejecución

### Con Maven

```bash
./mvnw spring-boot:run
```

### Con Docker

```bash
docker-compose up -d
```

## Ejemplos de uso

### Crear un producto

```bash
curl -X POST \
  http://localhost:8080/productos \
  -H 'Content-Type: application/json' \
  -d '{
	"nombre": "Producto de ejemplo",
	"precio": 19.99
}'
```

### Obtener todos los productos

```bash
curl -X GET http://localhost:8080/productos
```

### Obtener un producto específico

```bash
curl -X GET http://localhost:8080/productos/1
```

### Actualizar un producto

```bash
curl -X PUT \
  http://localhost:8080/productos/1 \
  -H 'Content-Type: application/json' \
  -d '{
	"nombre": "Producto actualizado",
	"precio": 29.99
}'
```

### Eliminar un producto

```bash
curl -X DELETE http://localhost:8080/productos/1
```

## Contribuir

1. Haz un fork del proyecto
2. Crea una rama para tu característica (`git checkout -b feature/nueva-caracteristica`)
3. Haz commit de tus cambios (`git commit -am 'Añadir nueva característica'`)
4. Haz push a la rama (`git push origin feature/nueva-caracteristica`)
5. Crea un nuevo Pull Request

## Licencia

Este proyecto está licenciado bajo [MIT](LICENSE) - consulta el archivo LICENSE para más detalles. 
