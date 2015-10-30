Описание протокола: https://github.com/ifmo-ctd-2012-networks/labs/tree/master/SkiporVladimir/lab2

Схема стейт-машины: https://github.com/ifmo-ctd-2012-networks/labs/blob/master/SkiporVladimir/lab2/Protocol_state_machine.pdf


##### Vagrant
* Для запуска сконфигурированных виртуальных машин нужно:
  * [Установить свежий Vagrant] (https://www.vagrantup.com/downloads.html)
  * Перейти в корне проекта: *vagrant up*
* Для подключения к нужной машине используйте *vagrant ssh* (Для дефолтной) или *vagrant ssh machine_name* 
* Для перезапуска всех машин *vagrant reload*, для перезапуска конкретной машины *vagrant reload machine_name*,   перезапуска с реконфигурированием нужно добавить параметр *--provision*
* Для посмотра рапущенных виртуальных машин: *vagrant status*
* Логи лежат в *home* дирректории пользователя *vagrant*. Эта дефолтная дирректория при подключении по *ssh*
* Для просмотров логов в real time : *tail -f node.log | grep [INFO]*

##### 0. Общие штуки
`__getstate__` и `__setstate__` - методы нужные для сериализации (представление ввиде `dict` (считай `Map` в питоне), которые легко сериализовать и отправить с помощью *JSON*)

`yield from` - ключевое слово питоновского *asyncio* (на самом деле не только его, но в этом проекте только в таком смысле используется), это асинхронный вызов, что-то вроде "исполняй этот метод, а когда он исполниться, пусть основной поток сюда вернется и продолжит исполнение"

##### 1. Token https://github.com/ifmo-ctd-2012-networks/labs/blob/master/PolyarniyNikolay/Lab2/src/task2/entity/token.py
Token:
 - progress - как далеко зашел прогресс
 - author_node_id - кто создал токен (!= тому, кто последний считал данные благодаря владению токеном. Т.е. создать этот токен-право на вычисления, мог кто-нибудь очень давно)
 - author_token_revision - уникальный номер токена среди всех токенов созданных author_node_id

##### 2. Messages https://github.com/ifmo-ctd-2012-networks/labs/blob/master/PolyarniyNikolay/Lab2/src/task2/entity/messages.py:
 - Message, он базовый, содержит тип сообщения и номер автора
 - три сообщения, которые расширяют базовый Message, добавляя туда токен (TAKE_TOKEN, TOKEN_IS_HERE, GENERATING_TOKEN)
 - TAKE_TOKEN при этом передает еще перечень всех инстансов (nodes) и результат вычисления

##### 3. Consts https://github.com/ifmo-ctd-2012-networks/labs/blob/master/PolyarniyNikolay/Lab2/src/task2/entity/consts.py
какие-то параметры того, что сколько ждать

##### 4. States https://github.com/ifmo-ctd-2012-networks/labs/blob/master/PolyarniyNikolay/Lab2/src/task2/flow/states.py
(интерфейс тут - https://github.com/ifmo-ctd-2012-networks/labs/blob/master/PolyarniyNikolay/Lab2/src/task2/flow/context.py )

все состояния имеют одинаковую структуру:

у них есть метод `execute(context)`, где в context хранится полное состояние, доступ к обмену сообщениями и т.п.

логика этого метода такая:

начинаю жить в этом состоянии, жду событий:
 - пришло сообщение, смотрю какого оно вида, соответственно реагирую
 - случился какой-то таймаут (например долго был в WAIT_STATE и не дождался очередного HEART_BEAT сообщения) - реагирую на него, и запускаю таймер этого типа заново

реакция на сообщения или таймеры может быть:
 - отправить сообщение (отправить в ответ на пришедшее, если оно было TCP / отправить всем / отправить кому-то конкретному (используется для передачи токена))
 - перейти в другое состояние, и отдать контроль (return из execute)
 - поменять context, например начать считать что-то, или заменить токен, который у нас был на токен, который к нам пришел

##### 5. Context https://github.com/ifmo-ctd-2012-networks/labs/blob/master/PolyarniyNikolay/Lab2/src/task2/flow/context.py
наверное все довольно очевидно

##### 6. Messenger https://github.com/ifmo-ctd-2012-networks/labs/blob/master/PolyarniyNikolay/Lab2/src/task2/flow/messenger.py
есть один TCPMessenger и один UDPMessenger
на них построен Messenger, который позволяет:
 - дождаться до прихода одного сообщения из любого из них (Messenger.listen_message)
 - отправить сообщение кому-то конкретному / всем
 - он же хранит перечень инстансов (nodes)
 - он же при любом сообщении от неизвестного ранее инстанса - добавляет его в перечень (Messenger._on_message)

