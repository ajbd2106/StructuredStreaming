# StructuredStreaming

The required configuration is only one sql file

The overall structure of the code refers to the open source project waterdrop

The SQL file parsing part of the code refers to the open source project flinkStreamSQL

1.Implement socket input and console output

Configuration:
CREATE TABLE SocketTable(
    word String,
    valuecount int
)WITH(
    type='socket',
    host='hadoop-sh1-core1',
    port='9998',
    delimiter=' '
);

create SINK console(
)WITH(
    type='console',
    outputmode='complete'
);

insert into console select word,count(*) from SocketTable group by word;

In the above statement, first create a table. The first half of it is the fields and types, the second half is the data source of type socket, and the delimiter is a space character (the comma is the default). In the following, a streaming with the same name will be created according to the name of create. table, schema is the configured field

Then create sink-output table, define console as a table, type is console, outputmode is complete (the default is also)

The statement is first an insert into (must write), the insert table is the sink table, and then the sql for the data to be processed. This example is select word, count (valuecount) from SocketTable group by word, so that the data can be Use Structured Streaming's default streaming method from socket to console

Enter:
a 2
a 2
Output:
Batch: 0

-------------------------------------------
+----+--------+
|WORD|count(1)|
+----+--------+
+----+--------+

-------------------------------------------
Batch: 1
-------------------------------------------
+----+--------+
|WORD|count(1)|
+----+--------+
|a   |4       |
+----+--------+

2.Kafka input console output

CREATE TABLE kafkaTable(
    word string,
    wordcount int
)WITH(
    type='kafka',
    kafka.bootstrap.servers='dfttshowkafka001:9092',
    subscribe='test',
    group='test'
);

create SINK consoleOut(
)WITH(
    type='console',
    outputmode='complete',
    process='2s'
);

insert into consoleOut select word,count(wordcount) from kafkaTable group by word;

The above statement is the same as before, there is an additional process = '2s' in the consoleOut configuration, which means that the console outputs once every 2 seconds

3. Implement csv input console output
CREATE TABLE csvTable(
    name string,
    age int
)WITH(
    type='csv',
    delimiter=';',
    path='F:\E\wordspace\sqlstream\filepath'
);

create SINK console(
)WITH(
    type='console',
    outputmode='complete',
);

insert into console select name,sum(age) from csvTable group by name;

The data in the input csv file is:

zhang; 23
wang; 24
li; 25
zhang; 56

The output is:
root
 |-- NAME: string (nullable = true)
 |-- AGE: integer (nullable = true)

-------------------------------------------
Batch: 0
-------------------------------------------
+-----+--------+
|NAME |sum(AGE)|
+-----+--------+
|zhang|79      |
|wang |24      |
|li   |25      |
+-----+--------+


4.Implement socket input and console output, add processtime window function

CREATE TABLE SocketTable(
    word String
)WITH(
    type='socket',
    host='hadoop-sh1-core1',
    processwindow='10 seconds,5 seconds',
    watermark='10 seconds',
    port='9998'
);

create SINK console(
)WITH(
    type='console',
    outputmode='complete'
);

insert into console select processwindow,word,count(*) from SocketTable group by processwindow,word;

There are two more parameters in the socket above, processwindow and watermark. Processwindow is actually similar to sparkstreaming's streaming processing. The front is window, the latter is slide, and writing one or two is a flip window.

Watermark is a delay, that is, how long is your data allowed to be late. This seems to be meaningless in processtime.

In the SQL statement, processwindow actually contains two values, the start and end of the window. Let's look at the results.
-------------------------------------------
Batch: 0
-------------------------------------------
+-------------+----+--------+
|PROCESSWINDOW|WORD|count(1)|
+-------------+----+--------+
+-------------+----+--------+

-------------------------------------------
Batch: 1
-------------------------------------------
+------------------------------------------+----+--------+
|PROCESSWINDOW                             |WORD|count(1)|
+------------------------------------------+----+--------+
|[2018-12-11 19:17:00, 2018-12-11 19:17:10]|c   |1       |
|[2018-12-11 19:17:00, 2018-12-11 19:17:10]|a   |3       |
+------------------------------------------+----+--------+

