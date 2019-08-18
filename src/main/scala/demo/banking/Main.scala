package demo.banking

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import demo.banking.dataaccess.Repository.BankRepositoryImpl
import demo.banking.dataaccess.provider.{PostgresDbProvider, PostgresTransactor}
import demo.banking.services.BankRegisterServiceImpl

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Main extends App {

  implicit val ac: ActorSystem = ActorSystem()
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = ac.dispatcher

  // IMPORTANT - DO NOT DELETE THIS
  PostgresTransactor.createTable

  val service = new BankRegisterServiceImpl(new BankRepositoryImpl(new PostgresDbProvider))

  val totalInserted = service.registerBanks(Paths.get("/Users/schauhan/somedata.csv"))

  val result = for {
    records <- totalInserted
    record <- service.getBankInfo(10040000)
  } yield (records,record)

  result.onComplete {
    case Success(value) =>
      println(s"Bank Name: ${value._2.get.name}")
      ac.terminate()
    case Failure(ex) =>
      println(ex.getMessage)
      ac.terminate()
  }
}
