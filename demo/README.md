# Guião de Demonstração

## 1. Preparação do sistema

Para testar o sistema e todos os seus componentes, é necessário preparar um ambiente com dados para proceder à verificação dos testes.

### 1.1. Lançar o *registry*

Para lançar o *ZooKeeper*, ir à pasta `zookeeper/bin` e correr o comando  
`./zkServer.sh start` (Linux) ou `zkServer.cmd` (Windows).

É possível também lançar a consola de interação com o *ZooKeeper*, novamente na pasta `zookeeper/bin` e correr `./zkCli.sh` (Linux) ou `zkCli.cmd` (Windows).

### 1.2. Compilar o projeto

Primeiramente, é necessário compilar e instalar todos os módulos e suas dependências --  *rec*, *hub*, *app*, etc.
Para isso, basta ir à pasta *root* do projeto e correr o seguinte comando:

```sh
$ mvn clean install -DskipTests
```

### 1.3. Lançar e testar o *rec*

Para proceder aos testes, é preciso em primeiro lugar lançar o servidor *rec* .
Para isso basta ir à pasta *rec* e executar:

```sh
$ mvn compile exec:java
```

Este comando vai colocar o *rec* no endereço *localhost* e na porta *8091*, bem como registá-lo no *ZooKeeper* com o *path* `/grpc/bicloin/rec/1`, conforme definido no ficheiro `pom.xml`.

Para lançar o servidor com outros argumentos:

```sh
$ rec <endereço_zookeeper> <porto_zookeeper> <endereço_rec> <porto_rec> <path_rec>
```

**Nota:** Para poder correr o script *rec* diretamente é necessário fazer `mvn install` e adicionar ao *PATH* ou utilizar diretamente os executáveis gerados na pasta `target/appassembler/bin/`.

Para confirmar o funcionamento do servidor com um *ping*, fazer:

```sh
$ cd rec-tester
$ mvn compile exec:java
```

Para executar toda a bateria de testes de integração, fazer:

```sh
$ mvn verify
```

Todos os testes devem ser executados sem erros.


### 1.4. Lançar e testar o *hub*

Para proceder aos testes, é preciso em primeiro lugar lançar o servidor *hub* .
Para isso basta ir à pasta *hub* e executar:

```sh
$ mvn compile exec:java
```

Este comando vai colocar o *hub* no endereço *localhost* e na porta *8081*, conforme definido no ficheiro `pom.xml`.

Para lançar o servidor com outros argumentos:

```sh
$ hub <endereço_zookeeper> <porto_zookeeper> <endereço_hub> <porto_hub> <path_hub> <ficheiro_users> <ficheiro_stations> [initRec]
```

**Nota:** Para poder correr o script *hub* diretamente é necessário fazer `mvn install` e adicionar ao *PATH* ou utilizar diretamente os executáveis gerados na pasta `target/appassembler/bin/`.

Para confirmar o funcionamento do servidor com um *ping*, fazer:

```sh
$ cd hub-tester
$ mvn compile exec:java
```

**Nota:** Ao usar a flag initRec, será necessário iniciar primeiro o servidor *rec* (descrito acima) para receber resposta ao ping. 

Para executar toda a bateria de testes de integração, será preciso iniciar o servidor *rec* e depois fazer:

```sh
$ mvn verify
```

Todos os testes devem ser executados sem erros.

Após a execução dos testes, os servidores encontram-se num estado alterado e será preciso reiniciá-los, tanto para executar outra vez os testes, como para proceder à normal execução da aplicação.

### 1.5. *App*

Iniciar a aplicação com a utilizadora alice:

```sh
$ app localhost 2181 alice +35191102030 38.7380 -9.3000
```

**Nota:** Para poder correr o script *app* diretamente é necessário fazer `mvn install` e adicionar ao *PATH* ou utilizar diretamente os executáveis gerados na pasta `target/appassembler/bin/`.

Abrir outra consola, e iniciar a aplicação com o utilizador bruno.

Depois de lançar todos os componentes, tal como descrito acima, já temos o que é necessário para usar o sistema através dos comandos.

## 2. Teste dos comandos

Nesta secção vamos correr os comandos necessários para testar todas as operações do sistema.
Cada subsecção é respetiva a cada operação presente no *hub*.

### 2.1. *balance*

O comando *balance* serve para mostrar o saldo da conta.
Para usar o comando *balance*, só temos de fazer:

```sh
> balance
alice 0 BIC
```

### 2.2 *top_up*

O comando *top-up* serve para carregarmos dinheiro na conta.
Para usar o comando *top-up* temos que escolher um valor para carregar (por exemplo 15). Temos que ter em atenção se o número usado no login da app é o mesmo com que foi efetuado o registo. Se sim:

```sh
> top-up 15
alice 150 BIC
```

Caso contrário, teremos o seguinte erro:

```sh
> top-up 15
ERRO Número de telemóvel errado
```

Se o utilizador não estiver registado: 

```sh
> top-up 15
ERRO Utilizador não existe
```

Para além disso, o valor a carregar tem que ser entre 1 e 20 euros. Caso contrário:

```sh
> top-up 50
ERRO Carregamento deve ser um valor entre 1 e 20   
```

Por fim, se o montante não estiver em euros:

```sh
> top-up 15
ERRO O montante deve ser em euros
```


### 2.3 *info_station*

O comando *info* usa a operação *info-station* do hub e serve para fornecer informações acerca de uma estação.

Para usar o comando *info*, temos que escolher uma estação (previamente registada): 

```sh
> info istt
IST Taguspark, lat 38.7372, -9.3023 long, 20 docas, 4 BIC prémio, 12 bicicletas, 0 levantamentos, 0 devoluções, https://www.google.com/maps/place/38.7372,-9.3023
```