-------------------------------------------
Batch: 2
-------------------------------------------
+------------------------------------------+----+--------+
|PROCESSWINDOW                             |WORD|count(1)|
+------------------------------------------+----+--------+
|[2018-12-11 19:17:00, 2018-12-11 19:17:10]|c   |2       |
|[2018-12-11 19:17:00, 2018-12-11 19:17:10]|a   |4       |
+------------------------------------------+----+--------+

The select part in sql can also remove the PROCESSWINDOW parameter without adding processwindow, but the group part must be added so that the data can be grouped according to the window

4.Implement socket input console output, add eventtime window function

The difference between eventtime and processtime is that eventtime processes data according to event events, and process processes one by one.

CREATE TABLE SocketTable(
    timestamp Timestamp,
    word String
)WITH(
    type='socket',
    host='hadoop-sh1-core1',
    eventfield='timestamp',
    eventwindow='10 seconds,5 seconds',
    watermark='10 seconds',
    port='9998'
);

create SINK console(
)WITH(
    type='console',
    outputmode='complete'
);

insert into console select eventwindow,word,count(*) from SocketTable group by eventwindow,word;
eventtime ——> Generated according to the event. There must be a field in your data that represents time. In the above example, the timestamp field represents the time field, and the type is Timestamp.

There is an eventfield configuration in the lower part of the configuration, which is to specify which of the previous fields is used as the event time.

eventwindow and processtime mean almost the same thing

Watermark is the time allowed to delay the event, because according to the event time processing, there will definitely be first come, first come, watermark is set to 10 seconds, that is to allow your record time to be delayed by 10 seconds, and later, more than 10 seconds of data, then later Will be discarded.

Schema printed during operation
root
 |-- TIMESTAMP: timestamp (nullable = true)
 |-- WORD: string (nullable = true)
 |-- eventwindow: struct (nullable = true)
 |    |-- start: timestamp (nullable = true)
 |    |-- end: timestamp (nullable = true)

Input data
2018-12-07 16:36:12,a
2018-12-07 16:36:22,a
2018-12-07 16:36:32,b
2018-12-07 16:36:42,a
2018-12-07 16:36:52,a

Output result
Batch: 0
-------------------------------------------
+-----------+----+--------+
|EVENTWINDOW|WORD|count(1)|
+-----------+----+--------+
+-----------+----+--------+

-------------------------------------------
Batch: 1
-------------------------------------------
+------------------------------------------+----+--------+
|EVENTWINDOW                               |WORD|count(1)|
+------------------------------------------+----+--------+
|[2018-12-07 16:36:05, 2018-12-07 16:36:15]|a   |1       |
|[2018-12-07 16:36:10, 2018-12-07 16:36:20]|a   |1       |
+------------------------------------------+----+--------+

-------------------------------------------
Batch: 2
-------------------------------------------
+------------------------------------------+----+--------+
|EVENTWINDOW                               |WORD|count(1)|
+------------------------------------------+----+--------+
|[2018-12-07 16:36:30, 2018-12-07 16:36:40]|b   |1       |
|[2018-12-07 16:36:15, 2018-12-07 16:36:25]|a   |1       |
|[2018-12-07 16:36:45, 2018-12-07 16:36:55]|a   |1       |
|[2018-12-07 16:36:40, 2018-12-07 16:36:50]|a   |1       |
|[2018-12-07 16:36:20, 2018-12-07 16:36:30]|a   |1       |
|[2018-12-07 16:36:50, 2018-12-07 16:37:00]|a   |1       |
|[2018-12-07 16:36:25, 2018-12-07 16:36:35]|b   |1       |
|[2018-12-07 16:36:05, 2018-12-07 16:36:15]|a   |1       |
|[2018-12-07 16:36:10, 2018-12-07 16:36:20]|a   |1       |
|[2018-12-07 16:36:35, 2018-12-07 16:36:45]|a   |1       |
+------------------------------------------+----+--------+

