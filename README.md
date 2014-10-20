# HTTP server written with Netty 4 (HamsterCoders test project)

## Task

Необходимо реализовать http-сервер на фреймворке netty
(http://netty.io/), со следующим функционалом:

1. По запросу на http://somedomain/hello отдает «Hello World» через 10 секунд

2. По запросу на http://somedomain/redirect?url=<url> происходит
переадресация на указанный url

3. По запросу на http://somedomain/status выдается статистика:

 - общее количество запросов

 - количество уникальных запросов (по одному на IP)

 - счетчик запросов на каждый IP в виде таблицы с колонкам и IP,
кол-во запросов, время последнего запроса

 - количество переадресаций по url’ам  в виде таблицы, с колонками
url, кол-во переадресация

 - количество соединений, открытых в данный момент

 - в виде таблицы лог из 16 последних обработанных соединений, колонки
src_ip, URI, timestamp,  sent_bytes, received_bytes, speed (bytes/sec)

Все это (вместе с особенностями имплементации в текстовом виде) выложить на github, приложить к этому:

 - скриншоты как выглядят станицы /status в рабочем приложении

 - скриншот результата выполнения команды ab – c 100 – n 10000
http://somedomain/status

 - еще один скриншот станицы /status, но уже после выполнение команды
ab из предыдущего пункта

## Screenshots

1. ```/status``` page:
![Screenshot1](https://github.com/WildSpirit94/NettyHTTPServer/blob/master/Screenshot%201.png)

2. ``` ab -c 100 -n 10000 http://domain/status ``` result:
![Screenshot2](https://github.com/WildSpirit94/NettyHTTPServer/blob/master/Screenshot%202.png)

3. ```/status``` page after benchmarking:
![Screenshot3](https://github.com/WildSpirit94/NettyHTTPServer/blob/master/Screenshot%203.png)

## Some implementation details

### Request processing pipeline

Request arrives to server -> Pipeline is created -> Traffic counter starts to count -> Arrived ByteBuf is decoded to HttpRequest -> HttpServerHandler creates response (message) based on URI and requests to write this message through the pipeline -> Response gets gzipped -> Response is encoded to sequence of bytes -> Traffic counter stops to count -> Response is sent

### Threading model

* Server listens to one port, therefore, there is one boss thread which accepts incoming connections.
* Once the connection is established, handlers in pipeline are executed in one of the worker threads.
* Worker threads perform non-blocking IO for one or more ```Channel```s.
