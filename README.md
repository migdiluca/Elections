# Elections

## Compilación
1. Utilizando el comando **cd** situarse en el directorio del proyecto.
2. Ejecutar el comando **mvn clean install**.

## Ejecucion
1. Una vez compilado situarse en las carpetas target de *client* y *server*.
2. Con el comando **tar -xzf** descomprimir los archivos: *Elections-client-1.0-SNAPSHOT-bin.tar.gz* y *Elections-server-1.0-SNAPSHOT-bin.tar.gz*.
Situados en sus respectivas carpetas. 
3. Situarse en la carpeta descomprimida.
4. Ejecutar el comando **chmod u+x** sobre los scripts *run-*.sh* para otorgarles permiso de ejecuccion.
Puede utilizar el script *run-Permits.sh* el cual le otorgara permisos a todos los demas scripts.
    ### Disponibilizar el servidor de elección
    - Correr el script **run-server.sh**. Por default utiliza el puerto 1099.
    ### Clientes de elección
    - Correr el script **run-FiscalClient.sh** para fiscalizar una mesa.
    - Correr el script **run-ManagementClient.sh** para administrar la elección.
    - Correr el script **run-QueryClient.sh** para consultar resultados de la elección.
    - Correr el script **run-VoteClient.sh** para cargar votos en el sistema de elección. 
    
    
    