5. Change the SQL statement without restarting the project to achieve the update

Only dynamic addition is currently implemented, and dynamic deletion is pending

kafka is configured as
CREATE TABLE kafkaTable(
    word string,
    wordcount int
)WITH(
    type='kafka',
    kafka.bootstrap.servers='dfttshowkafka001:9092',
    processwindow='10 seconds,10 seconds',
    watermark='10 seconds',
    subscribe='test',
    group='test'
);

create SINK consoleOut(
)WITH(
    type='console',
    outputmode='complete',
    process='10s'
);

insert into consoleOut select word,count(*) from kafkaTable group by word;

Enter:
a
a
cc
c
c
cc
Output:
root
 |-- WORD: string (nullable = true)
 |-- WORDCOUNT: integer (nullable = true)
 |-- timestamp: timestamp (nullable = true)
 |-- processwindow: struct (nullable = false)
 |    |-- start: timestamp (nullable = true)
 |    |-- end: timestamp (nullable = true)

table:KAFKATABLE
-------------------------------------------
Batch: 0
-------------------------------------------
+----+--------+
|WORD|count(1)|
+----+--------+
+----+--------+
-------------------------------------------
Batch: 1
-------------------------------------------
+----+--------+
|WORD|count(1)|
+----+--------+
|a   |2       |
|c   |2       |
|cc  |2       |
+----+--------+

Adding a configuration or modifying the current configuration will be added by default, and has no effect on the current

CREATE TABLE kafkaTable(
    word string
)WITH(
    type='kafka',
    kafka.bootstrap.servers='dfttshowkafka001:9092',
    processwindow='10 seconds,10 seconds',
    watermark='10 seconds',
    subscribe='test',
    group='test'
);

create SINK consoleOut(
)WITH(
    type='console',
    outputmode='complete',
    process='10s'
);

insert into consoleOut select processwindow,word,count(*) from kafkaTable group by word,processwindow;
Compared with the above, a processwindow is added

Run ZkNodeCRUD, update the configuration, and output

SQL updated Table
Delimiter is not configured, default is comma

root
 |-- WORD: string (nullable = true)
 |-- timestamp: timestamp (nullable = true)
 |-- processwindow: struct (nullable = false)
 |    |-- start: timestamp (nullable = true)
 |    |-- end: timestamp (nullable = true)

table:KAFKATABLE
-------------------------------------------
Batch: 0
-------------------------------------------
+-------------+----+--------+
|PROCESSWINDOW|WORD|count(1)|
+-------------+----+--------+
+-------------+----+--------+
Continue typing:
a
a
cc
c
c
cc
Output:

-------------------------------------------
Batch: 2
-------------------------------------------
+----+--------+
|WORD|count(1)|
+----+--------+
|c   |4       |
|a   |4       |
|cc  |4       |
+----+--------+
-------------------------------------------
Batch: 1
-------------------------------------------
+------------------------------------------+----+--------+
|PROCESSWINDOW                             |WORD|count(1)|
+------------------------------------------+----+--------+
|[2018-12-17 16:45:10, 2018-12-17 16:45:20]|a   |2       |
|[2018-12-17 16:45:10, 2018-12-17 16:45:20]|c   |2       |
|[2018-12-17 16:45:10, 2018-12-17 16:45:20]|cc  |2       |
+------------------------------------------+----+--------+


Both windows are updated

Achieve a dynamic update of the new version (direct replacement, no restart required).

CREATE TABLE kafkaTable(
    word string
)WITH(
    type='kafka',
    kafka.bootstrap.servers='dfttshowkafka001:9092',
    processwindow='10 seconds,10 seconds',
    watermark='10 seconds',
    subscribe='test',
    group='test'
);

create SINK consoleOut(
)WITH(
    type='console',
    outputmode='complete',
    process='10s'
);

insert into consoleOut select word,count(*) from kafkaTable group by word,processwindow;

