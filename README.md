# SistemaDistribuido 
-Práctica final de la asignatura Programación Concurrente.

El sistema, representado por un grafo, está formado por un conjunto de nodos. De entre estos nodos destaca uno, denominado nodo de entorno. El nodo de entorno se encarga de generar el grafo (creación del resto de nodos y relaciones entre ellos) dado un fichero que contiene la relación padre-hijo entre nodos. El nodo de entorno recibe un trabajo que es dividido entre los nodos 'nodos hijo' que tiene. Los nodos hijos, a su vez, dividen el trabajo que reciben entre sus respectivos hijos. De este modo, el trabajo queda dividido en subtareas ejecutadas de forma concurrente por cada uno de los nodos. A medida que los nodos van acabando su trabajo, salvan el resultado en un fichero en común, garantizando la exclusión mutua para asegurar la consistencia de los datos.

En esta práctica, una tarea está representada por la clase JOB, y las clases que heredan de ella representan la tarea en específico.

Difuminación de imágenes
------------------------
Una de las tareas implementadas es la difuminación de una imagen. Dada una imagen, cada nodo se encarga de difuminar una sección determinada y guarda el resultado de su porción en un fichero .png

Dependencias: Es necesario para la ejecución del programa, usar el sistema de paso de mensajes Beanstalk
