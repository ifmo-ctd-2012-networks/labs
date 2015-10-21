#Lab2 <protocol_name> Protocol

###Token 
Token = [progress :: uint64, authorMAC :: [6]byte, authorTokenNum :: uint32]
priority(t Token) = [~progress,authorMAC, authorTokenNum]
###### progress is always increasing megure of progress. If there is no megure of progress in algorithm, timestamp in nanos is used as progress

##### Any message format [messageKind :: uint8, reserved :: uint8 (0x00 default), messageAuthorMAC :: [6]byte, message kind specific data ...]

##### Warning: all numiric data transmited in Big endian (Network endian, not Intel)
* Algo States:
  * OWNER_STATE
    * Is token owner. 
    * Broadcasting TOKEN_IS_HERE_MESSAGE every HEART_BEAT_PERIOD nanos
  * WAITER_STATE
    * Waiting for token. 
    * Node that heard about token (received TOKEN_IS_HERE_MESSAGE message) earlier than WAITER_STATE_TIMEOUT before current moment
    * On WAITER_STATE_TIMEOUT 
      * Broadcasts WHERE_IS_TOKEN_MESSAGE
      * Change state to LOOSER_STATE
  * LOOSER_STATE
    * Node that loose token.
    * Node that doesn't heard about token (received TOKEN_IS_HERE_MESSAGE message) earlier than WAITER_STATE_TIMEOUT before current moment
    * Every LOOSER_STATE_TIMEOUT
      * Broadcasts WHERE_IS_TOKEN_MESSAGE
      * Waits for TOKEN_WAS_HERE_MESSAGE_RECENTLY_MESSAGE
        * On receive continue be LOOSER_STATE till next timeout
        * Else Broadcasts
  * GENERATING_STATE
    
* Messages
  * TAKE_TOKEN_MESSAGE (передача токена)
  * TOKEN_IS_HERE_MESSAGE (heartBeat броадкастом, он же уничтожь токен, если его priority больше, его же отсылаем индивидуально и броадкастом при получении любого сообщения от LOOSER_STATE (значит токен потерялся, нужно им сообщить, что он у нас) )
  * PING_MESSAGE(индивидуально отсылаем при получении броадкаста от того, кого мы не знаем, или в ответ на броадкаст TOKEN_IS_HERE_MESSAGE, если мы собираемся свой токен)
  * TOKEN_WAS_HERE_MESSAGE_RECENTLY_MESSAGE (отсылается WAITER_STATEом индивидуально в ответ на любое сообщение LOOSER_STATE)
  * WHERE_IS_TOKEN_MESSAGE отсылается броадкастом LOOSER_STATEом, который хочет перейти в GENERATING_STATE
  * GERERATING_TOKEN - отсылается GENERATING_STATEом, тому кто прислал GENERATING_TOKEN_MESSAGE с большим priority и броадкастом, когда переходим в LOOSER_STATE_STATE
###Node State
state :: StateENUM
ringMembers :: SortedVector<(mac, ip)>  // all ring members. 
ringMembersHash :: uint64 // hash in fixed cross-platform algorithm
heardAboutToken :: uint64 (timestamp nanos)
currentMyTokenNumber :: uint32

###Protocol constants
HEART_BEAT_PERIOD :: uint64 (nanos)
WAITER_SLEEP_INTERVALS :: uint32
WAITER_TIMEOUT = HEART_BEAT_PERIOD * WAITER_SLEEP_INTERVALS :: nanos
LOOSER_SLEEP_INTERVALS = WAITER_STATE_SLEEP_INTERVALS/2
LOOSER_TIMEOUT = HEART_BEAT_PERIOD * LOOSER_SLEEP_INTERVALS :: nanos
LOOSER_ANSWER_INTERVALS  :: uint32
LOOSER_ANSWER_TIMEOUT = HEART_BEAT_PERIOD * LOOSER_ANSWER_INTERVALS :: nanos

      Пусть у каждого будет текущая версия таблицы участников. 
      Будем помнить сколько последний раз было записей в таблице токена. Если в пришедшем токене больше, то заменяем свою.
      08.10.15  

      Для простоты ни кто не удаляется, и передаём токен тому кто дальше по списку.  Будут служебные сообщения. Индивидуальные по TCP с гарантией доставки, и броадкасты.  Во всех сообщениях вкладываем МАС. При получении любого сообщения, кладём отправителя в таблицу если его там не было.  

