# 2. SEGURIDAD
## DATASET
Para el dataset vamos a usar: [PwnedPasswordsDownloader]{https://github.com/HaveIBeenPwned/PwnedPasswordsDownloader} para eso necesitamos del sistema operativo Ubuntu (22.04 LTS de preferencia) o el subsistema WSL de Windows 
requerimientos:

```console
user@User:~$ dotnet --version
7.0.108 
```

Una vez hecho, ejecutamos el comando: 

```console
user@User:~$ dotnet tool install --global haveibeenpwned-downloader
```

Para la pruebas ejecutamos un 
```console
user@User:~$ haveibeenpwned-downloader pwnedpasswords
```
esto nos retornará un .txt llamado pwnedpasswords.txt  el cual tendrá solo un peso de 12 Mb con el que podremos hacer pruebas.

Si queremos descargas de una vez el dataset completo ejecutamos:
```console
user@User:~$ haveibeenpwned-downloader pwnedpasswords -o -p 64
```
este pesa aproximadamente 36Gb, y va a guardarlo todo en un solo.txt

RECUERDA: Si en caso no te funcionará el comando haveibeenpwned-downloader trata de ejecutarlo usando: haveibeenpwned-downloader.exe

## EJECUCIÓN
PREVIO: Configura todo el sistema para que puedas correr el entorno de [ooxwv-docker_hadoop]{https://github.com/ibm-developer-skills-network/ooxwv-docker_hadoop} para poder seguir con la ejecución.

Para poder configurar y ejecutar el programa en el entorno tenemos como requisitos dentro del entorno de ejecucion nodename:
- El dataset dentro 
- El programa jar

Para hacerlo ejecutamos:
```console
user@User:~$ docker cp pwnedpasswords.txt namenode:/
```

Ya estaría dentro del entorno de namenode, ahora tendríamos que importarlo dentro del entorno de ejecución que se encuentra en **usr/root/**:

Para ello nos envolvemos dentro del entorno nodename con el siguiente comando:

```console
user@User:~$ docker exec -it namenode /bin/bash
```

lo que no enviara a un nuevo entorno de ejecución (nodename):
```console
root@<id_del_entorno>:/#
```

El archivo para la busqueda que llamamos **PasswordCheck.java** 
- el archivo de entrada
- el directorio de salida
- la contraseña que va a ser buscada dentro del dataset (archivo de entrada)

        - Usage: PasswordCheck \<input-file> \<output-directory> \<password>

Para ello ejecutamos lo siguientes comandos:
---
Para importarlo dentro del directorio / entorno de nodename:
```console
user@User:~$ docker cp PasswordCheck.java namenode:/
```

Para ejecutar el archivo PasswordCheck.java:
Nos metemos dentro del entorno de ejecución nodename (anteriormente visto):
```console
user@User:~$ docker exec -it namenode /bin/bash
```

y dentro de este ejecutamos:
```console
root@<id_del_entorno>:/# javac -classpath $(hadoop classpath) PasswordCheck.java
```

Para convertir la clase resultante a un archivo jar:
```console
root@<id_del_entorno>:/# jar cf PasswordCheck.jar PasswordCheck*.class
```

Para ejecutar el hadoop con el archivo jar que generamos:

```console
root@<id_del_entorno>:/# hadoop jar PasswordCheck.jar PasswordCheck <archivo_entrada>.txt <directorio_salida> <contraseña_a_comprobar>
```

Ejemplo de ejecución
---
Probando con 10 Gb del dataset 


```console
root@<id_del_entorno>:/# hadoop jar PasswordCheck.jar PasswordCheck pwnedpasswords_10gb.txt salida_dataset amitech123
```