start up
Delimiter is not configured, default is comma
root
 |-- WORD: string (nullable = true)
 |-- timestamp: timestamp (nullable = true)
 |-- processwindow: struct (nullable = false)
 |    |-- start: timestamp (nullable = true)
 |    |-- end: timestamp (nullable = true)

table:KAFKATABLE
first add :e753351e-9777-448e-b649-704e59c11755
-------------------------------------------
Batch: 0
-------------------------------------------
+----+--------+
|WORD|count(1)|
+----+--------+
+----+--------+

Query made progress: e753351e-9777-448e-b649-704e59c11755
active query length:1

Input data
-------------------------------------------
Batch: 1
-------------------------------------------
+----+--------+
|WORD|count(1)|
+----+--------+
|c   |7       |
|a   |3       |
+----+--------+

Replace SQL
CREATE TABLE kafkaTable(
    word string
)WITH(
    type='kafka',
    kafka.bootstrap.servers='dfttshowkafka001:9092',
    processwindow='10 seconds,10 seconds',
    watermark='10 seconds',
    subscribe='test',
    group='test'
);

create SINK consoleOut(
)WITH(
    type='console',
    outputmode='complete',
    process='10s'
);

insert into consoleOut select processwindow,word,count(*) from kafkaTable group by word,processwindow;

Add a processwindow display in sql

Input data

SQL updated 
Delimiter is not configured, default is comma

root
 |-- WORD: string (nullable = true)
 |-- timestamp: timestamp (nullable = true)
 |-- processwindow: struct (nullable = false)
 |    |-- start: timestamp (nullable = true)
 |    |-- end: timestamp (nullable = true)

table:KAFKATABLE
Query started: 549cf074-6cf9-4934-b57d-0932230320e8
zhiqian :e753351e-9777-448e-b649-704e59c11755
new de  :549cf074-6cf9-4934-b57d-0932230320e8
Query terminated: e753351e-9777-448e-b649-704e59c11755
-------------------------------------------
Batch: 0
-------------------------------------------
+-------------+----+--------+
|PROCESSWINDOW|WORD|count(1)|
+-------------+----+--------+
+-------------+----+--------+

Query made progress: 549cf074-6cf9-4934-b57d-0932230320e8
active query length:1
-------------------------------------------
Batch: 1
-------------------------------------------
+------------------------------------------+----+--------+
|PROCESSWINDOW                             |WORD|count(1)|
+------------------------------------------+----+--------+
|[2019-01-03 14:49:30, 2019-01-03 14:49:40]|a   |3       |
|[2019-01-03 14:49:30, 2019-01-03 14:49:40]|c   |8       |
+------------------------------------------+----+--------+

Query made progress: 549cf074-6cf9-4934-b57d-0932230320e8
active query length:1
Query made progress: 549cf074-6cf9-4934-b57d-0932230320e8
active query length:1
Query made progress: 549cf074-6cf9-4934-b57d-0932230320e8
active query length:1
-------------------------------------------
Batch: 2
-------------------------------------------
+------------------------------------------+----+--------+
|PROCESSWINDOW                             |WORD|count(1)|
+------------------------------------------+----+--------+
|[2019-01-03 14:49:50, 2019-01-03 14:50:00]|a   |3       |
|[2019-01-03 14:49:30, 2019-01-03 14:49:40]|a   |3       |
|[2019-01-03 14:49:50, 2019-01-03 14:50:00]|c   |7       |
|[2019-01-03 14:50:00, 2019-01-03 14:50:10]|c   |1       |
|[2019-01-03 14:49:30, 2019-01-03 14:49:40]|c   |8       |
+------------------------------------------+----+--------+


The jobid is updated, and the previous window is gone, which solves the bug that it can only be added but cannot be deleted.

6. Add spark configuration parameters to the configuration for tuning (to be implemented)

7.Custom UDF function (to be implemented)

8. Monitoring (to be implemented)

9. Pressure test (to be implemented)

10.Complete code structure and log adjustment