Тот у кого токен будет рассылать heartBeat с -то переодом HEART_BEAT_PEREOD.
будет у всех фиксированный таймаут n * t.
При получении сообщения heartBeat или другого, из существования которого можно сделать вывод, что токен появится, запоминаем это время, ка последнее время когда мы знаем о токене и таймаут считает от него.
08.10.15  
Поправка - нужно передавать хеш по фиксированному алгоритму, иначе может прийти токен с тем же числом участников, но из другой подсети, которая недавно подсоеденилась и у нас всё испортится
Т.е. хеш и размер.


 Если мы владелец токена, получаем heartBeat токена с priority меньше нашего, то отсылаем индивидуальное сообщение владельцу токена TOKEN_DESTROYED, На случай, если heartBeat прошёл из другой подсети и о нас они не знают ещё. В случае доставки сообщения уничтожаем наш токен. 
 Если priority больше нашего - отсылаем индивидуальное сообщение, что токен нужно уничтожить. Уничтожат или нет, не так важно.
  Храним время когда последний раз узнавали о существовании токена (heartBeat), и от него считаем таймаут. 
  Состояния
  Владелец токена: OWNER_STATE
  таймаут не кончился: WAITER_STATE
  Таймаут кончился: LOOSER_STATE
  Когда таймаут кончается. переходим в состояние LOOSER_STATE Рассылаем броадкаст 
  WHERE_IS_TOKEN_MESSAGE. Владелец токена на такой отсылает тот же heartBeat отправителю в инидвидуальном сообщении. Те у кого таймаут не кончился отсылают индивидуально TOKEN_WAS_HERE_MESSAGE_RECENTLY_MESSAGE.
  при получении heartBeat (пусть называетя TOKEN_IS_HERE_MESSAGE)
  LOOSER_STATE переходит в WAITER_STATE обновляя время когда послединий раз узнавал о существовании токена HEARD_ABOUT_TOKEN)
  WAITER_STATE просто время обновляет.
  при получении TOKEN_WAS_HERE_MESSAGE_RECENTLY_MESSAGE
  LOOSER_STATE засыпает на n * t/2
  После чего снова Рассылает броадкаст 
  WHERE_IS_TOKEN_MESSAGE

  В итоге при потере токена вся подсеть со временем станет looser и начнётся голосование кто будет генерировать токен.

  Первый LOOSER_STATE не получивший за k * t TOKEN_WAS_HERE_MESSAGE_RECENTLY_MESSAGE или TOKEN_IS_HERE_MESSAGE, переходит в состояние GENERATING_STATE и рассылает Броадкастом GENERATING_TOKEN_MESSAGE и приоритет (вкладывает свой прогресс, МАС и CURRENT_TOKEN_NUMBER + 1 )

  LOOSER_STATE получишвий GERERATING_TOKEN, пытается поучавствовать в генерации токена. Создаёт свой, и если его priority меньше, то рассылает броадкастом GERERATING_TOKEN, и инидвидульным сообщением тому кто прислал его нам, а если priority больше, то снова засыпает на n * t/2 

  WAITER_STATE в ответ отсылает индивидуально TOKEN_WAS_HERE_MESSAGE_RECENTLY_MESSAGE, 
  OWNER_STATE отсылает TOKEN_IS_HERE_MESSAGE

  GENERATING_STATE не получивший GERERATING_TOKEN с меньшим priority, или TOKEN_WAS_HERE_MESSAGE_RECENTLY_MESSAGE, или TOKEN_IS_HERE_MESSAGE, Радостно инкрементит CURRENT_TOKEN_NUMBER (он становится, каким он его рассылал в приоритете), и броадкастит TOKEN_IS_HERE_MESSAGE и считает, что токен у него


