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
    ```$xslt
        ./run_server.sh #numero del puerto
    ```
    ### Clientes de elección
    - Correr el script **run-FiscalClient.sh** para fiscalizar una mesa.
    ```$xslt
        ./FiscalClient.sh -DserverAddress=xx.xx.xx.xx:yyyy
         -Did=​pollingPlaceNumber -Dparty=​partyName​
    ```
    - Correr el script **run-ManagementClient.sh** para administrar la elección.
    ```$xslt
        ./run_ManagementClient.sh -DserverAddress=​xx.xx.xx.xx:yyyy​
         -Daction=​actionName
    ```
    - Correr el script **run-QueryClient.sh** para consultar resultados de la elección.
    ```$xslt
        #Para el nivel nacional
        ./run-QueryClient.sh -DserverAddress=10.6.0.1:1099 -DoutPath=query1.csv
        
        #Para el nivel provincial
        -DserverAddress=10.6.0.1:1099 -DoutPath=../query2.csv -Dstate=JUNGLE
        
        #Para el nivel de mesa de votación
        -DserverAddress=10.6.0.1:1099 -DoutPath=../query2.csv -Did=x
        
    ```
    - Correr el script **run-VoteClient.sh** para cargar votos en el sistema de elección.
    ```$xslt
        ./run-VoteClient.sh -DserverAddress=​xx.xx.xx.xx:yyyy​ -DvotesPath=​fileName
    ```
    
    
    


