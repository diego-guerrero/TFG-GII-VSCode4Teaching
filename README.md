# Trabajo Fin de Grado - Grado en Ingeniería Informática

**Título del trabajo**: VSCode4Teaching: implementación de interfaz web para el ecosistema para la docencia de la programación en línea.  
**Autor**: Diego Guerrero Carrasco ([correo electrónico](mailto:diegogcarrasco@icloud.com), [perfil en GitHub](https://github.com/diego-guerrero), [perfil en LinkedIn](https://www.linkedin.com/in/diego-guerrero-carrasco/)).  
**Titulación**: Grado en Ingeniería Informática (dentro del Doble grado en Ingeniería Informática e Ingeniería del *Software*), Escuela Técnica Superior de Ingeniería Informática, Universidad Rey Juan Carlos (Madrid, España).  
**Defendido** el martes 16 de julio de 2024 en el Campus de Móstoles de la Universidad Rey Juan Carlos.

El presente repositorio contiene el código fuente del proyecto VSCode4Teaching desde su origen hasta la finalización del Trabajo Fin de Grado correspondiente al Grado en Ingeniería Informática. El máximo punto de actualización se ubica en la rama [`develop`](https://github.com/diego-guerrero/TFG-GII-VSCode4Teaching/tree/develop), situada en la última versión de VSCode4Teaching disponible antes de finalizar el Trabajo Fin de Grado.


## Resumen
Desde su origen, [*VSCode4Teaching*](https://github.com/codeurjc-students/2019-VSCode4Teaching) ha venido siendo una extensión web para Visual Studio Code, un entorno de desarrollo integrado, que tiene como objetivo facilitar y potenciar la docencia de la programación informática para mejorar la educación en competencias digitales y en el ámbito de la informática, área en pleno crecimiento y promulgación a nivel global.

Para ello, *VSCode4Teaching* permite a los profesores crear y gestionar cursos con ejercicios de programación que se basan en una plantilla inicial propuesta por ellos y, opcionalmente, una propuesta de solución al ejercicio. Los alumnos inscritos en los cursos completarán los ejercicios descargándose la plantilla y realizando sobre ella su propuesta propia de resolución, sincronizándola con el servidor para guardarla e informar en tiempo real a sus profesores de los avances realizados hasta finalizarla.

La memoria de este Trabajo Fin de Grado describe en profundidad el ciclo de desarrollo relativo al cuarto hito evolutivo del proyecto *VSCode4Teaching*, en el que se implementa una aplicación web de navegador que incorpora los procesos de negocio que los usuarios ejecutaban en la extensión de Visual Studio Code, eliminando la obligatoriedad de uso de este entorno para alcanzar a un público objetivo mayor.

El *software* del proyecto se organiza en una arquitectura cliente-servidor. El servidor, encargado del suministro, persistencia e interpretación de los datos, intercambia información con dos clientes: la extensión para Visual Studio Code y la aplicación web de navegador, que disponen en consecuencia las interfaces gráficas necesarias para la interacción con la aplicación.

El proyecto *VSCode4Teaching* es *software* libre divulgado bajo licencia Apache 2.0 a través de un [repositorio público en GitHub](https://github.com/codeurjc-students/2019-VSCode4Teaching) que contiene, además, documentación sobre el proyecto para favorecer la libre ejecución, utilización y adaptación del proyecto a toda la comunidad de desarrolladores.


## Recursos asociados
- Memoria de la evolución realizada sobre el proyecto VSCode4Teaching en este Trabajo Fin de Grado: [https://github.com/diego-guerrero/TFG-GII-Memoria](https://github.com/diego-guerrero/TFG-GII-Memoria).


## Licencia
Este proyecto se divulga desde su origen bajo licencia **Apache License 2.0**, por lo que tanto el trabajo del presente autor como de los anteriores debe ser reconocido en sucesivas reutilizaciones del proyecto en caso de no ser modificado. Se puede consultar más información en el fichero [`LICENSE`](LICENSE).

---

# VSCode4Teaching

[![Travis CI build status](https://img.shields.io/travis/com/codeurjc-students/2019-VSCode4Teaching?label=Travis%20CI&style=flat-square)](https://app.travis-ci.com/github/codeurjc-students/2019-VSCode4Teaching)
[![Docker Hub Repository](https://img.shields.io/docker/v/vscode4teaching/vscode4teaching?color=0db7ed&label=Docker%20Hub&sort=date&style=flat-square)](https://hub.docker.com/r/vscode4teaching/vscode4teaching)
[![Docker Hub pulls](https://img.shields.io/docker/pulls/vscode4teaching/vscode4teaching?color=0db7ed&label=Docker%20Hub%20pulls&style=flat-square)](https://hub.docker.com/r/vscode4teaching/vscode4teaching)
[![VS Marketplace extension's version](https://img.shields.io/visual-studio-marketplace/v/vscode4teaching.vscode4teaching?color=0078d7&label=VS%20Marketplace&style=flat-square)](https://marketplace.visualstudio.com/items?itemName=VSCode4Teaching.vscode4teaching)
[![VS Marketplace extension's installs](https://img.shields.io/visual-studio-marketplace/i/vscode4teaching.vscode4teaching?color=0078d7&label=VS%20Marketplace%20installs&style=flat-square)](https://marketplace.visualstudio.com/items?itemName=VSCode4Teaching.vscode4teaching)

VSCode4Teaching is a [Visual Studio Code](https://code.visualstudio.com) extension that brings the programming exercises of a course directly to the student’s editor, so that the teacher of that course can check the progress of the students and help them. It was created and expanded by Iván Chicano Capelo (whose blog can be read clicking [here](https://medium.com/@ivchicano)) and Álvaro Justo Rivas Alcobendas. Currently, this project is being developed by Diego Guerrero Carrasco. All the information about the progress of this stage of the project can be read in [this blog](https://medium.com/@diego-guerrero).


## Table of contents
- [User guide](#user-guide)
- [Developer guide](#developer-guide)
  - [License](#license)
  - [Architecture](#architecture)
  - [How to quickly start up a server](#how-to-quickly-start-up-a-server)


## User guide

A complete user guide is introduced in the [README of the extension](https://github.com/codeurjc-students/2019-VSCode4Teaching/blob/master/vscode4teaching-extension/README.md#user-guide).

## Developer guide

**Sections**
- [License](#license)
- [Architecture](#architecture)
- [How to quickly start up a server](#how-to-quickly-start-up-a-server)

### License
VSCode4Teaching is an application released under *Apache License 2.0*, which is a permissive license that allows the modification, distribution and public and private use of the source code of all the components of the application as long as the original *copyright* is guaranteed.

Therefore, it is possible to use the tools provided by GitHub (such as *Issues* or *Pull Requests*) to contact the developer in charge or to propose functionality improvements or bug fixes that users of this *Open Source software* may detect during their modification or reuse processes.

The license can be checked and read in the file [LICENSE](LICENSE) of the repository (in the root directory).

### Architecture
VSCode4Teaching is composed of three components that work cooperatively with each other. The source code of each component is placed in directories located in the root location of this repository. They are:
- A **web server** ([**``vscode4teaching-server``**](vscode4teaching-server)) based on the Spring Boot *framework* that takes the role of *backend* in the application, performing the tasks of: managing the persistence and interpretation of information, storing the files that conform the exercises (both templates and proposed solutions) and exposing a REST API that allows communication with the other components.  
  More information about the web server can be found in its respective [README](vscode4teaching-server/README.md) (in the root directory of the component).
- A **extension** (*plugin*) for Visual Studio Code ([**``vscode4teaching-extension``**](vscode4teaching-extension)) based on Node.js that acts as a *frontend*, being the component that is installed in the IDE and that allows users to use an intuitive and friendly GUI to interact with the *backend* and thus be able to access all the functionality of the application. Although it is server-independent in terms of code, it is necessary to configure a connection to a VSCode4Teaching server to be able to use the functionality of this extension.  
  More information about the extension can be found in its respective [README](vscode4teaching-extension/README.md) (in the root directory of the component).
- A **web application** ([**``vscode4teaching-webapp``**](vscode4teaching-webapp)) based on the Angular *framework* that acts as a complementary web *frontend* to the extension (from which it is independent), allowing to expand its functionality beyond the IDE to incorporate features such as two-step registration for teachers or the creation of the custom help page for students. It is intended to be built and introduced in the server to be deployed inside it.

### How to quickly start up a server
To set up a VSCode4Teaching server, the fastest method is to use **Docker**, which is a lightweight container-based technology to speed up application deployment. For this purpose, some relevant files are inserted into the repository:
- A [``Dockerfile``](Dockerfile) file containing the necessary coding to compile the webapp and insert it as a server view, which is compiled and launched in a Java container. The image resulting from this compilation is published in [*Docker Hub*](https://hub.docker.com/r/vscode4teaching/vscode4teaching) each time a new version of the application is released.
- A file [``vscode4teaching-server/docker/docker-compose.yml``](vscode4teaching-server/docker/docker-compose.yml) that allows using Docker Compose to quickly run two containers: one for the MySQL database used (``db``), for the image compiled from the ``Dockerfile`` above (``app``) and for the execution of a graphical database manager (``adminer``), which is optional and can be removed without affecting the operation of the server.
- A file [``vscode4teaching-server/docker/.env``](vscode4teaching-server/docker/.env) with user-customizable environment variables for the execution of the ``docker-compose.yml`` above.

Therefore, it is possible to run a VSCode4Teaching server directly using the ``docker-compose.yml`` file, by pointing a terminal to the directory containing it and running the command ``docker compose up -d`` (or ``docker-compose up -d`` in earlier versions of Docker).
In case you do not want to use the image published in the *Docker Hub*, it is possible to build the image manually by running a ``docker build -t vscode4teaching/vscode4teaching .`` command in the directory containing the ``Dockerfile``.

More information about the development of the application can be read in the ``README`` file of each of the components ([server](vscode4teaching-server/README.md) and [extension](vscode4teaching-extension/README.md)).