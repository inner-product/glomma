# Glomma

A bookstore simulation


## Usage

Run the server. From within sbt:

``` sh
project ingest
run
```

You should see something like

``` sh
[info] running (fork) glomma.ingest.IngestServer 
[info] 20:02:52.709 [io-compute-8] INFO org.http4s.blaze.channel.nio1.NIO1SocketServerGroup - Service bound to address /127.0.0.1:8808
[info] 20:02:52.713 [blaze-acceptor-0-0] DEBUG org.http4s.blaze.channel.nio1.SelectorLoop - Channel initialized.
[info] 20:02:52.714 [io-compute-8] INFO org.http4s.blaze.server.BlazeServerBuilder - 
[info]   _   _   _        _ _
[info]  | |_| |_| |_ _ __| | | ___
[info]  | ' \  _|  _| '_ \_  _(_-<
[info]  |_||_\__|\__| .__/ |_|/__/
[info]              |_|
[info] 20:02:52.776 [io-compute-8] INFO org.http4s.blaze.server.BlazeServerBuilder - http4s v0.15.1 on blaze v0.15.1 started at http://127.0.0.1:8808/
```

which indicates the ingestion server is running.


Now let's run the test client, which will generate data and send it to the server. Run sbt from a different terminal and then

``` sh
project data
run
```

Now we can check the result. First let's see the (approximate) most viewed and purchased books, and the customers that made the most purchases.

We can do this on the command line, or you can use visit the page in your web browser. 
``` sh
curl http://localhost:8808/stats
```

You should see output like the following.

``` json
{
  "views":[["Dulce et Decorum Est",7],["A Confederacy of Dunces",359],["Kane and Abel",180],["Lumberjanes: To The Max Edition, Volume 2",3],["The Diary of Anne Frank (Het Achterhuis)",194],["A Wrinkle in Time",3],["Blithe Spirit",2],["Les Misérables",7],["Black Beauty",4],["Brideshead Revisited",1],["Man's Search for Meaning (Ein Psychologe erlebt das Konzentrationslager)",85],["She: A History of Adventure",50],["Wolf Totem (狼图腾)",1],["After Many a Summer Dies the Swan",7],["Moab Is My Washpot",2]],
  "purchases":[["Dulce et Decorum Est",1],["A Confederacy of Dunces",122],["Kane and Abel",67],["Lumberjanes: To The Max Edition, Volume 2",1],["The Diary of Anne Frank (Het Achterhuis)",65],["A Wrinkle in Time",1],["Blithe Spirit",2],["Les Misérables",12],["Black Beauty",1],["Man's Search for Meaning (Ein Psychologe erlebt das Konzentrationslager)",37],["She: A History of Adventure",17],["A Scanner Darkly",1],["After Many a Summer Dies the Swan",1],["The Heart Is a Lonely Hunter",2],["East of Eden",1],["Moab Is My Washpot",4]],
  "customers":[["Karoline Rasch",5],["Ida Lindahl",7],["Eivind Dørumsgaard",2],["Bente Steffensen",3],["Lise Haugsrud",1],["Bodil Dahl",2],["Ove Aavik",2],["Vigdis Sibbern",5],["Astrid Smeplass",4],["Lillian Støylen",4],["Ove Bryhn",1],["Lillian Asbjørnsen",3],["Siv Jorgensen",2]]
}⏎     
```

We can also get the total value of all the sales.

``` sh
curl http://localhost:8808/sales
```

Output should be similar to the below.

``` json
{"value":54101.699999999146}
```

We can also get the value of the sales for a particular book.

``` sh
curl -G "http://localhost:8808/sale" --data-urlencode "bookName=A Wrinkle in Time"
```
 
 ```json
{"bookName":"A Wrinkle in Time","sales":1424.0500000000006}⏎          
 ```
