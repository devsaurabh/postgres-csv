# Scala CSV to DB (Postgres)

## Requirement
1. Given a csv file with the bank names and bank identifiers, insert the data in DB (preferrably Postgres).
2. Able to read the data back from DB

## Assumptions
1. Number of records in the file can vary and possibly not possible to load the whole file at one go.
2. Optimize the performance by using multiple threads and processors

## Libraries Used
1. Doobie for DB Operations (https://github.com/tpolecat/doobie)
2. Hikari for managing connection pool
3. Scala Cats for functional programming(https://typelevel.org/cats/)
4. Akka Sources and Sink (https://akka.io/)
5. ScalaTest for unit testing
6. SBT for project management

## Requirements
- Scala version: 2.12.7
- SBT Version: 1.2.8
- Postgres

## Running the project
In order to run the project, modify the application.conf inside `source\main\resources`
```
dbConfig = {
  url = "jdbc:postgresql://localhost:5432/banking"
  username = "postgres"
  password = ""
  threadPoolSize = 32
}
```
and replace the connection url, username, passowrd

## Approach
The idea is to stream the csv file and insert them in the database in parallel using Flow operation
Source is the CSV File. Intermediate flows are Line parser and mapping the row to object (BankInformation)
```
FileIO.fromPath(csvFile)
        .via(CsvParsing.lineScanner(delimiter = ';'))
        .via(CsvToMap.toMapAsStrings())
        .via(BankRegisterServiceImpl.mapToBankInfo)
        .via(registerBankFlowAsync)
        .runWith(BankRegisterServiceImpl.sumInsertedBanksSink)
```

After that insert the records in database in parallel calling `registerBankFlowAsync`
```
private val registerBankFlowAsync: Flow[Option[BankInformation], Int, NotUsed] =
    Flow[Option[BankInformation]]
    .mapAsync(5) {
      case None =>
        println("Invalid row. Skipping")
        Future.successful(0)
      case Some(value: BankInformation) =>
        val res: Future[Either[String, BankInformation]] = bankRepository.safeInsertBankInfo(value)
        res.flatMap {
          case Left(error) =>
            println(s"Id: ${value.identifier}. $error")
            Future.successful(0)
          case Right(_) => Future.successful(1)
        }
    }
```

## Limitations
The system inserts one record at a time to DB. The reason not to choose batch operation is to handle the failed/duplicate rows individually. To enhance this design further we can have a stored proc which can accept list of info and return list of duplicates.

Also, there is no 
The reason for not inserting records in batches to DB is to keep track of failed/duplicate records and minimize failures. Although the same pro