Caso contrário, não estando registada, teremos o seguinte erro:

```sh
> info istta
ERRO Estação não encontrada
```

### 2.3 *locate_station*

O comando *scan* usa a operação *locate-station* do hub e serve para mostrar as *n* estações mais próximas do utilizador.

Para usar o comando *scan*, temos que escolher o número de estações a mostrar: 

```sh
> scan 3
istt, lat 38.7372, -9.3023 long, 20 docas, 4 BIC prémio, 12 bicicletas, a 218 metros
stao, lat 38.6867, -9.3124 long, 30 docas, 3 BIC prémio, 20 bicicletas, a 5805 metros
jero, lat 38.6972, -9.2064 long, 30 docas, 3 BIC prémio, 20 bicicletas, a 9302 metros
```

Se escolhermos um número maior que o número de estações registadas não existe erro, mas receberemos apenas o número máximo de estações.

### 2.4 *bike_up*

O comando *bike-up* é usado com a abreviatura da estação escolhida e serve para alugar uma bicicleta. 

```sh
> bike-up istt
OK
```

Se verificarmos o *balance* em seguida, observamos que gastamos 10 Bicloins na operação.

```sh
> balance
alice 150 BIC
> bike-up istt
OK
> balance
alice 140 BIC
```

Podem ocorrer vários erros durante esta operação:

Se não tivermos dinheiro suficiente para a operação:

```sh
> bike-up istt
ERRO O utilizador não tem saldo suficiente
```

Se já estivermos a alugar uma bicicleta no momento:

```sh
> bike-up istt
ERRO O utilizador já se encontra numa bicicleta
```

Se a estação escolhida estiver demasiado longe (mais de 200 metros):

```sh
> bike-up istt
ERRO O utilizador está muito longe
```

Se não houver bicicletas disponíveis na estação:

```sh
> bike-up istt
ERRO Não há bicicletas disponíveis
```

### 2.5 *bike_down*

O comando *bike-down* é usado com a abreviatura da estação escolhida e serve para devolver uma bicicleta. 

```sh
> bike-down istt
OK
```

Se verificarmos o balance em seguida, observamos que recebemos o prémio associado à estação de chegada.

```sh
> balance
alice 140 BIC
> bike-down istt
OK
> balance
alice 144 BIC
```

Podem ocorrer vários erros durante esta operação:

Se não estivermos a alugar uma bicicleta no momento:

```sh
> bike-down istt
ERRO O utilizador não se encontra numa bicicleta
```

Se a estação escolhida estiver demasiado longe (mais de 200 metros):

```sh
> bike-down istt
ERRO O utilizador está muito longe
```

Se não houver docas disponíveis na estação:

```sh
> bike-down istt
ERRO Não há docas disponíveis
```

### 2.6 *ping*

O comando *ping* serve para verificar o estado de vida do servidor *hub*. 

```sh
> ping
Estado do servidor: up
```

### 2.6 *sys_status*

O comando *sys_status* serve para verificar o ponto de situação de todo o sistema. 

```sh
> sys_status
Estado do sistema: 
Hub: up
Rec 1: up
Rec 2: up
```

Caso algum dos servidores não esteja ativo, será imprimida a seguinte mensagem:

```sh
> sys_status
Estado do sistema: 
Hub: up
Rec 1: down
Rec 2: up
```

### 2.7 Erros gerais

Durante toda a execução do programa, poderemos obter os seguintes erros:

```sh
> ping
ERRO Não foi possível contactar o servidor
```

```sh
> ping
ERRO O servidor cancelou o pedido
```

```sh
> ping
ERRO O servidor excedeu o tempo de resposta
```
----

## 3. Replicação e tolerância a faltas

### 3.1 Lançar as réplicas

Para lançar as réplicas, navega-se para a pasta `rec/target/appassembler/bin/` em três terminais diferentes, e executam-se os respetivos comandos em cada um deles:

```sh
# Terminal 1
$ ./rec localhost 2181 localhost 8091 /grpc/bicloin/rec/1
```
```sh
# Terminal 2
$ ./rec localhost 2181 localhost 8092 /grpc/bicloin/rec/2
```
```sh
# Terminal 3
$ ./rec localhost 2181 localhost 8093 /grpc/bicloin/rec/3
```
São iniciadas três réplicas no endereço *localhost* com portos *8091*, *8092* e *8093*, respetivamente.

### 3.2 Lançar o hub

Para lançar o hub, navega-se para a pasta `hub` num terminal diferente, e executa-se o seguinte comando:

```sh
$ mvn compile exec:java
```
O hub é iniciado no endereço *localhost:8081*.

Após ser lançado, vai popular simultaneamente as três réplicas com os dados dos ficheiros `csv/users.csv` e `csv/stations.csv`. Para cada registo a escrever, são efetuados dois pedidos a cada réplica:

- Um pedido de leitura, para obter a tag mais recente
- Um pedido de escrita, com o valor a escrever e a nova tag

### 3.3 Lançar a app

Para lançar a app, navega-se para a pasta `app` num terminal diferente, e executa-se o seguinte comando:

```sh
$ mvn compile exec:java
```

### 3.4 Testar o funcionamento normal

Os seguintes comandos deverão ser executados na *app*.

Fazer um carregamento:

```
> top-up 15
alice 150 BIC
```
É primeiro efetuada uma leitura do saldo atual do utilizador. O pedido de leitura é enviado às três réplicas, e espera por um quórum de respostas para obter o valor.

Depois, é efetuada uma escrita do novo saldo nas três réplicas. Como para todas as escritas, é enviado primeiro um pedido de leitura da tag mais recente, e de seguida o pedido de escrita.